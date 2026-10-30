import { chromium } from 'playwright';
import { execFileSync } from 'node:child_process';
import { fileURLToPath } from 'node:url';

/** Connect only to the independently launched, isolated real VS Code desktop. */
export async function connectHost() {
  const browser = await chromium.connectOverCDP(process.env.ZERNOVA_UI_CDP || 'http://127.0.0.1:9223');
  const context = browser.contexts()[0];
  const page = context.pages().find(p => p.url().startsWith('vscode-file:') && p.url().includes('workbench'));
  if (!page) throw new Error('A real VS Code desktop workbench is required. This is not a standalone browser test.');
  page.setDefaultTimeout(10000);
  return { browser, context, page };
}
export async function command(page, name) {
  await page.keyboard.press('Escape');
  await page.keyboard.press('Control+Shift+P');
  const input = page.locator('.quick-input-widget input');
  await input.waitFor({ state: 'visible' });
  await input.fill('>' + name);
  const escaped = name.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
  const match = page.locator('.quick-input-list').getByRole('option', { name: new RegExp('^' + escaped + '(?:,|$)') }).first();
  await match.waitFor({ state: 'visible', timeout: 10000 });
  await match.click();
  await page.waitForTimeout(250);
}
export async function webviewFrame(page, selector = 'main.studio') {
  for(let attempt=0;attempt<50;attempt++) {
    for(const frame of page.frames()) {
      if(frame===page.mainFrame()) continue;
      try {
        if (!(await frame.locator(selector).isVisible())) continue;
        let ancestor = frame, visible = true;
        while (ancestor.parentFrame()) {
          if (!(await (await ancestor.frameElement()).isVisible())) { visible = false; break; }
          ancestor = ancestor.parentFrame();
        }
        if (visible) return frame;
      } catch {}
    }
    await page.waitForTimeout(100);
  }
  throw new Error('The installed ZERNOVA webview did not initialize.');
}
export async function resizeHost(page, width, height) {
  execFileSync('python3',[fileURLToPath(new URL('./resize-host.py',import.meta.url)),String(width),String(height)],{env:{...process.env,DISPLAY:process.env.DISPLAY||':99'}});
  await page.waitForTimeout(300);
  const actual = await page.evaluate(() => ({ width: innerWidth, height: innerHeight }));
  if(actual.width!==width || actual.height!==height) throw new Error(`Native window resize was ${actual.width}×${actual.height}, requested ${width}×${height}`);
  return actual;
}
export async function hostTheme(page, name) {
  await command(page, 'Preferences: Color Theme');
  const input=page.locator('.quick-input-widget input');
  await input.waitFor({state:'visible'});
  await input.fill(name);
  await page.waitForTimeout(150);
  const match=page.locator('.quick-input-list .monaco-list-row').filter({hasText:name}).first();
  await match.click();
  await page.waitForTimeout(200);
}
export async function scenario(frame, id) {
  await frame.getByRole('combobox',{name:'Preview scenario',exact:true}).selectOption(id);
}
