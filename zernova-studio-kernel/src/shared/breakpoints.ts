import type { IntegrationModel } from './xml';
/** Listener breakpoints stop at flow entry, after acceptance and before processor 0. */
export function componentLocation(model: IntegrationModel, id: string): string | undefined {
    for (const flow of model.flows) {
        if (flow.source?.id === id)
            return flow.name + '/processors/0';
        for (const [index, node] of flow.processors.entries())
            if (node.id === id)
                return flow.name + '/processors/' + index;
        for (const [index, node] of (flow.errorHandler?.processors ?? []).entries())
            if (node.id === id)
                return flow.name + '/errorHandler/0/processors/' + index;
    }
    return undefined;
}
