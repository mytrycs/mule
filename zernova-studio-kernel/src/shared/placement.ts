/** Insertion boundaries are inferred from the visible component centers. */
export function insertionAt(centers: number[], x: number): number {
  const before = centers.findIndex(center => x < center);
  return before < 0 ? centers.length : before;
}
/** Remove the moving item before converting an insertion boundary to its index. */
export function moveToInsertion(from: number, insertion: number, count: number): number {
  return Math.max(0, Math.min(count - 1, insertion - (from < insertion ? 1 : 0)));
}
