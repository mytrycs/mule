import type { IntegrationModel,RealProcessor } from './xml';
/** Native AST locations for processors, nested chains and router branches. */
export function componentLocation(model:IntegrationModel,id:string):string|undefined{
 const visit=(nodes:RealProcessor[],base:string):string|undefined=>{
  for(const [index,node] of nodes.entries()){
   const location=base+'/processors/'+index;if(node.id===id)return location;
   if(node.kind!=='core')continue;
   if(['choice','scatter-gather','first-successful','round-robin'].includes(node.coreType)){
    for(const [routeIndex,route]of node.children.entries()){
     const routeLocation=location+'/route/'+routeIndex;if(route.id===id)return routeLocation;
     if(route.kind==='core'){const found=visit(route.children,routeLocation);if(found)return found;}
    }
   }else{
    const ordinary=node.children.filter(n=>n.kind!=='core'||n.coreType!=='error-handler');
    const found=visit(ordinary,location);if(found)return found;
    const handler=node.children.find(n=>n.kind==='core'&&n.coreType==='error-handler');
    if(handler?.kind==='core'){const found=handlers(handler.children,location);if(found)return found;}
   }
  }
 };
 const handlers=(routes:RealProcessor[],base:string):string|undefined=>{
  for(const [index,route]of routes.entries()){const location=base+'/errorHandler/'+index;if(route.id===id)return location;if(route.kind==='core'){const found=visit(route.children,location);if(found)return found;}}
 };
 for(const flow of model.flows){if(flow.source?.id===id)return flow.name+'/processors/0';const found=visit(flow.processors,flow.name);if(found)return found;
  const handled=flow.errorHandler?.component?handlers(flow.errorHandler.component.children,flow.name):visit(flow.errorHandler?.processors??[],flow.name+'/errorHandler/0');if(handled)return handled;
 }
 return undefined;
}
