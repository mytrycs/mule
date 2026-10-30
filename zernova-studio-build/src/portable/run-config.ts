/** Run configuration data. No shell command or Maven goal is executed. */
export interface RunConfiguration { environment: string; variables: Record<string, string>; properties: Record<string, string>; debugPort: number; redeployOnSave?: boolean; initialHeapMB?: number; maximumHeapMB?: number; jvmArguments?: string[]; workingDirectory?: string; inheritEnvironment?: boolean; startupTimeoutSeconds?: number; deploymentTimeoutSeconds?: number; stopTimeoutSeconds?: number; deploymentScanIntervalMs?:number; }
export const defaultRunConfiguration = (): RunConfiguration => ({ environment: 'development', variables: {}, properties: {}, debugPort: 5005, redeployOnSave: false, initialHeapMB:256, maximumHeapMB:768, jvmArguments:[], workingDirectory:'', inheritEnvironment:true, startupTimeoutSeconds:120, deploymentTimeoutSeconds:120, stopTimeoutSeconds:45, deploymentScanIntervalMs:1000 });
export function validateRunConfiguration(value: RunConfiguration): RunConfiguration {
  value = { ...defaultRunConfiguration(), ...value };
  for (const key of ['initialHeapMB','maximumHeapMB'] as const) if (!Number.isInteger(value[key]) || value[key]! < 16 || value[key]! > 65536) throw new Error('Heap sizes must be integers from 16 to 65536 MB.');
  if (value.initialHeapMB! > value.maximumHeapMB!) throw new Error('Initial heap cannot exceed maximum heap.');
  for (const key of ['startupTimeoutSeconds','deploymentTimeoutSeconds','stopTimeoutSeconds'] as const) if (!Number.isInteger(value[key]) || value[key]! < 1 || value[key]! > 300) throw new Error('Lifecycle timeouts must be integers from 1 to 300 seconds.');
  if(!Number.isInteger(value.deploymentScanIntervalMs)||value.deploymentScanIntervalMs!<100||value.deploymentScanIntervalMs!>60000)throw new Error('Deployment scan interval must be 100–60000 ms.');
  if (typeof value.inheritEnvironment !== 'boolean') throw new Error('Environment inheritance must be Yes or No.');
  if (typeof value.workingDirectory !== 'string' || value.workingDirectory.length > 4096 || /[\0\r\n]/.test(value.workingDirectory)) throw new Error('Working directory must be a single-line path.');
  if (!Array.isArray(value.jvmArguments) || value.jvmArguments.length > 100) throw new Error('Use at most 100 JVM options, one per line.');
  for (const argument of value.jvmArguments) {
    if (typeof argument !== 'string' || argument.length > 4096 || /[\0\r\n]/.test(argument) || /^-XX:(?:[+-])?(?:OnError|OnOutOfMemoryError|Flags|VMOptionsFile)(?:=|$)/i.test(argument) || !/^-(?:XX:[A-Za-z0-9_+-]+(?:=.*)?|X(?:ss.+|log:.+|int|comp|mixed|batch|noclassgc|share:.+|verify:.+)|(?:ea|da|esa|dsa)(?::.*)?|verbose(?::.*)?|server|-(?:add-opens|add-exports|add-reads)=.+)$/.test(argument)) throw new Error('Unsupported JVM option: ' + argument + '. Use heap and property fields for -Xms/-Xmx/-D. Runtime agents and bootstrap arguments are managed by Studio.');
  }
  if (value.redeployOnSave !== undefined && typeof value.redeployOnSave !== 'boolean') throw new Error('Redeploy on manual save must be Yes or No.');
  if (!/^[A-Za-z0-9_.-]{1,80}$/.test(value.environment)) throw new Error('Environment must use letters, numbers, dots, underscores or hyphens.');
  if (!Number.isInteger(value.debugPort) || value.debugPort < 1024 || value.debugPort > 65535) throw new Error('Debug port must be between 1024 and 65535.');
  for (const [key, item] of Object.entries(value.variables)) {
    if (!/^[A-Za-z_][A-Za-z0-9_]*$/.test(key) || /^(PATH|JAVA_HOME|JAVA_TOOL_OPTIONS|JDK_JAVA_OPTIONS|_JAVA_OPTIONS|CLASSPATH|MULE_.*|ZERNOVA_.*|LD_.*|DYLD_.*)$/i.test(key)) throw new Error('Reserved or invalid environment variable: ' + key);
    if (typeof item !== 'string' || item.length > 8192 || /[\0\r\n]/.test(item)) throw new Error('Environment variable values must be single-line strings.');
  }
  for (const [key, item] of Object.entries(value.properties)) {
    if (!/^[A-Za-z_][A-Za-z0-9_.-]*$/.test(key) || /^(java\.|javax\.|jdk\.|sun\.|mule\.|log4j|zernova\.(?:home|base|http\.|distribution\.|bootstrap))/i.test(key)) throw new Error('Reserved or invalid system property: ' + key);
    if (typeof item !== 'string' || item.length > 8192 || /[\0\r\n]/.test(item)) throw new Error('Property values must be single-line strings.');
  }
  if (Object.keys(value.variables).length > 100 || Object.keys(value.properties).length > 100) throw new Error('Use at most 100 variables and 100 properties.');
  return { deploymentScanIntervalMs:value.deploymentScanIntervalMs, initialHeapMB:value.initialHeapMB, maximumHeapMB:value.maximumHeapMB, jvmArguments:[...value.jvmArguments!], workingDirectory:value.workingDirectory, inheritEnvironment:value.inheritEnvironment, startupTimeoutSeconds:value.startupTimeoutSeconds, deploymentTimeoutSeconds:value.deploymentTimeoutSeconds, stopTimeoutSeconds:value.stopTimeoutSeconds, redeployOnSave: value.redeployOnSave === true, environment: value.environment, debugPort: value.debugPort, variables: { ...value.variables }, properties: { ...value.properties } };
}
export function parseRunFields(fields: Record<string, string>): RunConfiguration {
  const pairs = (text: string, property: boolean): Record<string, string> => {
    const result: Record<string, string> = {};
    for (const line of text.split(/\r?\n/).map(x => x.trim()).filter(Boolean)) {
      const match = (property ? /^(?:-D)?([A-Za-z_][A-Za-z0-9_.-]*)(?:=(.*))?$/ : /^([A-Za-z_][A-Za-z0-9_]*)=(.*)$/).exec(line);
      if (!match) throw new Error(property ? 'Enter one -Dname=value per line. Maven goals and shell commands are not supported by the offline builder.' : 'Enter one NAME=value per line.');
      if (Object.hasOwn(result, match[1])) throw new Error('Duplicate setting: ' + match[1]); result[match[1]] = match[2] ?? 'true';
    }
    return result;
  };
  if (fields.redeployOnSave !== undefined && !['true', 'false'].includes(fields.redeployOnSave)) throw new Error('Redeploy on manual save must be Yes or No.');
  if (fields.inheritEnvironment !== undefined && !['true','false'].includes(fields.inheritEnvironment)) throw new Error('Environment inheritance must be Yes or No.');
  return validateRunConfiguration({ deploymentScanIntervalMs:Number(fields.deploymentScanIntervalMs??1000), initialHeapMB:Number(fields.initialHeapMB ?? 256), maximumHeapMB:Number(fields.maximumHeapMB ?? 768), jvmArguments:(fields.jvmArguments ?? '').split(/\r?\n/).map(x=>x.trim()).filter(Boolean), workingDirectory:fields.workingDirectory ?? '', inheritEnvironment:fields.inheritEnvironment !== 'false', startupTimeoutSeconds:Number(fields.startupTimeoutSeconds ?? 120), deploymentTimeoutSeconds:Number(fields.deploymentTimeoutSeconds ?? 120), stopTimeoutSeconds:Number(fields.stopTimeoutSeconds ?? 45), redeployOnSave: fields.redeployOnSave === 'true', environment: fields.environment, debugPort: Number(fields.debugPort), variables: pairs(fields.variables, false), properties: pairs(fields.properties, true) });
}
