/* Copyright (c) 2026 ZerNova. See LICENSE.txt. */
export interface Anchor { x: number; y: number; }
export function responseGeometry(last: Anchor, source: Anchor, bottom: number): string {
  const baseline = Math.max(bottom, last.y + 16, source.y + 16);
  return `M ${last.x} ${last.y} V ${baseline} H ${source.x} V ${source.y + 5} M ${source.x - 4} ${source.y + 10} L ${source.x} ${source.y + 5} L ${source.x + 4} ${source.y + 10}`;
}
/** Measure actual rendered symbols, including zoom; never infer an endpoint
 * from the card's edge or a fixed processor count. */
export function layoutFlows(root: HTMLElement): void {
  for (const lane of root.querySelectorAll<HTMLElement>('.flow-lanes')) {
    const source = lane.querySelector<HTMLElement>('.source-lane .node-symbol');
    const processors = lane.querySelectorAll<HTMLElement>('.processor-lane > [data-processor-index] .node-symbol');
    const path = lane.querySelector<SVGPathElement>('.response-path path');
    if (!source || !processors.length || !path) continue;
    const bounds = lane.getBoundingClientRect();
    if (!bounds.width || !bounds.height) continue;
    const scaleX = lane.offsetWidth / bounds.width, scaleY = lane.offsetHeight / bounds.height;
    const anchor = (node: HTMLElement): Anchor => {
      const rect = node.getBoundingClientRect();
      return { x: (rect.left + rect.width / 2 - bounds.left) * scaleX, y: (rect.bottom - bounds.top) * scaleY };
    };
    path.setAttribute('d', responseGeometry(anchor(processors[processors.length - 1]), anchor(source), lane.offsetHeight - 22));
  }
}
