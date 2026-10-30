declare module 'sax' {
  interface SaxParser {
    position: number;
    startTagPosition:number;
    oncdata?:(text:string)=>void;
    line: number;
    column: number;
    onerror?: (error: Error) => void;
    ondoctype?: (declaration: string) => void;
    onopentag?: (tag: { name: string; local: string; uri: string; attributes: Record<string, unknown>;isSelfClosing:boolean }) => void;
    onclosetag?: (name: string) => void;
    ontext?: (text: string) => void;
    write(xml: string): SaxParser;
    close(): SaxParser;
  }
  export function parser(strict: boolean, options?: { xmlns?: boolean; position?: boolean; strictEntities?: boolean }): SaxParser;
}
