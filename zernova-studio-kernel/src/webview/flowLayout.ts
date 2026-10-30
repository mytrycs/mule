/* Copyright (c) 2026 ZerNova. See LICENSE.txt. */
export interface Anchor { x: number; y: number; }
export function responseGeometry(last: Anchor, source: Anchor, bottom: number): string {
  const baseline = Math.max(bottom, last.y + 16, source.y + 16);
  return `M ${last.x} ${last.y} V ${baseline} H ${source.x} V ${source.y} M ${source.x - 4} ${source.y + 5} L ${source.x} ${source.y} L ${source.x + 4} ${source.y + 5}`;
}
export function requestGeometry(source: Anchor, target: Anchor): string {
  return `M ${source.x} ${source.y} L ${target.x} ${target.y} M ${target.x - 5} ${target.y - 4} L ${target.x} ${target.y} L ${target.x - 5} ${target.y + 4}`;
}
/** Route through the measured port centers, including zoom, never the card edge. */
export function layoutFlows(root: HTMLElement): void {
  for (const lane of root.querySelectorAll<HTMLElement>('.flow-lanes')) {
    const source = lane.querySelector<HTMLElement>('.source-lane .node');
    const processors = lane.querySelectorAll<HTMLElement>('.processor-lane > [data-processor-index] .node');
    const response = lane.querySelector<SVGPathElement>('.response-path path');
    const request = lane.querySelector<SVGPathElement>('.request-path path');
    if (!source || !processors.length || !response) continue;
    const bounds = lane.getBoundingClientRect();
    if (!bounds.width || !bounds.height) continue;
    const scaleX = lane.offsetWidth / bounds.width, scaleY = lane.offsetHeight / bounds.height;
    const point = (node: HTMLElement, center = false): Anchor => {
      const rect = node.getBoundingClientRect();
      return { x: (rect.left + rect.width / 2 - bounds.left) * scaleX, y: ((center ? rect.top + rect.height / 2 : rect.bottom) - bounds.top) * scaleY };
    };
    const port = (node: HTMLElement, selector: string): Anchor => { const p=node.querySelector<HTMLElement>(selector); return point(p ?? node,!!p); };
    response.setAttribute('d',responseGeometry(port(processors[processors.length-1],'[data-anchor="response-origin"]'),port(source,'[data-anchor="response"]'),lane.offsetHeight-22));
    if (request) {
      const first=processors[0], target=point(first.querySelector<HTMLElement>('.node-symbol') ?? first,true);
      target.x=(first.getBoundingClientRect().left-bounds.left)*scaleX;
      request.setAttribute('d',requestGeometry(port(source,'[data-anchor="request"]'),target));
    }
  }
}
