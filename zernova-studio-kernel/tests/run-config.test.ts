import test from 'node:test';
import assert from 'node:assert/strict';
import { parseRunFields, validateRunConfiguration, defaultRunConfiguration } from '../../zernova-studio-build/src/portable/run-config';
test('Maven-style properties remain literal arguments, including spaces and shell syntax', () => {
  const r = parseRunFields({ environment: 'qa', debugPort: '5005', variables: 'GREETING=hello world', properties: '-Dhttp.port=18081\n-Dmessage=literal $(never-run) and spaces' });
  assert.equal(r.properties.message, 'literal $(never-run) and spaces'); assert.equal(r.variables.GREETING, 'hello world');
  assert.equal(parseRunFields({ environment: 'qa', debugPort: '5005', variables: '', properties: '-Denabled' }).properties.enabled, 'true');
});
test('Run configurations reject goals, duplicate settings and reserved injection switches', () => {
  const base = { environment: 'qa', debugPort: '5005', variables: '', properties: '' };
  for (const properties of ['clean install -DskipTests', '-Djava.class.path=/tmp', '-Dkey=one\n-Dkey=two']) assert.throws(() => parseRunFields({ ...base, properties }));
  for (const variables of ['JAVA_TOOL_OPTIONS=-javaagent:untrusted', 'LD_PRELOAD=/tmp/a', 'invalid line']) assert.throws(() => parseRunFields({ ...base, variables }));
  assert.throws(() => validateRunConfiguration({ ...defaultRunConfiguration(), debugPort: 80 }));
});
test('Redeploy on manual save is opt-in and validates Yes/No values', () => {
  const fields = { environment:'dev',debugPort:'5005',variables:'',properties:'' };
  assert.equal(defaultRunConfiguration().redeployOnSave,false);
  assert.equal(parseRunFields(fields).redeployOnSave,false);
  assert.equal(parseRunFields({...fields,redeployOnSave:'true'}).redeployOnSave,true);
  assert.throws(()=>parseRunFields({...fields,redeployOnSave:'sometimes'}));
});
test('Java launch settings validate heap, VM options, environment inheritance and timeouts', () => {
  const fields={environment:'qa',debugPort:'15005',variables:'TEST_ENV=present',properties:'-Dmessage=hello',initialHeapMB:'128',maximumHeapMB:'512',jvmArguments:'-ea\n-XX:+UseG1GC\n-Xlog:gc',workingDirectory:'${project}',inheritEnvironment:'false',startupTimeoutSeconds:'90',deploymentTimeoutSeconds:'100',stopTimeoutSeconds:'30'};
  const value=parseRunFields(fields);assert.equal(value.initialHeapMB,128);assert.equal(value.maximumHeapMB,512);assert.deepEqual(value.jvmArguments,['-ea','-XX:+UseG1GC','-Xlog:gc']);assert.equal(value.inheritEnvironment,false);assert.equal(value.workingDirectory,'${project}');
  for(const jvmArguments of ['-javaagent:/tmp/foreign.jar','-XX:OnError=touch /tmp/should-not-execute','-Xmx128m','-Dunsafe=1','@args.txt','--module=foreign']) assert.throws(()=>parseRunFields({...fields,jvmArguments}));
  assert.throws(()=>parseRunFields({...fields,maximumHeapMB:'64'}));assert.throws(()=>parseRunFields({...fields,startupTimeoutSeconds:'0'}));
});
