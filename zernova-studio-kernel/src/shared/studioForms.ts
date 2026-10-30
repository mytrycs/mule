/** Serializable fields for a draggable form rendered inside an existing Studio editor. */
export interface FormField {key:string;label:string;value:string;multiline?:boolean;hint?:string;browse?:boolean;options?:{value:string;label:string}[];section?:string;maxLength?:number;}
export interface StudioFormView {id:string;title:string;fields:FormField[];help:string;error?:string;busy?:boolean;}
export type StudioForm = <T>(title:string,fields:FormField[],submit:(values:Record<string,string>)=>Promise<T>,help?:string)=>Promise<T|undefined>;
