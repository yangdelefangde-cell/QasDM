(function(root){
'use strict';
const CURRENT_VERSION='52-1';
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
async function check(fetcher,installed){const controller=new AbortController(),timer=setTimeout(()=>controller.abort(),12000);try{const response=await fetcher(VERSION_URL+'?t='+Date.now(),{cache:'no-store',signal:controller.signal});if(!response.ok)throw Error('版本文件 HTTP '+response.status);return resolve(await response.json(),installed)}finally{clearTimeout(timer)}}
const api={CURRENT_VERSION,VERSION_URL,compare,resolve,check};if(typeof module!=='undefined'&&module.exports)module.exports=api;else root.QasUpdates=api;
})(typeof window!=='undefined'?window:globalThis);
