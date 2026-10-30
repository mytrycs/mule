#!/usr/bin/env python3
# Copyright (c) 2026 ZerNova. Original native test infrastructure.
# Preparation downloads happen in CI, never when a user runs the bundled VSIX.
from pathlib import Path,PurePosixPath
import sys,os,json,hashlib,urllib.request,urllib.error,concurrent.futures,tarfile,zipfile,shutil,stat,subprocess
TARGET=sys.argv[1]
ROOT=Path(__file__).resolve().parent
os.chdir(ROOT)
INPUT=ROOT/'native-inputs';INPUT.mkdir(exist_ok=True)
OUT=ROOT/'runtime-baseline';OUT.mkdir(exist_ok=False)
FONTS={'.ttf','.otf','.woff','.woff2','.eot','.ttc'}
def sha(p):
 h=hashlib.sha256()
 with p.open('rb') as f:
  for b in iter(lambda:f.read(1024*1024),b''):h.update(b)
 return h.hexdigest()
def download(url,p,expected=None,auth=False):
 headers={'User-Agent':'ZERNOVA-native-test'}
 if auth:
  headers['Authorization']='Bearer '+os.environ['GH_TOKEN']
  class NoRedirect(urllib.request.HTTPRedirectHandler):
   def redirect_request(self,*args):return None
  try:r=urllib.request.build_opener(NoRedirect).open(urllib.request.Request(url,headers=headers),timeout=60)
  except urllib.error.HTTPError as e:
   if e.code!=302:raise
   r=urllib.request.urlopen(e.headers['Location'],timeout=180)
 else:r=urllib.request.urlopen(urllib.request.Request(url,headers=headers),timeout=180)
 p.parent.mkdir(parents=True,exist_ok=True)
 with r,p.open('wb') as f:shutil.copyfileobj(r,f,1024*1024)
 actual=sha(p)
 if expected and actual!=expected:raise ValueError('Checksum mismatch: '+p.name)
 return actual
def safe(name):
 p=PurePosixPath(name)
 if p.is_absolute() or '..' in p.parts or '\\' in name or ':' in name:raise ValueError('Unsafe archive path')
 if p.suffix.lower() in FONTS:raise ValueError('Font files cannot be delivered')
 return p
def unzip(src,dest,strip=True):
 with zipfile.ZipFile(src) as z:
  for m in z.infolist():
   p=safe(m.filename)
   if strip:p=PurePosixPath(*p.parts[1:])
   if not p.parts:continue
   target=dest/str(p)
   if stat.S_ISLNK(m.external_attr>>16):raise ValueError('Unexpected ZIP link')
   if m.is_dir():target.mkdir(parents=True,exist_ok=True);continue
   target.parent.mkdir(parents=True,exist_ok=True);target.write_bytes(z.read(m))
   target.chmod(0o755 if (m.external_attr>>16)&0o111 else 0o644)
ARTIFACTS={
 'engine':(12943114206,'62a1372d5f9f03248060b8a6f59555c484390c68b9db5cadfbbb5b020bdc0775'),
 'java-unix':(12945708080,'67bc854d6d9b9f9fe1a11415d977403960b56078a9f372758c732fc54a1d8770'),
 'win32-x64':(12946237379,'e91b73f4bf70f1579ed2ddd5d411b95c923916a2af2054048caac8edb1b726f4'),
 'win32-arm64':(12946346467,'b98d086031752b28db94405c11e26fd1ad771f321083477af8767ecef2a4701e')}
def recover(key):
 aid,digest=ARTIFACTS[key];p=INPUT/(key+'.zip');download(f'https://api.github.com/repos/mytrycs/mule/actions/artifacts/{aid}/zip',p,digest,True)
list(concurrent.futures.ThreadPoolExecutor(max_workers=2).map(recover,['engine',TARGET if TARGET.startswith('win32') else 'java-unix']))
with zipfile.ZipFile(INPUT/'engine.zip') as z:
 name='ZERNOVA_4.10_Compiled_Source_Checkpoint.tar.gz'
 with z.open(name) as src,(INPUT/name).open('wb') as dest:shutil.copyfileobj(src,dest)
sourcezip=INPUT/'corresponding-source.zip';engine=INPUT/'source-built-engine.zip'
with tarfile.open(INPUT/name) as tar,zipfile.ZipFile(sourcezip,'w',zipfile.ZIP_DEFLATED) as z:
 for m in tar:
  if not m.isfile():continue
  safe(m.name)
  if m.name.startswith('standalone-assembly/') and m.name.endswith('.zip'):
   with tar.extractfile(m) as f,engine.open('wb') as d:shutil.copyfileobj(f,d)
  elif '-source/' in m.name or m.name.startswith('kernel-source/') or m.name.startswith('evidence/'):
   z.writestr(m.name,tar.extractfile(m).read())
