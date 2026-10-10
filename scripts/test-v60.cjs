module.exports=async function(page,assert,results){
 const shot=async name=>{await page.waitForTimeout(270);await page.screenshot({path:'ui-review/'+name+'.png'})};
 await page.setViewportSize({width:430,height:900});
 await page.evaluate(async()=>{
  test52.state.shared.firstLaunch53=true;document.querySelector('.prank53')?.remove();
  for(const p of [...test59.pages])test59.close(p);test52.hideDialog();test52.discard();
  for(const id of ['profileUI','pluginOverlay','appSettings53'])document.getElementById(id).hidden=true;
  test60.setLanguage('zh-CN');test52.leaveChat();test52.showHome();const c=test52.active();c.cfg.userBlocksAi=false;
  const thread={id:'sms-regression60',characterId:c.roleId||c.id,sessionId53:c.id,number:'xxxxxxxxxxx',label:'保持原名',unknown:false,created:Date.now(),unread:0,messages:Array.from({length:45},(_,i)=>({id:'retained60-'+i,role:i%3?'assistant':'user',content:'已有短信 '+i+'\n这一段足够长，用来检查旧记录阅读位置。'.repeat(3),created:Date.now()-i*200000})).reverse()};
  test52.threads.set(thread.id,thread);await test52.smsSave(thread);window.thread60=thread;test60.smsShowThread(thread.id);
 });
 await page.waitForTimeout(350);
 const stable=await page.evaluate(async()=>{
  const root=document.querySelector('#smsRoot'),history=document.querySelector('#smsHistory'),first=history.querySelector('.sms-line'),face=document.querySelector('#smsContact').firstElementChild,box=root.getBoundingClientRect(),home=document.querySelector('#homeOverview').firstElementChild,samples=[];
  history.scrollTop=0;
  for(let frame=0;frame<22;frame++){
   if(frame%2===0&&frame<16){const m={id:'received60-'+frame,role:'assistant',content:'连续发来 '+frame,created:Date.now()};thread60.messages.push(m);test60.smsIncoming(thread60,m);root.classList.toggle('star-static')}
   await new Promise(requestAnimationFrame);const r=root.getBoundingClientRect();samples.push({opacity:Number(getComputedStyle(root).opacity),x:r.x,width:r.width,hidden:root.hidden,animations:root.getAnimations().filter(a=>a.playState==='running').length});
  }
  return {samples,home:home===document.querySelector('#homeOverview').firstElementChild,identity:first===history.querySelector('.sms-line'),face:face===document.querySelector('#smsContact').firstElementChild,top:history.scrollTop,expected:thread60.messages.length,actual:history.querySelectorAll('.sms-line').length,box:{x:box.x,width:box.width},tail:history.lastElementChild.querySelector('path')?.getAttribute('d')};
 });
 assert.ok(stable.home,'the covered home page must not rebuild for every SMS');assert.ok(stable.identity&&stable.face,'receiving messages must retain old bubble and contact nodes');assert.equal(stable.actual,stable.expected);assert.equal(stable.top,0,'receiving a message must preserve the position when reading older messages');
 assert.ok(stable.samples.every(s=>!s.hidden&&s.opacity>.99&&s.animations===0&&Math.abs(s.x-stable.box.x)<1&&Math.abs(s.width-stable.box.width)<1),'SMS must stay visible without restarting its entrance animation');assert.ok(stable.tail?.includes('C'));
 await shot('v60-sms-stable');
 await page.evaluate(()=>{const h=document.querySelector('#smsHistory');h.scrollTop=h.scrollHeight;const m={id:'follow60',role:'assistant',content:'在底部继续跟随',created:Date.now()};thread60.messages.push(m);test60.smsIncoming(thread60,m)});
 assert.ok(await page.locator('#smsHistory').evaluate(e=>e.scrollHeight-e.clientHeight-e.scrollTop<3));
 await page.evaluate(()=>{thread60.messages.shift();test60.renderSmsThread()});assert.equal(await page.locator('#smsHistory .sms-line').count(),stable.expected);
 await page.evaluate(()=>{test52.configure({url:'https://fixture60.example/v1/chat/completions',model:'fixture60',delay:0});test52.setModel(async()=>'{"actions":[{"type":"say","text":"真实收发路径一"},{"type":"say","text":"真实收发路径二"}]}')});
 await page.locator('#smsInput').fill('检查真实收发路径');await page.locator('#smsForm').evaluate(e=>e.dispatchEvent(new Event('submit',{bubbles:true,cancelable:true})));
 await page.waitForFunction(()=>document.querySelector('#smsHistory').textContent.includes('真实收发路径二'));assert.ok(await page.locator('#smsHistory').evaluate(e=>e.scrollHeight-e.clientHeight-e.scrollTop<3));
 results.push('v60 frame-by-frame SMS visibility, persistent bubbles/avatar, tails, reading position, bottom following and actual send/reply path');

 const centers=await page.evaluate(async()=>{test60.smsEditThread(thread60);const samples=[];for(let i=0;i<19;i++){await new Promise(requestAnimationFrame);const r=document.querySelector('#dialog').getBoundingClientRect();samples.push(r.x+r.width/2)}return samples});
 assert.ok(centers.every(x=>Math.abs(x-215)<1),'contact popup must remain centered throughout scaling');await shot('v60-sms-edit');
 await page.locator('#dialog>.actions').getByRole('button',{name:'取消',exact:true}).click();
 await page.evaluate(()=>{test60.smsHide();test59.popup('检查居中缩放弹窗')});
 const alert=await page.evaluate(async()=>{const a=[];for(let i=0;i<19;i++){await new Promise(requestAnimationFrame);const r=document.querySelector('#dialog').getBoundingClientRect();a.push(r.x+r.width/2)}return {centers:a,animation:getComputedStyle(document.querySelector('#dialog')).animationName}});
 assert.ok(alert.centers.every(x=>Math.abs(x-215)<1));assert.ok(alert.animation.includes('iosAlert59'));await page.evaluate(()=>test52.hideDialog());
 const global=await page.evaluate(async()=>{window.popup60=test60.popup({title:'保存',blocks:[{type:'text',text:'角色自定义正文'}],buttons:[{id:'ok',label:'好的'}]},{name:'保存'});const a=[];for(let i=0;i<19;i++){await new Promise(requestAnimationFrame);const r=document.querySelector('.global-interaction').getBoundingClientRect();a.push(r.x+r.width/2)}return a});assert.ok(global.every(x=>Math.abs(x-215)<1));
 await page.keyboard.press('Escape');assert.equal(await page.locator('.global-interaction-shade').count(),0);
 results.push('v60 contact/ordinary/plugin popups keep their center across every animation frame and close with Escape');

 await page.evaluate(()=>{test52.state.shared.listTheme='dark';test52.openProfile('user')});await shot('v60-dark-profile');
 const profile=await page.locator('#profileUI').evaluate(e=>({scope:e.dataset.scope59,card:getComputedStyle(e).getPropertyValue('--settings-card').trim(),bg:getComputedStyle(e.querySelector('header')).backgroundColor}));assert.notEqual(profile.bg,'rgb(255, 255, 255)');
 await page.locator('#profileMore').click();await page.getByRole('button',{name:'编辑主页',exact:true}).click();assert.ok(await page.locator('[data-background60=profile]').isVisible());assert.equal(await page.locator('#profileUI>header [data-background59]').count(),0);await shot('v60-dark-profile-edit');
 await page.evaluate(()=>{document.querySelector('#profileUI').hidden=true;test60.plugins()});await shot('v60-dark-plugins');assert.ok(await page.locator('#pluginOverlay').evaluate(e=>{const c=getComputedStyle(e).backgroundColor.match(/\d+/g);return c&&c.slice(0,3).every(n=>Number(n)<80)}));
 await page.evaluate(()=>{document.querySelector('#pluginOverlay').hidden=true;test52.beginSettings();test58.category('tools')});
 const gaps=await page.locator('#toolSet>.field.settings-switch58').evaluateAll(rows=>rows.map(r=>{const s=getComputedStyle(r);return {top:parseFloat(s.paddingTop),bottom:parseFloat(s.paddingBottom)}}));assert.ok(gaps.length>3&&gaps.every(r=>r.top<=12&&r.bottom<=12));
 await page.evaluate(()=>test58.category('appearance'));const colors=await page.locator('#settingsShade input[type=color]').evaluateAll(es=>es.filter(e=>e.getClientRects().length).map(e=>({radius:getComputedStyle(e).borderRadius,emoji:e.closest('.color-choice')?.textContent.includes('🎨')})));assert.ok(colors.length>0&&colors.every(x=>x.radius==='50%'&&!x.emoji));await shot('v60-compact-colors');await page.evaluate(()=>test52.discard());
 results.push('v60 dark profile/editor/plugins, background inside edit, compact switches and round color controls');

 for(const [lang,title]of [['en','Settings'],['ja','設定'],['zh-TW','設定']]){
  await page.evaluate(lang=>{const c=test52.active();c.cfg.name='保存';c.cfg.remark='';const own=test52.ownPage();own.name='保存';own.bio='保存';test60.setLanguage(lang);test52.openAppSettings()},lang);
  await page.waitForTimeout(70);assert.equal(await page.locator('#appSettings53>header h2').textContent(),title);await shot('v60-language-'+lang);
  await page.evaluate(()=>{test57.connections();test57.connectionEditor()});await page.waitForTimeout(70);
  const modelLabel=await page.locator('.page57:not([data-closing59])').last().textContent();assert.ok(!modelLabel.includes('获取模型列表')&&!modelLabel.includes('供应商名称'),'newly created connection controls must localize');
  await page.evaluate(()=>{for(const p of [...test59.pages])test59.close(p);document.querySelector('#appSettings53').hidden=true;test52.beginSettings();test58.category('tools')});await page.waitForTimeout(210);
  assert.ok(await page.locator('.settings-subtitle58').textContent().then(t=>t.includes('保存')),'a name that equals UI copy must remain unchanged');
  await page.evaluate(()=>{test52.discard();test52.openProfile('user')});assert.equal(await page.locator('.profile-name>strong').textContent(),'保存');assert.equal(await page.locator('.profile-bio').textContent(),'保存');
  await page.evaluate(()=>{document.querySelector('#profileUI').hidden=true;window.fileAuth60=null;test52.active().cfg.agentPermissions57={files:'ask'};test57.authorize(test52.active(),{type:'createFile',filename:'保存.txt',content:'保存'}).then(v=>window.fileAuth60=v)});
  assert.equal(await page.locator('.authorization-value60>p').last().textContent(),'保存');assert.ok(await page.locator('.global-interaction>header h2').textContent().then(t=>t.includes('保存')));
  assert.equal(await page.locator('.authorization-details60').evaluate(e=>e.open),false);await shot('v60-authorization-'+lang);await page.locator('.interaction-actions>button').first().click();await page.waitForFunction(()=>window.fileAuth60===true);
  await page.evaluate(()=>{test60.smsShowThread(thread60.id);test60.smsEditThread(thread60)});await page.waitForTimeout(80);const smsText=await page.locator('#dialog').textContent();assert.ok(!smsText.includes('清除自定义头像与背景')&&!smsText.includes('模拟号码'),'SMS editor must localize');
  await page.locator('#dialog>.actions>button').first().click();await page.evaluate(()=>test60.smsHide());
 }
 await page.evaluate(()=>{test60.setLanguage('en');test52.openAppSettings()});await page.getByRole('button',{name:/Usage license/}).click();assert.ok(await page.locator('.page57:not([data-closing59])').last().textContent().then(t=>t.includes('modified QasDM versions are prohibited')));await shot('v60-license');
 await page.evaluate(()=>{for(const p of [...test59.pages])test59.close(p);document.querySelector('#appSettings53').hidden=true;test60.setLanguage('zh-CN');test52.openAppSettings()});assert.equal(await page.locator('#appSettings53>header h2').textContent(),'设置');
 results.push('v60 English/Japanese/Traditional Chinese static and newly opened UI, reversible language changes, original names/content, permission preview/approval and license page');

 await page.evaluate(()=>{document.querySelector('#appSettings53').hidden=true;const c=test52.active();c.cfg.storyEffects56=true;c.cfg.storyTextCap59=10;test59.story({text:'保持角色正文',input:false,effects:['textFill'],effectText:'铺满',effectColor:'#ff2378',effectGlow:{color:'#22eeff',intensity:.7},effectDuration:1.6,buttons:[{id:'one',label:'继续'}]})});await page.waitForTimeout(750);
 const fill=await page.locator('.textFill59').evaluate(layer=>{const es=[...layer.children],s=getComputedStyle(layer);return {color:s.color,glow:s.textShadow,sizes:new Set(es.map(e=>e.style.fontSize)).size,angles:new Set(es.map(e=>e.style.transform)).size,x:es.map(e=>parseFloat(e.style.left)),y:es.map(e=>parseFloat(e.style.top))}});assert.equal(fill.color,'rgb(255, 35, 120)');assert.notEqual(fill.glow,'none');assert.ok(fill.sizes>8&&fill.angles>8&&Math.max(...fill.x)-Math.min(...fill.x)>230&&Math.max(...fill.y)-Math.min(...fill.y)>500);await shot('v60-text-fill');
 await page.waitForTimeout(1000);assert.equal(await page.locator('.textFill59').count(),0);await page.evaluate(()=>test52.closeStory());
 await page.evaluate(()=>test59.story({text:'可主动退出',effects:['textFall'],effectColor:'#55ff99',effectDuration:10,buttons:[{id:'one',label:'继续'}]}));await page.waitForTimeout(180);await page.locator('.story51-exit').click();assert.equal(await page.locator('.textFall59').count(),0);
 results.push('v60 random full-screen text positions, sizes/angles, independent effect color/glow, timed cleanup and immediate exit');
};
