/* Copyright (c) 2026 ZerNova. Console branding and structured lifecycle stages. */
export function deploymentBanner(app:string,environment:string,studioVersion:string,runtimeVersion:string):string{
 return `\n  ███████╗\n  ╚══███╔╝   Z E R N O V A\n    ███╔╝    STUDIO ${studioVersion} · RUNTIME ${runtimeVersion}\n   ███╔╝     Application: ${app}\n  ███████╗   Environment: ${environment}\n  ╚══════╝   Java 17 · ${typeof process==='undefined'?'local':process.platform+' / '+process.arch}\n\n`;
}
export function stageLine(stage:string,message:string,elapsedMs?:number):string{
 return `[${new Date().toISOString()}] ${stage.padEnd(8)} ${message}${elapsedMs===undefined?'':` (${(elapsedMs/1000).toFixed(2)} s)`}\n`;
}
/** Only presentation boilerplate is hidden; raw JVM logs/notices retain provenance. */
export function deploymentConsole(text:string):string{
 return text.split(/\r?\n/).filter(line=>!/^Copyright \(c\) 2003-2015 MuleSoft Inc\.$/.test(line)&&!/^Powered by Mule\./.test(line)).map(line=>line.replace('Mule encoding:','Runtime encoding:')).join('\n');
}
