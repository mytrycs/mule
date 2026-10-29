#!/usr/bin/env bash
# Copyright (c) 2026 ZerNova. Original source recovery automation.
# Upstream sources, POM modifications, and dependencies retain their original licenses.
set -euo pipefail
EVIDENCE="$PWD/source-build-evidence"
mkdir -p "$EVIDENCE"
REPOS=(api extensions kernel distributions)
for repo in "${REPOS[@]}"; do
  git -C "$repo" diff --quiet
  git -C "$repo" rev-parse HEAD > "$EVIDENCE/${repo^^}_COMMIT.txt"
done
export JAVA_HOME="${JAVA_HOME_17_X64:-${JAVA_HOME:-}}"
if [ ! -x "$JAVA_HOME/bin/java" ]; then echo 'Java 17 JDK is required.' >&2; exit 3; fi
TMP="${RUNNER_TEMP:-$PWD/.zernova-build-tools}"
mkdir -p "$TMP"
curl --fail --location --retry 3 --connect-timeout 20 --max-time 120 https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.11/apache-maven-3.9.11-bin.tar.gz --output "$TMP/apache-maven-3.9.11-bin.tar.gz"
printf '%s  %s\n' 'bcfe4fe305c962ace56ac7b5fc7a08b87d5abd8b7e89027ab251069faebee516b0ded8961445d6d91ec1985dfe30f8153268843c89aa392733d1a3ec956c9978' "$TMP/apache-maven-3.9.11-bin.tar.gz" | sha512sum --check
tar -xzf "$TMP/apache-maven-3.9.11-bin.tar.gz" -C "$TMP"
export PATH="$JAVA_HOME/bin:$TMP/apache-maven-3.9.11/bin:$PATH"
java -version 2> "$EVIDENCE/JAVA_VERSION.txt"
mvn -version > "$EVIDENCE/MAVEN_VERSION.txt"
python3 - <<'PY'
from pathlib import Path
import re,json
out=Path('source-build-evidence')
out.joinpath('maven-settings.xml').write_text('''<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"><profiles><profile><id>public-source-inputs</id><repositories><repository><id>mulesoft-public</id><url>https://repository.mulesoft.org/nexus/content/repositories/public/</url><releases><enabled>true</enabled></releases><snapshots><enabled>false</enabled></snapshots></repository></repositories><pluginRepositories><pluginRepository><id>mulesoft-public</id><url>https://repository.mulesoft.org/nexus/content/repositories/public/</url><releases><enabled>true</enabled></releases><snapshots><enabled>false</enabled></snapshots></pluginRepository></pluginRepositories></profile></profiles><activeProfiles><activeProfile>public-source-inputs</activeProfile></activeProfiles></settings>''')
changes={}
for repo in ('api','extensions','kernel','distributions'):
    p=Path(repo)/'pom.xml'; text=p.read_text()
    (out/(repo+'-original-pom.xml')).write_text(text)
    subs=[]
    if repo!='distributions':
        pattern=r'(<artifactId>mule-plugin-mgmt-parent-pom</artifactId>\s*<version>)4\.10\.0-SNAPSHOT(</version>)'
        text,n=re.subn(pattern,r'\g<1>4.10.0\2',text,count=1); assert n==1
        subs.append({'parent':'org.mule:mule-plugin-mgmt-parent-pom:4.10.0'})
    start=text.index('<properties>'); end=text.index('</properties>',start)
    def stable(match):
        before=match.group(0); name=match.group(1)
        if name=='muleApiVersion' and repo in ('extensions','kernel'): return before
        if name=='muleExtensionsApiVersion' and repo=='kernel': return before
        after=before.replace('-SNAPSHOT',''); subs.append({'old':before,'candidate':after}); return after
    props=re.sub(r'<([A-Za-z0-9_.-]+)>[0-9][^<\n]*-SNAPSHOT</\1>',stable,text[start:end])
    if repo=='distributions':
        props=props.replace('<muleBomVersion>${project.version}</muleBomVersion>','<muleBomVersion>4.10.0</muleBomVersion>')
        props=props.replace('<muleDependenciesVersion>1.x</muleDependenciesVersion>','<muleDependenciesVersion>1.0.25</muleDependenciesVersion>')
        subs.append({'runtimeBOM':'4.10.0','testDependenciesBOM':'1.0.25','basis':'Both released POMs were resolved and checksum-recorded in the preceding source build.'})
    text=text[:start]+props+text[end:]
    managed=[]
    if repo in ('extensions','kernel','distributions'):
        managed.append(('mule-api','1.10.0-SNAPSHOT',None,None))
    if repo in ('kernel','distributions'):
        for artifact in ('mule-extensions-api','mule-extensions-api-persistence','mule-extensions-api-dsql','mule-extensions-mime-types'):
            managed.append((artifact,'1.10.0-SNAPSHOT',None,None))
        managed.append(('mule-extensions-api','1.10.0-SNAPSHOT','test-jar',None))
    if managed:
        addition='\n            <!-- Source recovery: select explicitly compiled API artifacts, not released BOM binaries. -->'
        for artifact,version,typ,classifier in managed:
            addition+='\n            <dependency><groupId>org.mule.runtime</groupId><artifactId>'+artifact+'</artifactId><version>'+version+'</version>'
            if typ: addition+='<type>'+typ+'</type>'
            if classifier: addition+='<classifier>'+classifier+'</classifier>'
            addition+='</dependency>'
        pattern=r'(<dependencyManagement>\s*<dependencies>)'
        text,n=re.subn(pattern,lambda m:m.group(1)+addition,text,count=1); assert n==1
        subs.append({'sourceBuiltDependencyManagement':managed})
    p.write_text(text); changes[repo]=subs
