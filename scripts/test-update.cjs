const assert=require('node:assert/strict'),fs=require('node:fs');const u=require('../update.js'),v=require('../version.json');
assert.equal(u.compare('V46','51-3'),-1);assert.equal(u.compare('51-3','51-3'),0);assert.equal(u.compare('51-10','51-3'),1);assert.equal(u.compare('51.3.0','51-3'),0);assert.throws(()=>u.compare('garbage','51-3'));
assert.equal(u.resolve(v).comparison,0);assert.equal(u.resolve({...v,version:'53'}).comparison,1);
assert.equal(u.resolve(v,{versionCode:v.android.versionCode,versionName:v.android.versionName}).comparison,0);
assert.equal(u.resolve(v,{versionCode:v.android.versionCode-1,versionName:'older'}).downloadUrl,v.android.downloadUrl);
assert.throws(()=>u.resolve({...v,version:'53',downloadUrl:'javascript:alert(1)'}));assert.throws(()=>u.resolve({...v,android:null},{versionCode:1}));
assert.match(fs.readFileSync('index.html','utf8'),/<title>QasDM v52-1<\/title>/);assert.equal(v.version,u.CURRENT_VERSION);assert.match(fs.readFileSync('android-runtime.js','utf8'),new RegExp('versionCode:'+v.android.versionCode));
(async()=>{assert.equal((await u.check(async()=>({ok:true,json:async()=>v}))).comparison,0);await assert.rejects(u.check(async()=>({ok:false,status:503})),/503/);await assert.rejects(u.check(async()=>({ok:true,json:async()=>{throw Error('Invalid JSON')}})),/Invalid JSON/);console.log('PASS version comparison, platform downloads, validation, HTTP and JSON errors, packaged version consistency')})().catch(e=>{console.error(e);process.exitCode=1});
