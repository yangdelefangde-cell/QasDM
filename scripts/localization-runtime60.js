// Localize UI copy only; names, chat content and user-created work stay intact.
const originalNodes=new WeakMap(),translatedNodes60=new WeakMap(),localeAttributes60=new WeakMap();
const localeRoots60='#regionWarning,#diaryOverlay,#browserOverlay,#roleOverlay,#listenOverlay,#listenIncoming,#smsRoot,#fixedIntro,#inlineIntro,.top,.composer,#settingsShade,#conversationList,#listMenu,#listPlusMenu,#callScreen,#dialogShade,#contextMenu,#pluginOverlay,#selectionHead,#selectionBar,#profileUI,#appSettings53,.page57,.global-interaction-shade,.comments-panel53,.prank53,#toast';
const localeSkip60='textarea,script,style,pre,code,.bubble,.call-line,#status,.bio,.avatar,.top .name,.intro h1,.memory-card,.list-row-name,.list-row-preview,.banner-notice-text,#listenPartner,#listenSong,#listenArtist,#listenLyrics,#listenShuffle,#listenRepeat,#listenTracks,#listenChatLog,#listenCurrentTime,#listenTotalTime,#listenUserName,#listenAiName,#listenIncomingName,#smsContact,#smsNavTitle,.sms-thread,.sms-bubble,.profile-name>strong,.profile-bio,.profile-thought,.profile-handle,.profile-face,.profile-person-face,.profile-person-name,.profile-work-author>strong,.profile-work-caption>h3,.profile-work-caption>p,.profile-manage-row>strong,.sim-work,.sim-work53,.journal-copy,.comment-copy53>p,.comment-copy53>small,.comment-caption53 p,.user-content60,[data-locale60=off],.plugin-settings,.connection-card59 strong,.global-interaction>header small,.global-interaction>header h2';
let localeReverse60;
function localeSource60(text){
 if(Object.hasOwn(english,text)||Object.hasOwn(japanese,text)||Object.hasOwn(traditional,text))return text;
 if(!localeReverse60){localeReverse60=new Map();for(const dict of [english,japanese,traditional])for(const [source,value] of Object.entries(dict))if(source!==value&&!localeReverse60.has(value))localeReverse60.set(value,source)}
 return localeReverse60.get(text)||text;
}
function L(text){
 text=String(text??'');const lang=state.shared.language||'zh-CN';if(lang==='zh-CN')return text;
 const dict=lang==='en'?english:lang==='ja'?japanese:traditional;
 if(Object.hasOwn(dict,text))return dict[text];
 if(text.includes('·')||text.includes('\n'))return text.split(/(\s*·\s*|\n)/).map(t=>/^\s*·\s*$/.test(t)||t==='\n'?t:L(t)).join('');
 const number='([\\d,.]+)',rules=[
  ['^'+number+' 次请求$',n=>n+' '+L('次请求')],
  ['^'+number+' 次有实际用量$',n=>n+' '+L('次有实际用量')],
  ['^'+number+' 个角色使用$',n=>n+' '+L('个角色使用')],
  ['^已隐藏 '+number+' 个$',n=>L('已隐藏')+' '+n],
  ['^输入 '+number+'$',n=>L('输入')+' '+n],['^输出 '+number+'$',n=>L('输出')+' '+n],
  ['^缓存 ('+number+'|未提供)$',n=>L('缓存')+' '+L(n)],
  ['^'+number+' (毫秒|秒|分钟|张图片|张|次|个对话)$',(n,u)=>n+' '+L(u)],
  ['^生图 '+number+' 张$',n=>L('生图')+' '+n+' '+L('张')],
  ['^配置单价估算 (¥[\\d.]+)$',n=>L('配置单价估算 ')+n],
  ['^获取了 '+number+' 个模型；仍可手动填写。$',n=>L('获取了模型前缀')+n+L('获取了模型后缀')],
  ['^下一次上下文估算：约 '+number+' tokens（尚未计入媒体编码及请求时实时附加内容）$',n=>L('下一次上下文估算：约 ')+n+' tokens'+L('（尚未计入媒体编码及请求时实时附加内容）')],
 ];
 for(const [pattern,render]of rules){const m=text.match(new RegExp(pattern));if(m)return render(...m.slice(1))}
 const prefix=Object.keys(dict).filter(k=>k.length>1&&(/[：:]$/.test(k)||/： /.test(k))).sort((a,b)=>b.length-a.length).find(k=>text.startsWith(k));
 if(prefix)return dict[prefix]+L(text.slice(prefix.length));
 if(lang==='zh-TW')return [...text].map(c=>traditionalChars60[c]||c).join('');return text;
}
function localizeSurface(scope){
 document.documentElement.lang=state.shared.language||'zh-CN';
 const roots=scope?[scope]:[...document.querySelectorAll(localeRoots60)].filter((el,i,all)=>!all.some((parent,j)=>i!==j&&parent.contains(el)));
 for(const root of roots){
  const walker=document.createTreeWalker(root,NodeFilter.SHOW_TEXT);let n;
  while(n=walker.nextNode()){
   if(n.parentElement?.closest(localeSkip60))continue;
   const value=n.textContent,trimmed=value.trim();if(!trimmed)continue;
   let original=originalNodes.get(n);
   if(original===undefined||value!==translatedNodes60.get(n)){original=value.replace(trimmed,localeSource60(trimmed));originalNodes.set(n,original)}
   const raw=original.trim(),next=original.replace(raw,L(raw));translatedNodes60.set(n,next);if(value!==next)n.textContent=next;
  }
  for(const el of root.querySelectorAll('[placeholder],[aria-label],[title]')){
   if(el.closest('[data-locale60=off],.plugin-settings'))continue;
   let attrs=localeAttributes60.get(el);if(!attrs){attrs={};localeAttributes60.set(el,attrs)}
   for(const key of ['placeholder','aria-label','title']){
    const value=el.getAttribute(key);if(!value)continue;
    let item=attrs[key];if(!item||item.last!==value)item=attrs[key]={source:localeSource60(value),last:value};
    const next=L(item.source);item.last=next;if(value!==next)el.setAttribute(key,next);
   }
  }
 }
}
const localePending60=new Set();let localeFrame60=0;
const localeObserver60=new MutationObserver(records=>{
 if((state.shared.language||'zh-CN')==='zh-CN')return;
 for(const record of records){
  const el=record.target.nodeType===Node.TEXT_NODE?record.target.parentElement:record.target;
  if(!(el instanceof Element)||!el.closest(localeRoots60)||el.closest(localeSkip60))continue;
  if(record.type==='characterData'&&translatedNodes60.get(record.target)===record.target.textContent)continue;
  localePending60.add(el);
 }
 if(!localePending60.size||localeFrame60)return;
 localeFrame60=requestAnimationFrame(()=>{localeFrame60=0;const roots=[...localePending60];localePending60.clear();for(const el of roots)if(el.isConnected&&!roots.some(p=>p!==el&&p.contains(el)))localizeSurface(el)});
});
localeObserver60.observe(document.body,{subtree:true,childList:true,characterData:true,attributes:true,attributeFilter:['placeholder','aria-label','title']});
