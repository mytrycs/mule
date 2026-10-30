#!/usr/bin/env python3
# Copyright (c) 2026 ZerNova. Original native test infrastructure.
# Preparation downloads happen in CI, never when a user runs the bundled VSIX.
from pathlib import Path,PurePosixPath
import tempfile
import sys,os,json,hashlib,urllib.request,urllib.error,concurrent.futures,tarfile,zipfile,shutil,stat,subprocess
TARGET=sys.argv[1]
ROOT=Path(__file__).resolve().parent
os.chdir(ROOT)
INPUT=ROOT/'native-inputs';INPUT.mkdir(exist_ok=True)
FINAL=ROOT/'runtime-inputs'
if FINAL.exists():
 record=json.loads((FINAL/'bundle.json').read_text())
 if record['target']!=TARGET:raise SystemExit('Prepared inputs belong to another target')
 expected=json.loads((FINAL/'input-checksums.json').read_text())
 actual={p.relative_to(FINAL).as_posix():hashlib.sha256(p.read_bytes()).hexdigest() for p in FINAL.rglob('*') if p.is_file() and p.name!='input-checksums.json'}
 if actual!=expected:raise SystemExit('Prepared inputs changed; move runtime-inputs aside before rebuilding')
 print('Verified existing pinned native inputs');sys.exit(0)
OUT=Path(tempfile.mkdtemp(prefix='runtime-inputs-preparing-',dir=ROOT))
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
record=next(r for r in json.loads((ROOT/'java-inputs.json').read_text()) if r['target']==TARGET)
java=INPUT/record['asset'];java_sha=record['sha256']
if not java.exists() or sha(java)!=java_sha:download(record['url'],java,java_sha)
vendor=record.get('vendor','Eclipse Temurin');version=record.get('version','17.0.21+9')
if TARGET.startswith('win32'):
 unzip(java,OUT/'jdk-input')
 jdk=OUT/'jdk-input';image=OUT/'java'
 subprocess.run([str(jdk/'bin/jlink.exe'),'--module-path',str(jdk/'jmods'),'--add-modules','ALL-MODULE-PATH','--output',str(image),'--no-header-files','--no-man-pages','--compress=2'],check=True)
 for name in ['NOTICE','LICENSE','DISCLAIMER']:
  if (jdk/name).exists():shutil.copy2(jdk/name,image/name)
 shutil.rmtree(jdk);java_home='java'
else:
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
(OUT/'notices').mkdir()
shutil.copy2(ROOT/'LICENSE.txt',OUT/'notices/ZERNOVA_ORIGINAL_COMPONENTS.txt')
info={'schemaVersion':1,'product':'ZERNOVA Runtime','version':'0.4.0-alpha.1','engineVersion':'4.10.0-SNAPSHOT','dataweaveVersion':'2.10.0','target':TARGET,'javaHome':java_home,'javaVendor':vendor,'javaVersion':version,'javaArchitecture':'arm64' if TARGET.endswith('arm64') else 'x64','controlAgent':'control/runtime-control.jar','sourceArchive':'sources/ZERNOVA_Runtime_Corresponding_Source.zip','distributionMode':'source-built-development','inputJavaSha256':java_sha}
(OUT/'bundle.json').write_text(json.dumps(info,indent=2)+'\n')
checksums={p.relative_to(OUT).as_posix():sha(p) for p in sorted(OUT.rglob('*')) if p.is_file()}
(OUT/'input-checksums.json').write_text(json.dumps(checksums,indent=2)+'\n')
OUT.rename(FINAL)
print(json.dumps({'target':TARGET,'inputs':str(FINAL),'source':'checksum-pinned vendor downloads'}))