out.joinpath('input-substitutions.json').write_text(json.dumps(changes,indent=2)+'\n')
out.joinpath('SCOPE.txt').write_text('Explicit experimental 4.10 source candidate, not a 4.12 rebuild. API, extension API, kernel and standalone sources are pinned to public commits. Java sources and licenses are unchanged; POM adaptations are retained. Test code is compiled for cross-module fixtures, but tests are not executed by the install commands. The existing tested 4.12 distribution and Studio are untouched. Standalone assembly is attempted only after all source projects build successfully. No customer publication.\n')
PY
for repo in "${REPOS[@]}"; do git -C "$repo" diff > "$EVIDENCE/$repo-inputs.patch"; done
ARGS=(-s "$EVIDENCE/maven-settings.xml" --batch-mode --no-transfer-progress -DskipTests -DskipVerifications=true -DskipRevapiCheck=true -Dmaven.javadoc.skip=true)
previous=0
for repo in "${REPOS[@]}"; do
  if [ "$previous" = 0 ]; then
    extra=(); [ "$repo" != distributions ] || extra=(-pl standalone -am)
    set +e
    timeout 900 mvn -f "$repo/pom.xml" "${ARGS[@]}" "${extra[@]}" install > "$EVIDENCE/$repo-build.log" 2>&1
    result=$?
    set -e
  else
    result=125
    printf 'Not attempted because the preceding source build failed.\n' > "$EVIDENCE/$repo-build.log"
  fi
  printf '%s\n' "$result" > "$EVIDENCE/$repo-build-exit-code.txt"
  tail -100 "$EVIDENCE/$repo-build.log"
  previous=$result
done
find api extensions kernel distributions -path '*/target/*.jar' -type f -exec sha256sum '{}' \; > "$EVIDENCE/generated-jars.sha256"
for repo in api extensions distributions; do tar --exclude=.git --exclude=target -czf "$EVIDENCE/$repo-source.tar.gz" "$repo"; done
python3 - <<'PY'
from pathlib import Path
import zipfile,hashlib,json,shutil
out=Path('source-build-evidence')
with zipfile.ZipFile(out/'compiled-candidate-jars.zip','w',zipfile.ZIP_DEFLATED) as z:
    for base in ('api','extensions','kernel','distributions'):
        for p in sorted(Path(base).glob('**/target/*.jar')): z.write(p,p.as_posix())
for p in Path('distributions/standalone/target').glob('*'):
    if p.is_file() and (p.name.endswith('.tar.gz') or p.name.endswith('.zip')):
        shutil.copy2(p,out/p.name)
repo=Path.home()/'.m2/repository'; records=[]
for p in sorted(repo.rglob('*')):
    if p.is_file() and p.suffix in ('.jar','.pom'):
        records.append({'path':p.relative_to(repo).as_posix(),'sha256':hashlib.sha256(p.read_bytes()).hexdigest(),'bytes':p.stat().st_size})
(out/'maven-inputs.json').write_text(json.dumps(records,indent=2)+'\n')
PY
