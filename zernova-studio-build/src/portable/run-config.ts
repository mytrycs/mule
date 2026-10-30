/** Run configuration data. No shell command or Maven goal is executed. */
export interface RunConfiguration { environment: string; variables: Record<string, string>; properties: Record<string, string>; debugPort: number; }
export const defaultRunConfiguration = (): RunConfiguration => ({ environment: 'development', variables: {}, properties: {}, debugPort: 5005 });
export function validateRunConfiguration(value: RunConfiguration): RunConfiguration {
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
  return { environment: value.environment, debugPort: value.debugPort, variables: { ...value.variables }, properties: { ...value.properties } };
}
export function parseRunFields(fields: Record<string, string>): RunConfiguration {
  const pairs = (text: string, property: boolean): Record<string, string> => {
    const result: Record<string, string> = {};
    for (const line of text.split(/\r?\n/).map(x => x.trim()).filter(Boolean)) {
      const match = (property ? /^(?:-D)?([A-Za-z_][A-Za-z0-9_.-]*)=(.*)$/ : /^([A-Za-z_][A-Za-z0-9_]*)=(.*)$/).exec(line);
      if (!match) throw new Error(property ? 'Enter one -Dname=value per line. Maven goals and shell commands are not supported by the offline builder.' : 'Enter one NAME=value per line.');
      if (Object.hasOwn(result, match[1])) throw new Error('Duplicate setting: ' + match[1]); result[match[1]] = match[2];
    }
    return result;
  };
  return validateRunConfiguration({ environment: fields.environment, debugPort: Number(fields.debugPort), variables: pairs(fields.variables, false), properties: pairs(fields.properties, true) });
}