assert sha(engine)=='394d3583b4d8ab3554e8957b5acb20e7bcb9e43524d55f20e18b0f697706f05f'
unzip(engine,OUT/'engine')
if TARGET.startswith('win32'):
 with zipfile.ZipFile(INPUT/(TARGET+'.zip')) as z:
  name='zernova-java-'+TARGET+'.zip';java=INPUT/name;java.write_bytes(z.read(name));provenance=json.loads(z.read('provenance.json').decode('utf-8-sig'))
 vendor='Microsoft';version='17.0.21.0.2';java_sha=sha(java)
 unzip(java,OUT/'java');java_home='java'
else:
 with zipfile.ZipFile(INPUT/'java-unix.zip') as z:
  records=json.loads(z.read('java-platform-inputs.json'));record=next(x for x in records if x['target']==TARGET)
  java=INPUT/record['asset'];java.write_bytes(z.read(record['asset']));java_sha=record['sha256'];assert sha(java)==java_sha
 vendor='Eclipse Temurin';version='17.0.21+9'
 with tarfile.open(java) as tar:
  files={m.name:m for m in tar.getmembers()}
  for m in tar.getmembers():
   p=safe(m.name);rel=PurePosixPath(*p.parts[1:]);dest=OUT/'java'/str(rel)
   if not rel.parts:continue
   if m.isdir():dest.mkdir(parents=True,exist_ok=True);continue
   current=m;visited=set()
   while current.issym() or current.islnk():
    if current.name in visited:raise ValueError('Archive link loop')
    visited.add(current.name)
    link=current.linkname if current.islnk() else os.path.normpath(str(PurePosixPath(current.name).parent/current.linkname))
    safe(link)
    if link not in files:raise ValueError('External Java link')
    current=files[link]
   if not current.isfile():raise ValueError('Special Java entry')
   dest.parent.mkdir(parents=True,exist_ok=True);dest.write_bytes(tar.extractfile(current).read());dest.chmod(0o755 if m.mode&0o111 else 0o644)
 java_home='java/Contents/Home' if (OUT/'java/Contents/Home/bin/java').exists() else 'java'
# These are the exact HTTP profile JARs in the Studio bundle, not replacements.
coordinates=[
 ('org.mule.connectors','mule-http-connector','1.12.2','mule-plugin','ff936aa4a36e38bf23e4a2225351fe5e32e8af14ef87e39f2ad23f17c1d2ea0e'),
 ('org.mule.connectors','mule-sockets-connector','1.2.9','mule-plugin','02f3fe0bb9ec420e0cfb61d012ee91e33e1a0e896a2099c275f5ccce2dce0be4'),
 ('org.mule.modules','mule-module-cors-kernel','1.1.2',None,'407e3ca28021600aa3d710e1e0fd37911bf69ea65a66930c991f457259ce7089'),
 ('com.sun.activation','jakarta.activation','1.2.2',None,'02156773e4ae9d048d14a56ad35d644bee9f1052a791d072df3ded3c656e6e1a'),
 ('commons-io','commons-io','2.20.0',None,'df90bba0fe3cb586b7f164e78fe8f8f4da3f2dd5c27fa645f888100ccc25dd72'),
 ('org.apache.commons','commons-lang3','3.18.0',None,'4eeeae8d20c078abb64b015ec158add383ac581571cddc45c68f0c9ae0230720'),
 ('org.mule.sdk','mule-sdk-compatibility-api','1.0.0',None,'ffdd349e6f457f538eb737f988b069960a15156f7050588e7678900d69b2d902'),
 ('org.mule.sdk','mule-sdk-api','1.0.0',None,'fe145fbeb1fc2b661d87323154342ddc188316921dfcf448b013de85a8089de7'),
 ('commons-io','commons-io','2.22.0',None,'2b9a7b1f726fb86216dbd2c8321eabe0221dbd5b1be81c18e1cb53811b104758')]
