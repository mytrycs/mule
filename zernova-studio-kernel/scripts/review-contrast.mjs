/** Review axe's partially-obscured color items using actual computed styles,
 * hit-testing and scroll reachability. This is not a screen-reader test. */
import { writeFile } from 'node:fs/promises';
import assert from 'node:assert/strict';
import { connectHost, webviewFrame, hostTheme, scenario } from './ui-utils.mjs';
const {browser,page}=await connectHost();const reviews=[];
try {
  for(const theme of ['ZERNOVA Studio Light','ZERNOVA Studio Dark','Dark High Contrast','Light High Contrast']){
    await hostTheme(page,theme);const frame=await webviewFrame(page);
    for(const view of ['logger','xml']){
      await scenario(frame,view);
      if(view==='xml')await frame.locator('.code-line').last().scrollIntoViewIfNeeded();
      else await frame.locator('#logger-message').scrollIntoViewIfNeeded();
      const values=await frame.evaluate(view=>{
        const rgb=s=>(s.match(/[\d.]+/g)||[]).map(Number);
        const luminance=a=>a.slice(0,3).map(v=>v/255).map(v=>v<=.04045?v/12.92:Math.pow((v+.055)/1.055,2.4)).reduce((n,v,i)=>n+v*[.2126,.7152,.0722][i],0);
        const styles=view==='logger'?[document.querySelector('#logger-message')]:['.line-number','.syn-tag','.syn-attribute','.syn-string','.line-source'].map(s=>document.querySelector(s));
        return styles.map(el=>{
          let ancestor=el,bg='';
          while(ancestor){const v=getComputedStyle(ancestor).backgroundColor;const parts=rgb(v);if(parts.length===3||parts[3]===1){bg=v;break;}ancestor=ancestor.parentElement;}
          const fg=getComputedStyle(el).color;const [a,b]=[luminance(rgb(fg)),luminance(rgb(bg))].sort((x,y)=>y-x);const ratio=(a+.05)/(b+.05);
          return {selector:el.id?'#'+el.id:'.'+el.classList[0],foreground:fg,background:bg,contrastRatio:Number(ratio.toFixed(3)),meetsSmallTextAA:ratio>=4.5};
        });
      },view);
      values.forEach(v=>assert(v.meetsSmallTextAA,JSON.stringify({theme,view,...v})));
      const reachability=await frame.evaluate(view=>{
        const el=view==='logger'?document.querySelector('#logger-message'):document.querySelector('.code-line:last-child .line-source');
        const r=el.getBoundingClientRect();const x=Math.max(0,Math.min(innerWidth-1,r.left+Math.min(30,r.width/2))),y=r.top+r.height/2;
        const hit=document.elementFromPoint(x,y);
        return {withinViewport:y>=0&&y<innerHeight,hitBelongsToContent:!!hit&&(hit===el||el.contains(hit)),text:el.value||el.textContent};
      },view);
      assert(reachability.withinViewport);assert(reachability.hitBelongsToContent);
      reviews.push({theme,view,status:'PASS',values,reachability});
    }
  }
  await hostTheme(page,'ZERNOVA Studio Light');const frame=await webviewFrame(page);await scenario(frame,'workbench');
  await writeFile('evidence/accessibility-contrast-review.json',JSON.stringify({completedAt:new Date().toISOString(),status:'PASS',statesReviewed:reviews.length,method:'Actual computed colors with WCAG contrast formula, real scrollIntoView and DOM hit test in installed VS Code webview',limitations:'Does not remove recorded axe incomplete entries and is not a screen-reader or full accessibility certification.',reviews},null,2));
  console.log('Contrast and reachability reviews PASS',reviews.length);
}finally{await browser.close();}
