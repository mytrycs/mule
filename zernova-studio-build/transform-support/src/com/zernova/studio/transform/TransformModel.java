/* Copyright (c) 2026 ZerNova. Native extension declaration for Transform Message. */
package com.zernova.studio.transform;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.XmlDslModel;
import com.zernova.runtime.api.meta.model.declaration.fluent.*;
import com.zernova.runtime.extension.api.provider.RuntimeExtensionModelProvider;
import com.zernova.runtime.extension.api.loader.*;
import com.zernova.runtime.core.api.extension.provider.ZernovaExtensionModelProvider;
import com.zernova.metadata.api.builder.BaseTypeBuilder;
import com.zernova.metadata.api.model.MetadataFormat;
import static com.zernova.runtime.api.meta.ExpressionSupport.NOT_SUPPORTED;
import static com.zernova.runtime.api.dsl.DslResolvingContext.nullDslResolvingContext;
public final class TransformModel extends ExtensionModelLoader implements RuntimeExtensionModelProvider {
 public String getId(){return "zernova-transform";}
 protected void declareExtension(ExtensionLoadingContext context){
  ExtensionDeclarer d=context.getExtensionDeclarer().named("ZERNOVA Transform").onVersion("0.5.0").fromVendor("ZerNova").withCategory(com.zernova.runtime.api.meta.Category.COMMUNITY).supportingJavaVersions(java.util.Set.of("17"))
   .withModelProperty(ZernovaExtensionModelProvider.getExtensionModel().getModelProperties().stream().filter(p->p.getName().equals("customBuildingDefinition")).findFirst().orElseThrow())
   .withXmlDsl(XmlDslModel.builder().setPrefix("zt").setNamespace("https://schemas.zernova.dev/transform").setSchemaVersion("0.5.0").setXsdFileName("transform.xsd").setSchemaLocation("https://schemas.zernova.dev/transform/current/transform.xsd").build());
  OperationDeclarer op=d.withOperation("message");
  op.withOutput().ofType(BaseTypeBuilder.create(MetadataFormat.JAVA).voidType().build());
  op.withOutputAttributes().ofType(BaseTypeBuilder.create(MetadataFormat.JAVA).voidType().build());
  for(String name:new String[]{"payload","attributes","variables","mimeType","payloadResource","attributesResource","variablesResource"})op.onDefaultParameterGroup().withOptionalParameter(name).ofType(BaseTypeBuilder.create(MetadataFormat.JAVA).stringType().build()).withExpressionSupport(NOT_SUPPORTED);
  declare(d,"evaluate",false,new String[]{"expression"});
  declare(d,"businessEvent",false,new String[]{"name","data"});
  declare(d,"cache",true,new String[]{"key","ttlMillis","maxEntries"});
  declare(d,"batchJob",true,new String[]{"name","blockSize","maxFailedRecords"});
  declare(d,"batchStep",true,new String[]{"name","acceptExpression","acceptPolicy"});
  declare(d,"batchAggregator",true,new String[]{"size"});
  declare(d,"batchOnComplete",true,new String[]{});
 }
 private void declare(ExtensionDeclarer d,String name,boolean children,String[] fields){
  OperationDeclarer operation=d.withOperation(name);
  operation.withOutput().ofType(BaseTypeBuilder.create(MetadataFormat.JAVA).voidType().build());operation.withOutputAttributes().ofType(BaseTypeBuilder.create(MetadataFormat.JAVA).voidType().build());
  if(children)operation.withChain().withModelProperty(com.zernova.runtime.extension.api.property.NoWrapperModelProperty.INSTANCE).setExecutionOccurrence(com.zernova.runtime.api.meta.model.nested.ChainExecutionOccurrence.MULTIPLE_OR_NONE);
  for(String field:fields)operation.onDefaultParameterGroup().withOptionalParameter(field).ofType(BaseTypeBuilder.create(MetadataFormat.JAVA).stringType().build()).withExpressionSupport(java.util.Set.of("expression","key","data","acceptExpression").contains(field)?com.zernova.runtime.api.meta.ExpressionSupport.SUPPORTED:NOT_SUPPORTED);
 }
 public ExtensionModel createExtensionModel(){return loadExtensionModel(ExtensionModelLoadingRequest.builder(getClass().getClassLoader(),nullDslResolvingContext()).build());}
}
