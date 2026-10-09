(()=>{
'use strict';if(!window.QasNative)return;
window.QasAndroid={versionCode:570001,versionName:'57.0-test.1'};
const pending=new Map();let seq=0,queue=Promise.resolve();
QasNative.onmessage=e=>{let r;try{r=JSON.parse(e.data)}catch{return}const p=pending.get(r.id);if(!p)return;pending.delete(r.id);clearTimeout(p.timer);r.error?p.reject(Error(r.error)):p.resolve(r)};
function send(action,data={}){return new Promise((resolve,reject)=>{const id=++seq;const timer=setTimeout(()=>{pending.delete(id);reject(Error('操作超时'))},180000);pending.set(id,{resolve,reject,timer});QasNative.postMessage(JSON.stringify({id,action,...data}))})}
function base64(blob){return new Promise((resolve,reject)=>{const reader=new FileReader();reader.onload=()=>resolve(String(reader.result).split(',')[1]);reader.onerror=()=>reject(reader.error);reader.readAsDataURL(blob)})}
window.qasNativeSend=send;
window.qasAndroidExport=(blob,name)=>{const task=queue.catch(()=>{}).then(async()=>{if(blob.size>128*1024*1024)throw Error('单次导出上限为 128 MB');await send('exportStart',{name:String(name||'QasDM-backup.json'),mime:blob.type||'application/octet-stream'});try{for(let offset=0;offset<blob.size;offset+=192*1024)await send('exportChunk',{data:await base64(blob.slice(offset,offset+192*1024))});return await send('exportFinish')}catch(e){await send('exportCancel').catch(()=>{});throw e}});queue=task;return task};
// Native clipboard remains scoped to the application's main frame.
try{Object.defineProperty(navigator,'clipboard',{configurable:true,value:{writeText:text=>send('clipboard',{text:String(text)})}})}catch{}
})();

