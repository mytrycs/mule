import test from 'node:test';
import assert from 'node:assert/strict';
import { parseRunFields, validateRunConfiguration, defaultRunConfiguration } from '../../zernova-studio-build/src/portable/run-config';
test('Maven-style properties remain literal arguments, including spaces and shell syntax', () => {
  const r = parseRunFields({ environment: 'qa', debugPort: '5005', variables: 'GREETING=hello world', properties: '-Dhttp.port=18081\n-Dmessage=literal $(never-run) and spaces' });
  assert.equal(r.properties.message, 'literal $(never-run) and spaces'); assert.equal(r.variables.GREETING, 'hello world');
});
test('Run configurations reject goals, duplicate settings and reserved injection switches', () => {
  const base = { environment: 'qa', debugPort: '5005', variables: '', properties: '' };
  for (const properties of ['clean install -DskipTests', '-Djava.class.path=/tmp', '-Dkey=one\n-Dkey=two']) assert.throws(() => parseRunFields({ ...base, properties }));
  for (const variables of ['JAVA_TOOL_OPTIONS=-javaagent:untrusted', 'LD_PRELOAD=/tmp/a', 'invalid line']) assert.throws(() => parseRunFields({ ...base, variables }));
  assert.throws(() => validateRunConfiguration({ ...defaultRunConfiguration(), debugPort: 80 }));
});
