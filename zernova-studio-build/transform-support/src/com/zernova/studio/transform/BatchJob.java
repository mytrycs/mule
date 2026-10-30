/* Copyright (c) 2026 ZerNova. Original single-instance, durable at-least-once batch jobs. */
package com.zernova.studio.transform;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.message.Message;
import com.zernova.runtime.api.metadata.MediaType;
import com.zernova.runtime.api.serialization.SerializationProtocol;
import com.zernova.runtime.core.api.processor.*;
import com.zernova.runtime.core.api.event.*;
import com.zernova.runtime.core.api.el.ExtendedExpressionManager;
import com.zernova.runtime.core.privileged.processor.MessageProcessors;
import jakarta.inject.Inject;
import java.util.*;
import java.util.concurrent.*;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.io.*;
public final class BatchJob extends AbstractMessageProcessorOwner implements Processor {
 @Inject private ExtendedExpressionManager expressions;
 private String name="batchJob",definition;private int blockSize=100,maxFailedRecords=-1;private List<Processor> children=List.of();private List<BatchStep> steps=List.of();private BatchComplete complete;private Path storage;private volatile boolean running;private ScheduledExecutorService worker;
 public void setName(String value){name=value;}public void setBlockSize(int value){blockSize=value;}public void setMaxFailedRecords(int value){maxFailedRecords=value;}public void setMessageProcessors(List<Processor> value){children=List.copyOf(value);}
 protected List<Processor> getOwnedMessageProcessors(){return children;}
 public ProcessingType getProcessingType(){return ProcessingType.BLOCKING;}
 public void initialise()throws InitialisationException{
  try{
   if(!name.matches("[A-Za-z_][A-Za-z0-9_.-]{0,99}")||blockSize<1||blockSize>10000||maxFailedRecords< -1)throw new IllegalArgumentException("Invalid Batch Job name, block size or failure limit.");
   List<BatchStep> found=new ArrayList<>();Set<String> names=new HashSet<>();for(Processor child:children){if(child instanceof BatchStep step){if(!step.name().matches("[A-Za-z_][A-Za-z0-9_.-]{0,99}"))throw new IllegalArgumentException("Invalid Batch Step name.");if(complete!=null)throw new IllegalArgumentException("On Complete must follow all steps.");if(!names.add(step.name()))throw new IllegalArgumentException("Batch Step names must be unique.");found.add(step);}else if(child instanceof BatchComplete callback&&complete==null)complete=callback;else throw new IllegalArgumentException("Batch Job accepts steps and one On Complete scope.");}if(found.isEmpty())throw new IllegalArgumentException("Batch Job needs at least one step.");steps=List.copyOf(found);
   try(InputStream source=muleContext.getExecutionClassLoader().getResourceAsStream("META-INF/zernova/package.json")){definition=hash(source==null?new byte[0]:source.readAllBytes());}
   String identity=hash((name+"/"+getLocation().getLocation()).getBytes(StandardCharsets.UTF_8));Path home=Path.of(System.getProperty("zernova.home",muleContext.getConfiguration().getWorkingDirectory())).toAbsolutePath().normalize();
   storage=privateDirectory(privateDirectory(privateDirectory(home.resolve("data")).resolve("zernova-batch")).resolve(identity));super.initialise();
  }catch(Exception error){throw new InitialisationException(error,this);}
 }
 private static String hash(byte[] data)throws java.security.NoSuchAlgorithmException{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(data));}
 private static Path privateDirectory(Path dir)throws IOException{if(Files.exists(dir,LinkOption.NOFOLLOW_LINKS)){if(Files.isSymbolicLink(dir)||!Files.isDirectory(dir,LinkOption.NOFOLLOW_LINKS))throw new IOException("Batch storage must be an ordinary directory.");}else Files.createDirectory(dir);try{Files.setPosixFilePermissions(dir,PosixFilePermissions.fromString("rwx------"));}catch(UnsupportedOperationException ignored){}return dir;}
 private SerializationProtocol codec(){return muleContext.getObjectSerializer().getInternalProtocol();}
 private void atomic(Path file,byte[] data)throws IOException{Path tmp=file.resolveSibling(file.getFileName()+"."+UUID.randomUUID()+".tmp");Files.write(tmp,data,StandardOpenOption.CREATE_NEW);try{Files.move(tmp,file,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);}catch(AtomicMoveNotSupportedException error){Files.move(tmp,file,StandardCopyOption.REPLACE_EXISTING);}}
 private void manifest(Path job,Properties state)throws IOException{ByteArrayOutputStream bytes=new ByteArrayOutputStream();state.store(bytes,"ZERNOVA Batch Job");atomic(job.resolve("manifest.properties"),bytes.toByteArray());}
 private Properties manifest(Path job)throws IOException{Path file=job.resolve("manifest.properties");if(Files.isSymbolicLink(file)||!Files.isRegularFile(file,LinkOption.NOFOLLOW_LINKS))throw new IOException("Invalid batch manifest.");Properties value=new Properties();try(InputStream input=Files.newInputStream(file)){value.load(input);}return value;}
 private CoreEvent fresh(CoreEvent source,Object payload,String jobId,long index,boolean failed,Map<String,String> errors){return CoreEvent.builder(EventContextFactory.create(UUID.randomUUID().toString(),muleContext.getId(),getLocation(),source.getCorrelationId(),Optional.empty())).message(Message.builder().value(payload).mediaType(MediaType.APPLICATION_JAVA).build()).variables(source.getVariables()).addVariable("zernovaBatch",Map.of("jobId",jobId,"recordIndex",index,"failed",failed,"errors",new HashMap<>(errors))).build();}
 private Path record(Path job,long index){return job.resolve(String.format(Locale.ROOT,"%012d.record",index));}
 private void checkpoint(Path job,long index,Map<String,Object> state)throws IOException{atomic(record(job,index),codec().serialize(state));}
 @SuppressWarnings("unchecked") private Map<String,Object> checkpoint(Path job,long index)throws IOException{Path file=record(job,index);if(Files.isSymbolicLink(file)||!Files.isRegularFile(file,LinkOption.NOFOLLOW_LINKS)||Files.size(file)>33554432)throw new IOException("Invalid batch record checkpoint.");return codec().deserialize(Files.readAllBytes(file),muleContext.getExecutionClassLoader());}
 public CoreEvent process(CoreEvent event)throws ZernovaException{
  if(!running)throw new IllegalStateException("Batch Job is stopped.");String id=UUID.randomUUID().toString();Path job=storage.resolve(id);
  try{
   privateDirectory(job);Properties state=new Properties();state.setProperty("id",id);state.setProperty("name",name);state.setProperty("definition",definition);state.setProperty("state","STAGING");manifest(job,state);
   Map<String,Object> template=new HashMap<>();template.put("event",fresh(event,null,id,-1,false,Map.of()));atomic(job.resolve("template.record"),codec().serialize(template));
   Object input=expressions.evaluate("#[payload as Array]",event,getLocation()).getValue();Iterable<?> iterable=input instanceof Iterable<?> items?items:input instanceof Object[] array?Arrays.asList(array):null;if(iterable==null)throw new IllegalArgumentException("Batch input must be a collection.");
   long index=0;for(Object payload:iterable){if(!running||Thread.currentThread().isInterrupted())throw new InterruptedException("Batch submission interrupted.");Map<String,Object> entry=new HashMap<>();entry.put("event",fresh(event,payload,id,index,false,Map.of()));entry.put("step",0);entry.put("failed",false);entry.put("errors",new HashMap<String,String>());checkpoint(job,index++,entry);}
   state.setProperty("total",Long.toString(index));state.setProperty("created",Long.toString(System.currentTimeMillis()));state.setProperty("state","READY");manifest(job,state);logger.info("ZERNOVA_BATCH_SUBMITTED jobId={} name={} records={}",id,name,index);
   return CoreEvent.builder(event).addVariable("zernovaBatchJobId",id).build();
  }catch(Exception error){throw new IllegalStateException("Cannot persist ZERNOVA batch submission "+id,error);}
 }
 public void start()throws ZernovaException{super.start();running=true;worker=Executors.newSingleThreadScheduledExecutor(task->{Thread thread=new Thread(task,"zernova-batch-"+name);thread.setDaemon(true);thread.setContextClassLoader(muleContext.getExecutionClassLoader());return thread;});worker.scheduleWithFixedDelay(this::scan,0,250,TimeUnit.MILLISECONDS);}
 private void scan(){if(!running)return;try(var jobs=Files.list(storage)){Iterator<Path> paths=jobs.filter(p->Files.isDirectory(p,LinkOption.NOFOLLOW_LINKS)&&p.getFileName().toString().matches("[a-f0-9-]{36}")).iterator();while(running&&paths.hasNext()){Path job=paths.next();Properties state=manifest(job);if(!Set.of("READY","RUNNING").contains(state.getProperty("state")))continue;if(!definition.equals(state.getProperty("definition"))){state.setProperty("state","DEFINITION_MISMATCH");manifest(job,state);logger.error("ZERNOVA_BATCH_QUARANTINED jobId={} requires its original application definition",state.getProperty("id"));continue;}processJob(job,state);}}catch(Exception error){if(running)logger.error("ZERNOVA batch scan failed",error);}}
 private record Pending(long index,Map<String,Object> state,CoreEvent event){}
 @SuppressWarnings("unchecked") private void processJob(Path job,Properties state)throws Exception{
  String id=state.getProperty("id");long total=Long.parseLong(state.getProperty("total"));state.setProperty("state","RUNNING");manifest(job,state);
  try{
   boolean aborted=false;
   for(int stepIndex=0;stepIndex<steps.size()&&running&&!aborted;stepIndex++){
    BatchStep step=steps.get(stepIndex);List<Pending> group=new ArrayList<>();long failures=failedCount(job,total);
    for(long index=0;index<total&&running;index++){
     Map<String,Object> record=checkpoint(job,index);
     if(((Number)record.get("step")).intValue()>stepIndex)continue;
     CoreEvent saved=(CoreEvent)record.get("event"),event=fresh(saved,saved.getMessage().getPayload().getValue(),id,index,Boolean.TRUE.equals(record.get("failed")),(Map<String,String>)record.get("errors"));
     try{
      if(!step.accepts(event,Boolean.TRUE.equals(record.get("failed")))){record.put("step",stepIndex+1);checkpoint(job,index,record);continue;}
      CoreEvent output=step.applyRecord(event);
      if(step.aggregator()!=null){group.add(new Pending(index,record,output));if(group.size()>=step.aggregator().size())failures+=finishGroup(job,id,stepIndex,step,group);}
      else{record.put("event",output);record.put("step",stepIndex+1);checkpoint(job,index,record);}
     }catch(Exception error){if(!running)throw new InterruptedException("Batch paused during shutdown.");if(!Boolean.TRUE.equals(record.get("failed")))failures++;fail(job,index,record,stepIndex,step.name(),error);}
     if(maxFailedRecords>=0&&failures>maxFailedRecords){aborted=true;break;}
     if((index+1)%blockSize==0)logger.debug("ZERNOVA_BATCH_PROGRESS jobId={} step={} records={}",id,step.name(),index+1);
    }
    if(running&&!group.isEmpty())failures+=finishGroup(job,id,stepIndex,step,group);
    if(maxFailedRecords>=0&&failures>maxFailedRecords)aborted=true;
   }
   if(!running)throw new InterruptedException("Batch paused.");long failures=0,processed=0,successful=0;CoreEvent first=null;
   for(long index=0;index<total;index++){Map<String,Object> record=checkpoint(job,index);if(first==null)first=(CoreEvent)record.get("event");if(Boolean.TRUE.equals(record.get("failed")))failures++;if(((Number)record.get("step")).intValue()>=steps.size()){processed++;if(!Boolean.TRUE.equals(record.get("failed")))successful++;}}
   Map<String,Object> report=new LinkedHashMap<>();report.put("jobId",id);report.put("name",name);report.put("status",aborted?"FAILED":"COMPLETED");report.put("totalRecords",total);report.put("successfulRecords",successful);report.put("unprocessedRecords",total-processed);report.put("failedRecords",failures);report.put("processedRecords",processed);
   if(first==null){Map<String,Object> template=codec().deserialize(Files.readAllBytes(job.resolve("template.record")),muleContext.getExecutionClassLoader());first=(CoreEvent)template.get("event");}
   if(complete!=null&&first!=null){CoreEvent result=fresh(first,report,id,-1,failures>0,Map.of());MessageProcessors.processToApplyWithChildContext(result,complete);}
   state.setProperty("state",aborted?"FAILED":"COMPLETED");state.setProperty("failed",Long.toString(failures));state.setProperty("processed",Long.toString(processed));manifest(job,state);logger.info("ZERNOVA_BATCH_COMPLETE jobId={} name={} status={} total={} failed={} processed={}",id,name,state.getProperty("state"),total,failures,processed);
  }catch(InterruptedException paused){state.setProperty("state","READY");manifest(job,state);logger.info("ZERNOVA_BATCH_PAUSED jobId={}",id);}
  catch(Exception error){state.setProperty("state","FAILED");state.setProperty("error",error.getClass().getSimpleName()+": "+String.valueOf(error.getMessage()));manifest(job,state);logger.error("ZERNOVA_BATCH_FAILED jobId={}",id,error);}
 }
 @SuppressWarnings("unchecked") private void fail(Path job,long index,Map<String,Object> record,int stepIndex,String step,Exception error)throws IOException{record.put("failed",true);record.put("step",stepIndex+1);Map<String,String> errors=(Map<String,String>)record.get("errors");String message=error.getClass().getSimpleName()+": "+String.valueOf(error.getMessage());errors.put(step,message.substring(0,Math.min(message.length(),4096)));checkpoint(job,index,record);logger.warn("ZERNOVA_BATCH_RECORD_FAILED jobId={} record={} step={}",job.getFileName(),index,step);}
 private long failedCount(Path job,long total)throws IOException{long count=0;for(long i=0;i<total;i++)if(Boolean.TRUE.equals(checkpoint(job,i).get("failed")))count++;return count;}
 private long finishGroup(Path job,String id,int stepIndex,BatchStep step,List<Pending> group)throws Exception{
  long failed=0;
  try{List<Object> payloads=new ArrayList<>();for(Pending item:group)payloads.add(item.event.getMessage().getPayload().getValue());CoreEvent aggregated=fresh(group.get(0).event,payloads,id,group.get(0).index,false,Map.of());MessageProcessors.processToApplyWithChildContext(aggregated,step.aggregator());for(Pending item:group){item.state.put("event",item.event);item.state.put("step",stepIndex+1);checkpoint(job,item.index,item.state);}}
  catch(Exception error){if(!running)throw new InterruptedException("Batch aggregation paused.");for(Pending item:group){if(!Boolean.TRUE.equals(item.state.get("failed")))failed++;fail(job,item.index,item.state,stepIndex,step.name(),error);}}
  finally{group.clear();}
  return failed;
 }
 public void stop()throws ZernovaException{running=false;if(worker!=null){worker.shutdown();try{if(!worker.awaitTermination(5,TimeUnit.SECONDS))worker.shutdownNow();}catch(InterruptedException error){Thread.currentThread().interrupt();worker.shutdownNow();}}super.stop();}
}