http=OUT/'connectors/http';http.mkdir(parents=True)
def dependency(item):
 g,a,v,c,digest=item;relative=g.replace('.','/')+'/'+a+'/'+v+'/'+a+'-'+v+('-'+c if c else '')+'.jar'
 p=http/'repository'/relative
 for base in ['https://repository.mulesoft.org/releases/','https://repo.maven.apache.org/maven2/']:
  try:download(base+relative,p,digest);break
  except urllib.error.HTTPError as e:
   if e.code!=404:raise
 else:raise ValueError('Pinned dependency unavailable: '+relative)
 coord={'groupId':g,'artifactId':a,'version':v,'type':'jar'}
 if c:coord['classifier']=c
 with zipfile.ZipFile(p) as z:
  names=[n for n in z.namelist() if not n.endswith('/')]
  for n in names:safe(n)
  packages=sorted({n.rsplit('/',1)[0].replace('/','.') for n in names if n.endswith('.class') and '/' in n})
  resources=sorted(n for n in names if not n.endswith('.class'))
 return {'artifactCoordinates':coord,'uri':'repository/'+relative,'packages':packages,'resources':resources}
items=list(concurrent.futures.ThreadPoolExecutor(max_workers=5).map(dependency,coordinates))
def reference(i):return {k:v for k,v in items[i].items() if k in ['artifactCoordinates','uri'] or not items[i]['artifactCoordinates'].get('classifier')}
for index,deps in [(0,[1,2,3,4,5,6,7]),(1,[8,7])]:
 data={'version':'1.2.0','artifactCoordinates':items[index]['artifactCoordinates'],'dependencies':[reference(i) for i in deps],'packages':items[index]['packages'],'resources':items[index]['resources']}
 p=(http/items[index]['uri']).parent/'classloader-model.json';p.write_text(json.dumps(data,indent=2)+'\n',encoding='utf-8')
(http/'classloader-template.json').write_text(json.dumps({'version':'1.2.0','dependencies':[reference(0),reference(1)]},indent=2)+'\n',encoding='utf-8')
# Compile only the original lifecycle agent, not any runtime engine substitutes.
agent_dir=ROOT/'runtime-support/java/io/zernova/studio';agent_dir.mkdir(parents=True)
shutil.copy2(ROOT/'RuntimeControlAgent.java',agent_dir/'RuntimeControlAgent.java')
classes=ROOT/'runtime-support/classes';classes.mkdir()
javac=shutil.which('javac');assert javac,'Native runner needs a compiler for this CI-only step'
subprocess.run([javac,'--release','17','-d',str(classes),str(agent_dir/'RuntimeControlAgent.java')],check=True)
control=OUT/'control';control.mkdir()
with zipfile.ZipFile(control/'runtime-control.jar','w',zipfile.ZIP_DEFLATED) as z:
 z.writestr('META-INF/MANIFEST.MF','Manifest-Version: 1.0\r\nPremain-Class: io.zernova.studio.RuntimeControlAgent\r\nCan-Redefine-Classes: false\r\n\r\n')
 for p in classes.rglob('*.class'):z.write(p,p.relative_to(classes).as_posix())
(OUT/'sources').mkdir();shutil.copy2(sourcezip,OUT/'sources/ZERNOVA_Runtime_Corresponding_Source.zip')
(OUT/'notices').mkdir();shutil.copy2(ROOT/'LICENSE.txt',OUT/'notices/ZERNOVA_ORIGINAL_COMPONENTS.txt')
info={'schemaVersion':1,'product':'ZERNOVA Runtime','version':'0.3.0-dev.1','engineVersion':'4.10.0-SNAPSHOT','dataweaveVersion':'2.10.0','sourceBuildCommit':'2eacf42f588ea9192e034cd8551cdfdddc8b0d87','target':TARGET,'javaHome':java_home,'javaVendor':vendor,'javaVersion':version,'javaArchitecture':'arm64' if TARGET.endswith('arm64') else 'x64','controlAgent':'control/runtime-control.jar','sourceArchive':'sources/ZERNOVA_Runtime_Corresponding_Source.zip','distributionMode':'source-built-development','inputEngineSha256':sha(engine),'inputJavaSha256':java_sha}
(OUT/'bundle.json').write_text(json.dumps(info,indent=2)+'\n',encoding='utf-8')
records={p.relative_to(OUT).as_posix():{'sha256':sha(p),'bytes':p.stat().st_size,'executable':bool(p.stat().st_mode&0o111)} for p in sorted(OUT.rglob('*')) if p.is_file()}
(OUT/'checksums.json').write_text(json.dumps({'schemaVersion':1,'files':records},indent=2)+'\n',encoding='utf-8')
print(json.dumps({'target':TARGET,'scope':'Pinned baseline inputs only; run prepare-native.py to assemble the renamed source-built runtime','files':len(records)},indent=2))
