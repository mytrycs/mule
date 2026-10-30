/** Shared by all Studio dialogs, including host-created project/run wizards. */
export function installDialogDragging(root: HTMLElement): void {
  const positions = new Map<string, { x: number; y: number }>();
  let moving: { dialog: HTMLElement; title: HTMLElement; key: string; x: number; y: number; startX: number; startY: number } | undefined;
  const key = (dialog: HTMLElement) => dialog.getAttribute('aria-labelledby') || dialog.getAttribute('aria-label') || 'dialog';
  const constrain = (dialog: HTMLElement, x: number, y: number) => {
    const old = positions.get(key(dialog)) ?? { x: 0, y: 0 }, rect = dialog.getBoundingClientRect();
    const left = rect.left - old.x, top = rect.top - old.y;
    return { x: Math.max(8 - left, Math.min(window.innerWidth - Math.min(rect.width, window.innerWidth - 16) - left - 8, x)), y: Math.max(8 - top, Math.min(window.innerHeight - 48 - top, y)) };
  };
  const apply = () => {
    for (const dialog of root.querySelectorAll<HTMLElement>('.dialog')) {
      const title = dialog.querySelector<HTMLElement>('.dialog-title');
      if (title) { title.tabIndex = 0; title.title = 'Drag to move; Alt + arrow keys also move this dialog'; }
      const p = positions.get(key(dialog));
      if (p) dialog.style.transform = `translate(${p.x}px, ${p.y}px)`;
    }
  };
  new MutationObserver(apply).observe(root, { childList: true, subtree: true });
  root.addEventListener('pointerdown', event => {
    const target = event.target as HTMLElement, title = target.closest<HTMLElement>('.dialog-title');
    if (!title || target.closest('button,input,select,textarea') || event.button !== 0) return;
    const dialog = title.closest<HTMLElement>('.dialog'); if (!dialog) return;
    event.preventDefault(); const p = positions.get(key(dialog)) ?? { x: 0, y: 0 };
    moving = { dialog, title, key: key(dialog), ...p, startX: event.clientX, startY: event.clientY };
    title.setPointerCapture(event.pointerId); title.style.cursor = 'grabbing';
  });
  root.addEventListener('pointermove', event => {
    if (!moving) return;
    const p = constrain(moving.dialog, moving.x + event.clientX - moving.startX, moving.y + event.clientY - moving.startY);
    positions.set(moving.key, p); moving.dialog.style.transform = `translate(${p.x}px, ${p.y}px)`;
  });
  const finish = () => { if (moving) moving.title.style.cursor = ''; moving = undefined; };
  root.addEventListener('pointerup', finish); root.addEventListener('pointercancel', finish);
  root.addEventListener('keydown', event => {
    const title = (event.target as HTMLElement).closest<HTMLElement>('.dialog-title');
    if (!title || !event.altKey || !event.key.startsWith('Arrow')) return;
    const dialog = title.closest<HTMLElement>('.dialog')!; const p = positions.get(key(dialog)) ?? { x: 0, y: 0 };
    event.preventDefault(); const next = constrain(dialog, p.x + (event.key === 'ArrowRight' ? 20 : event.key === 'ArrowLeft' ? -20 : 0), p.y + (event.key === 'ArrowDown' ? 20 : event.key === 'ArrowUp' ? -20 : 0));
    positions.set(key(dialog), next); apply();
  });
  window.addEventListener('resize', () => { for (const dialog of root.querySelectorAll<HTMLElement>('.dialog')) { const p = positions.get(key(dialog)); if (p) positions.set(key(dialog), constrain(dialog, p.x, p.y)); } apply(); });
  apply();
}
