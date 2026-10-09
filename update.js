(function(root){
'use strict';
const CURRENT_VERSION='56';
const VERSION_URL='https://raw.githubusercontent.com/yangdelefangde-cell/QasDM/main/version.json';
function parts(value){const text=String(value||'').trim();if(!/^v?\d+(?:[-.]\d+)*$/i.test(text))throw Error('版本号格式无效');return text.replace(/^v/i,'').split(/[-.]/).map(Number)}
function compare(a,b){const x=parts(a),y=parts(b);for(let i=0;i<Math.max(x.length,y.length);i++){const n=(x[i]||0)-(y[i]||0);if(n)return Math.sign(n)}return 0}
function resolve(manifest,installed){
 if(!manifest||typeof manifest!=='object')throw Error('版本文件不是有效对象');
 parts(manifest.version);
 const android=installed&&Number.isInteger(installed.versionCode);
 if(android&&(!manifest.android||!Number.isInteger(manifest.android.versionCode)||manifest.android.versionCode<1))throw Error('版本文件缺少安卓版本信息');
 const cmp=android?Math.sign(manifest.android.versionCode-installed.versionCode):compare(manifest.version,CURRENT_VERSION);
 const downloadUrl=android?manifest.android.downloadUrl:manifest.downloadUrl;
 if(cmp>0){let url;try{url=new URL(downloadUrl)}catch{throw Error('新版本缺少有效下载地址')}if(url.protocol!=='https:')throw Error('下载地址必须使用 HTTPS')}
 return {comparison:cmp,latest:android?manifest.android.versionName:manifest.version,current:android?installed.versionName:CURRENT_VERSION,downloadUrl,notes:String(manifest.notes||'')};
}
const VERSION_SOURCES=[VERSION_URL,'https://api.github.com/repos/yangdelefangde-cell/QasDM/contents/version.json?ref=main'];
async function check(fetcher,installed){let last;for(const source of VERSION_SOURCES){const controller=new AbortController(),timer=setTimeout(()=>controller.abort(),6500);try{const response=await fetcher(source+(source.includes('?')?'&':'?')+'t='+Date.now(),{cache:'no-store',signal:controller.signal,headers:{Accept:'application/vnd.github.raw+json'}});if(!response.ok)throw Error('版本文件 HTTP '+response.status);let data=await response.json();if(data.content&&data.encoding==='base64'){const decode=typeof atob==='function'?data=>new TextDecoder().decode(Uint8Array.from(atob(data),c=>c.charCodeAt(0))):data=>Buffer.from(data,'base64').toString('utf8');data=JSON.parse(decode(data.content.replace(/\s/g,'')))}return resolve(data,installed)}catch(error){last=error}finally{clearTimeout(timer)}}throw last||Error('暂时无法连接更新服务，请稍后重试')}

const api={CURRENT_VERSION,VERSION_URL,compare,resolve,check};if(typeof module!=='undefined'&&module.exports)module.exports=api;else root.QasUpdates=api;
})(typeof window!=='undefined'?window:globalThis);

