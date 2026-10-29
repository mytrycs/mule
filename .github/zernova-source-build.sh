#!/usr/bin/env bash
# Copyright (c) 2026 ZerNova. Original build automation.
# The upstream source files and generated POM modifications retain their licenses.
set -euo pipefail
mkdir -p source-build-evidence
for repo in kernel api; do
  git -C "$repo" diff --quiet
  git -C "$repo" rev-parse HEAD > "source-build-evidence/${repo^^}_COMMIT.txt"
done
export JAVA_HOME="${JAVA_HOME_17_X64:-${JAVA_HOME:-}}"
if [ ! -x "$JAVA_HOME/bin/java" ]; then echo 'Java 17 JDK is required for the source build.' >&2; exit 3; fi
TMP="${RUNNER_TEMP:-$PWD/.zernova-build-tools}"
mkdir -p "$TMP"
curl --fail --location --retry 3 --connect-timeout 20 --max-time 120 https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.11/apache-maven-3.9.11-bin.tar.gz --output "$TMP/apache-maven-3.9.11-bin.tar.gz"
printf '%s  %s\n' 'bcfe4fe305c962ace56ac7b5fc7a08b87d5abd8b7e89027ab251069faebee516b0ded8961445d6d91ec1985dfe30f8153268843c89aa392733d1a3ec956c9978' "$TMP/apache-maven-3.9.11-bin.tar.gz" | sha512sum --check
tar -xzf "$TMP/apache-maven-3.9.11-bin.tar.gz" -C "$TMP"
export PATH="$JAVA_HOME/bin:$TMP/apache-maven-3.9.11/bin:$PATH"
java -version 2> source-build-evidence/JAVA_VERSION.txt
mvn -version > source-build-evidence/MAVEN_VERSION.txt
python3 - <<'PY'
from pathlib import Path
import re,json
Path('source-build-evidence/maven-settings.xml').write_text('''<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"><profiles><profile><id>public-source-inputs</id><repositories><repository><id>mulesoft-public</id><url>https://repository.mulesoft.org/nexus/content/repositories/public/</url><releases><enabled>true</enabled></releases><snapshots><enabled>false</enabled></snapshots></repository></repositories><pluginRepositories><pluginRepository><id>mulesoft-public</id><url>https://repository.mulesoft.org/nexus/content/repositories/public/</url><releases><enabled>true</enabled></releases><snapshots><enabled>false</enabled></snapshots></pluginRepository></pluginRepositories></profile></profiles><activeProfiles><activeProfile>public-source-inputs</activeProfile></activeProfiles></settings>''')
changes={}
for repo in ('api','kernel'):
    p=Path(repo)/'pom.xml'; text=p.read_text()
    Path('source-build-evidence',repo+'-original-pom.xml').write_text(text)
    old='<artifactId>mule-plugin-mgmt-parent-pom</artifactId>\n        <version>4.10.0-SNAPSHOT</version>'
    assert old in text
    text=text.replace(old,old.replace('4.10.0-SNAPSHOT','4.10.0'),1)
    start=text.index('<properties>'); end=text.index('</properties>',start)
    subs=[]
    def stable(match):
        before=match.group(0)
        if repo=='kernel' and match.group(1)=='muleApiVersion': return before
        after=before.replace('-SNAPSHOT',''); subs.append({'old':before,'candidate':after}); return after
    props=re.sub(r'<([A-Za-z0-9_.-]+)>[0-9][^<\n]*-SNAPSHOT</\1>',stable,text[start:end])
    text=text[:start]+props+text[end:]
    if repo=='kernel':
        marker='<dependencyManagement>\n        <dependencies>'
        assert text.count(marker)==1
        override='\n            <!-- Source recovery: use the API compiled from pinned public source. -->\n            <dependency>\n                <groupId>org.mule.runtime</groupId>\n                <artifactId>mule-api</artifactId>\n                <version>${muleApiVersion}</version>\n            </dependency>'
        text=text.replace(marker,marker+override,1)
        subs.append({'dependencyManagementOverride':'org.mule.runtime:mule-api:1.10.0-SNAPSHOT'})
    p.write_text(text); changes[repo]=subs
Path('source-build-evidence/input-substitutions.json').write_text(json.dumps(changes,indent=2)+'\n')
Path('source-build-evidence/SCOPE.txt').write_text('Experimental pinned 4.10 source candidate, not a 4.12 rebuild. API/kernel Java sources and licenses are unchanged. Build-input substitutions are recorded. Tests are compiled because other modules require their fixture JARs, but test execution is skipped and must not be reported as passed. The shipped, tested 4.12 binary distribution and Studio remain unchanged.\n')
PY
git -C kernel diff > source-build-evidence/kernel-inputs.patch
git -C api diff > source-build-evidence/api-inputs.patch
SETTINGS="$PWD/source-build-evidence/maven-settings.xml"
ARGS=(-s "$SETTINGS" --batch-mode --no-transfer-progress -DskipTests -DskipVerifications=true -DskipRevapiCheck=true -Dmaven.javadoc.skip=true)
set +e
timeout 480 mvn -f api/pom.xml "${ARGS[@]}" install > source-build-evidence/api-build.log 2>&1
api_result=$?
printf '%s\n' "$api_result" > source-build-evidence/api-build-exit-code.txt
if [ "$api_result" = 0 ]; then
  javap -classpath api/target/mule-api-1.10.0-SNAPSHOT.jar org.mule.runtime.api.config.MuleRuntimeFeature > source-build-evidence/compiled-api-features.txt
  timeout 900 mvn -f kernel/pom.xml "${ARGS[@]}" install > source-build-evidence/kernel-build.log 2>&1
  result=$?
else
  result=125
  printf 'Kernel compilation not attempted because API build failed.\n' > source-build-evidence/kernel-build.log
fi
set -e
printf '%s\n' "$result" > source-build-evidence/kernel-build-exit-code.txt
find api kernel -path '*/target/*.jar' -type f -exec sha256sum '{}' \; > source-build-evidence/generated-jars.sha256
tail -70 source-build-evidence/api-build.log
tail -110 source-build-evidence/kernel-build.log
tar --exclude=.git --exclude=target -czf source-build-evidence/api-source.tar.gz api
python3 - <<'PY'
from pathlib import Path
import zipfile,hashlib,json
with zipfile.ZipFile('source-build-evidence/compiled-candidate-jars.zip','w',zipfile.ZIP_DEFLATED) as z:
    for base in ('api','kernel'):
        for p in sorted(Path(base).glob('**/target/*.jar')): z.write(p,p.as_posix())
repo=Path.home()/'.m2/repository'
records=[]
for p in sorted(repo.rglob('*')):
    if p.is_file() and p.suffix in ('.jar','.pom'):
        records.append({'path':p.relative_to(repo).as_posix(),'sha256':hashlib.sha256(p.read_bytes()).hexdigest(),'bytes':p.stat().st_size})
Path('source-build-evidence/maven-inputs.json').write_text(json.dumps(records,indent=2)+'\n')
PY
