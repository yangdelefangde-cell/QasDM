package app.qasdm.test;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.provider.Settings;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.webkit.*;
import android.widget.Toast;
import androidx.webkit.WebViewAssetLoader;
import androidx.webkit.WebViewCompat;
import androidx.webkit.WebViewFeature;
import androidx.webkit.JavaScriptReplyProxy;
import org.json.JSONObject;
import java.io.*;
import java.util.Collections;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {
    private static final String ORIGIN = "https://appassets.androidplatform.net";
    private static final int PICK_FILE = 10, SAVE_FILE = 11, MICROPHONE = 12;
    private WebView web;
    private Intent pendingFileIntent57;
    private static WebView runtimeWeb;
    private long lastBack;
    private File musicFile;
    private FileOutputStream musicStream;
    private long musicBytes;
    private String pendingConversation="",pendingThread="";
    static void emit(String js){if(runtimeWeb!=null)runtimeWeb.post(()->runtimeWeb.evaluateJavascript(js,null));}
    static Bitmap decodeImage(String data){try{if(!data.startsWith("data:image/")||data.length()>400000)return null;byte[] raw=Base64.decode(data.substring(data.indexOf(",")+1),Base64.DEFAULT);return BitmapFactory.decodeByteArray(raw,0,raw.length);}catch(Exception e){return null;}}
    private ValueCallback<Uri[]> fileCallback;
    private PermissionRequest microphoneRequest;
    private File exportFile;
    private FileOutputStream exportStream;
    private String exportName, exportMime;
    private long exported;
    private int finishId;
    private JavaScriptReplyProxy finishReply;
    private final ExecutorService files = Executors.newSingleThreadExecutor();
    private final ExecutorService formulas = Executors.newSingleThreadExecutor();

    private void emitViewport(){
        if(web==null||web.getHeight()<=0)return;
        int width=web.getWidth(),height=web.getHeight();
        web.evaluateJavascript("window.qasViewport54&&window.qasViewport54("+width+"/(window.devicePixelRatio||1),"+height+"/(window.devicePixelRatio||1))",null);
    }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(0xff24283d);
        getWindow().setNavigationBarColor(0xff24283d);
        web = new WebView(this);runtimeWeb=web;
        NotificationManager nm=getSystemService(NotificationManager.class);nm.createNotificationChannel(new NotificationChannel("qas-messages","角色消息",NotificationManager.IMPORTANCE_DEFAULT));NotificationChannel silent=new NotificationChannel("qas-silent","角色消息（静音）",NotificationManager.IMPORTANCE_DEFAULT);silent.setSound(null,null);silent.enableVibration(false);nm.createNotificationChannel(silent);
        setContentView(web);
        getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        web.addOnLayoutChangeListener((v,l,t,r,b,ol,ot,or,ob)->{if(r-l!=or-ol||b-t!=ob-ot)emitViewport();});
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(false);
        s.setAllowContentAccess(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        s.setSupportMultipleWindows(true);
        WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG);
        final WebViewAssetLoader assets = new WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this)).build();
        web.setWebViewClient(new WebViewClient() {
            @Override public void onPageFinished(WebView v,String url){deliverNotification();deliverSharedFile57();emitViewport();}
            @Override public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest req) {
                return assets.shouldInterceptRequest(req.getUrl());
            }
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest req) {
                if (!req.isForMainFrame()) return false;
                if (req.getUrl().toString().startsWith(ORIGIN + "/assets/www/")) return false;
                openExternal(req.getUrl());
                return true;
            }
        });
        web.setWebChromeClient(new WebChromeClient() {
            @Override public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback, FileChooserParams params) {
                if (fileCallback != null) fileCallback.onReceiveValue(null);
                fileCallback = callback;
                final String[] types = acceptedMimeTypes(params.getAcceptTypes());
                final boolean multiple = params.getMode() == FileChooserParams.MODE_OPEN_MULTIPLE;
                new AlertDialog.Builder(MainActivity.this).setTitle("选择文件来源")
                    .setItems(new String[]{"系统文件浏览器", "其他应用（文件管理器／相册）"}, (dialog, which) -> {
                        Intent pick = filePickerIntent(which == 0 ? Intent.ACTION_OPEN_DOCUMENT : Intent.ACTION_GET_CONTENT, types, multiple);
                        try { startActivityForResult(which == 0 ? pick : Intent.createChooser(pick, "选择提供文件的应用"), PICK_FILE); }
                        catch (Exception error) {
                            try { startActivityForResult(filePickerIntent(Intent.ACTION_GET_CONTENT, types, multiple), PICK_FILE); }
                            catch (Exception missing) { finishFileChoice(null); toast("没有可用的文件选择应用"); }
                        }
                    }).setOnCancelListener(dialog -> finishFileChoice(null)).show();
                return true;
            }
            @Override public void onPermissionRequest(PermissionRequest request) {
                runOnUiThread(() -> {
                    if (!trustedOrigin(request.getOrigin()) || !java.util.Arrays.asList(request.getResources()).contains(PermissionRequest.RESOURCE_AUDIO_CAPTURE)) {
                        request.deny(); return;
                    }
                    if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                        request.grant(new String[]{PermissionRequest.RESOURCE_AUDIO_CAPTURE});
                    } else {
                        if (microphoneRequest != null) microphoneRequest.deny();
                        microphoneRequest = request;
                        requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, MICROPHONE);
                    }
                });
            }
            @Override public void onPermissionRequestCanceled(PermissionRequest request) {
                if (microphoneRequest == request) microphoneRequest = null;
            }
            @Override public boolean onCreateWindow(WebView view, boolean dialog, boolean gesture, android.os.Message result) {
                if (!gesture) return false;
                WebView popup = new WebView(MainActivity.this);
                popup.setWebViewClient(new WebViewClient() {
                    @Override public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest req) {
                        openExternal(req.getUrl()); v.destroy(); return true;
                    }
                });
                ((WebView.WebViewTransport) result.obj).setWebView(popup);
                result.sendToTarget(); return true;
            }
        });
        if (WebViewFeature.isFeatureSupported(WebViewFeature.WEB_MESSAGE_LISTENER)) {
            WebViewCompat.addWebMessageListener(web, "QasNative", Collections.singleton(ORIGIN),
                (view, message, sourceOrigin, isMainFrame, reply) -> {
                    if (!isMainFrame || !trustedOrigin(sourceOrigin)) return;
                    try { handleMessage(new JSONObject(message.getData()), reply); }
                    catch (Exception e) { toast("本机操作失败：" + e.getMessage()); }
                });
        } else {
            new AlertDialog.Builder(this).setMessage("请先更新 Android System WebView，以支持文件导出和复制。")
                .setPositiveButton("知道了", null).show();
        }
        web.setDownloadListener((url, agent, disposition, mime, size) -> {
            if (url.startsWith("https://") || url.startsWith("http://")) openExternal(Uri.parse(url));
            else toast("请使用应用内的导出按钮保存文件");
        });
        pendingConversation=getIntent().getStringExtra("conversationId");pendingThread=getIntent().getStringExtra("threadId");
        pendingFileIntent57=getIntent();
        web.loadUrl(ORIGIN + "/assets/www/index.html");
    }

    private void handleMessage(JSONObject m, JavaScriptReplyProxy reply) throws Exception {
        int id = m.getInt("id");
        try {
            switch (m.getString("action")) {
                case "latexRender":
                    formulas.execute(() -> {
                        try { JSONObject result = QasLatex.render(m); result.put("id", id); runOnUiThread(() -> reply.postMessage(result.toString())); }
                        catch (Exception error) { runOnUiThread(() -> ack(reply, id, error.getMessage() == null ? "公式无法渲染" : error.getMessage())); }
                    }); break;
                case "settings":
                    JSONObject settings=new JSONObject();settings.put("id",id);settings.put("background",getSharedPreferences("qas-native",0).getBoolean("background",false));if(getSharedPreferences("qas-native",0).getBoolean("background",false))startForegroundService(new Intent(this,QasService.class).setAction("roles").putExtra("enabled",true));reply.postMessage(settings.toString());break;
                case "notificationPermission":
                    if(Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},53);
                    else if(!getSystemService(NotificationManager.class).areNotificationsEnabled())startActivity(new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE,getPackageName()));
                    ack(reply,id,null);break;
                case "batterySettings":
                    try{startActivity(new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS));}catch(Exception e){startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:"+getPackageName())));}ack(reply,id,null);break;
                case "background":
                    startForegroundService(new Intent(this,QasService.class).setAction("roles").putExtra("enabled",m.optBoolean("enabled")));ack(reply,id,null);break;
                case "searchRequest":
                    final String searchUrl=m.getString("url"),searchKey=m.getString("key"),searchBody=m.getString("body");
                    if(!searchUrl.startsWith("https://")||searchBody.length()>20000)throw new IOException("搜索请求无效");
                    files.execute(()->{try{java.net.HttpURLConnection connection=(java.net.HttpURLConnection)new java.net.URL(searchUrl).openConnection();connection.setConnectTimeout(8000);connection.setReadTimeout(20000);connection.setInstanceFollowRedirects(false);connection.setRequestMethod("POST");connection.setDoOutput(true);connection.setRequestProperty("Content-Type","application/json");connection.setRequestProperty("Authorization","Bearer "+searchKey);try(OutputStream output=connection.getOutputStream()){output.write(searchBody.getBytes(java.nio.charset.StandardCharsets.UTF_8));}int status=connection.getResponseCode();if(status<200||status>=300)throw new IOException("搜索服务 HTTP "+status);ByteArrayOutputStream output=new ByteArrayOutputStream();try(InputStream input=connection.getInputStream()){byte[] buffer=new byte[8192];int n;while((n=input.read(buffer))!=-1){if(output.size()+n>2097152)throw new IOException("搜索结果过大");output.write(buffer,0,n);}}connection.disconnect();JSONObject result=new JSONObject();result.put("id",id);result.put("body",output.toString("UTF-8"));runOnUiThread(()->reply.postMessage(result.toString()));}catch(Exception error){runOnUiThread(()->ack(reply,id,error.getMessage()));}});break;
                case "vibrate":
                    android.os.Vibrator vibrator=getSystemService(android.os.Vibrator.class);
                    if(vibrator!=null&&vibrator.hasVibrator())vibrator.vibrate(android.os.VibrationEffect.createOneShot(Math.max(20,Math.min(200,m.optInt("duration",100))),android.os.VibrationEffect.DEFAULT_AMPLITUDE));
                    ack(reply,id,null);break;
                case "notify":
                    notifyRole(m);ack(reply,id,null);break;
                case "musicStart":
                    if(musicStream!=null)musicStream.close();if(musicFile!=null)musicFile.delete();musicFile=File.createTempFile("qas-music-",".audio",getCacheDir());musicStream=new FileOutputStream(musicFile);musicBytes=0;ack(reply,id,null);break;
                case "musicChunk":
                    if(musicStream==null)throw new IOException("没有正在载入的音乐");String chunk=m.getString("data");if(chunk.length()>300000)throw new IOException("音乐分块过大");byte[] audio=Base64.decode(chunk,Base64.DEFAULT);musicBytes+=audio.length;if(musicBytes>128L*1024*1024)throw new IOException("音乐上限为128 MB");musicStream.write(audio);ack(reply,id,null);break;
                case "musicLoad":
                    if(musicStream==null)throw new IOException("没有正在载入的音乐");musicStream.close();musicStream=null;File track=musicFile;musicFile=null;startForegroundService(new Intent(this,QasService.class).setAction("media"));
                    new android.os.Handler().postDelayed(()->{try{if(QasService.current==null)throw new IOException("播放器启动失败");QasService.current.load(track,m);ack(reply,id,null);}catch(Exception e){track.delete();ack(reply,id,e.getMessage());}},100);break;
                case "musicControl":
                    if(QasService.current!=null){String command=m.optString("command");if("stop".equals(command))QasService.current.stopMusic();else QasService.current.command(command,m.optLong("position"));}ack(reply,id,null);break;
                case "clipboard":
                    ((ClipboardManager)getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("QasDM", m.getString("text")));
                    ack(reply, id, null); break;
                case "exportStart":
                    if (exportFile != null) throw new IOException("另一个文件正在保存");
                    exportName = m.optString("name", "QasDM-backup.json").replaceAll("[\\\\/:*?\"<>|]", "_");
                    if (exportName.length() > 120) exportName = exportName.substring(0,120);
                    exportMime = m.optString("mime", "application/octet-stream");
                    exportFile = File.createTempFile("qasdm-export-", ".tmp", getCacheDir());
                    exportStream = new FileOutputStream(exportFile);
                    exported = 0;
                    ack(reply, id, null); break;
                case "exportChunk":
                    if (exportStream == null) throw new IOException("没有正在导出的文件");
                    String data = m.getString("data");
                    if (data.length() > 300000) throw new IOException("数据分块过大");
                    byte[] bytes = Base64.decode(data, Base64.DEFAULT);
                    if (exported + bytes.length > 128L * 1024 * 1024) throw new IOException("单次导出上限为 128 MB");
                    exportStream.write(bytes); exported += bytes.length; ack(reply, id, null); break;
                case "exportFinish":
                    if (exportStream == null) throw new IOException("没有正在导出的文件");
                    exportStream.close(); exportStream = null;
                    finishReply = reply; finishId = id;
                    Intent save = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                    save.addCategory(Intent.CATEGORY_OPENABLE);
                    save.setType(exportMime.isEmpty() ? "application/octet-stream" : exportMime);
                    save.putExtra(Intent.EXTRA_TITLE, exportName);
                    startActivityForResult(save, SAVE_FILE); break;
                case "exportCancel":
                    clearExport(); ack(reply, id, null); break;
                default: throw new IOException("未知操作");
            }
        } catch (Exception e) { ack(reply, id, e.getMessage()); }
    }

    private void notifyRole(JSONObject m) throws IOException {
        NotificationManager nm=getSystemService(NotificationManager.class);if(!nm.areNotificationsEnabled())throw new IOException("系统通知未开启");
        String channel=m.optBoolean("silent",true)?"qas-silent":"qas-messages";
        NotificationChannel settings=nm.getNotificationChannel(channel);if(settings!=null&&settings.getImportance()==NotificationManager.IMPORTANCE_NONE)throw new IOException("角色消息通知渠道已关闭");
        String cid=m.optString("conversationId"),tid=m.optString("threadId"),name=m.optString("name","角色");
        Intent intent=new Intent(this,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP).putExtra("conversationId",cid).putExtra("threadId",tid);
        PendingIntent target=PendingIntent.getActivity(this,(cid+tid).hashCode(),intent,PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT);
        Bitmap avatar=decodeImage(m.optString("avatar"));
        
        Notification.Builder b=new Notification.Builder(this,channel).setSmallIcon(R.drawable.ic_qasdm).setContentTitle(name).setContentText(m.optString("text")).setContentIntent(target).setAutoCancel(true).setLargeIcon(avatar);

        nm.notify((cid+tid).hashCode(),b.build());
    }
    @Override protected void onNewIntent(Intent intent){super.onNewIntent(intent);setIntent(intent);pendingConversation=intent.getStringExtra("conversationId");pendingThread=intent.getStringExtra("threadId");deliverNotification();pendingFileIntent57=intent;deliverSharedFile57();}
    private void deliverNotification(){if(pendingConversation==null||pendingConversation.isEmpty())return;String cid=pendingConversation,tid=pendingThread==null?"":pendingThread;pendingConversation="";emit("if(window.qasOpenNotification52){window.qasOpenNotification52("+JSONObject.quote(cid)+","+JSONObject.quote(tid)+")}else{window.qasPendingNotification52=["+JSONObject.quote(cid)+","+JSONObject.quote(tid)+"]}");}
    private void deliverSharedFile57(){
        Intent intent=pendingFileIntent57;if(intent==null)return;pendingFileIntent57=null;
        Uri selected=null;
        if(Intent.ACTION_VIEW.equals(intent.getAction()))selected=intent.getData();
        else if(Intent.ACTION_SEND.equals(intent.getAction()))selected=intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if(selected==null||!"content".equals(selected.getScheme()))return;
        final Uri uri=selected;final String transfer="shared-"+System.nanoTime();
        files.execute(()->{String name="共享文件",type=null;try{type=getContentResolver().getType(uri);}catch(Exception ignored){}if(type==null)type="application/octet-stream";
            try(android.database.Cursor cursor=getContentResolver().query(uri,new String[]{android.provider.OpenableColumns.DISPLAY_NAME},null,null,null)){if(cursor!=null&&cursor.moveToFirst())name=cursor.getString(0);}catch(Exception ignored){}
            final String filename=name,mime=type;
            try(InputStream input=getContentResolver().openInputStream(uri)){
                if(input==null)throw new IOException("无法读取共享文件");byte[] buffer=new byte[49152];int n;long total=0;
                while((n=input.read(buffer))!=-1){total+=n;if(total>32L*1024*1024)throw new IOException("预览文件上限为32 MB");String data=Base64.encodeToString(buffer,0,n,Base64.NO_WRAP);runOnUiThread(()->emit("window.qasSharedFile57?window.qasSharedFile57("+JSONObject.quote(transfer)+","+JSONObject.quote(filename)+","+JSONObject.quote(mime)+","+JSONObject.quote(data)+",false):(window.qasPendingFiles57||=[]).push(["+JSONObject.quote(transfer)+","+JSONObject.quote(filename)+","+JSONObject.quote(mime)+","+JSONObject.quote(data)+",false])"));}
                runOnUiThread(()->emit("window.qasSharedFile57?window.qasSharedFile57("+JSONObject.quote(transfer)+","+JSONObject.quote(filename)+","+JSONObject.quote(mime)+",'',true):(window.qasPendingFiles57||=[]).push(["+JSONObject.quote(transfer)+","+JSONObject.quote(filename)+","+JSONObject.quote(mime)+",'',true])"));
            }catch(Exception e){runOnUiThread(()->emit("window.qasSharedFile57&&window.qasSharedFile57("+JSONObject.quote(transfer)+",'','', '',true,"+JSONObject.quote("共享文件读取失败："+e.getMessage())+")"));}
        });
    }
    private void ack(JavaScriptReplyProxy reply, int id, String error) {
        try { JSONObject value = new JSONObject(); value.put("id", id); if (error != null) value.put("error",error); reply.postMessage(value.toString()); }
        catch (Exception ignored) { }
    }
    private void clearExport() {
        try { if (exportStream != null) exportStream.close(); } catch (IOException ignored) { }
        exportStream = null;
        if (exportFile != null) exportFile.delete();
        exportFile = null; finishReply = null;
    }
    @Override protected void onActivityResult(int request, int result, Intent data) {
        super.onActivityResult(request, result, data);
        if (request == PICK_FILE && fileCallback != null) {
            ArrayList<Uri> selected = new ArrayList<>();
            if (result == RESULT_OK && data != null) {
                if (data.getClipData() != null) for (int i=0; i<data.getClipData().getItemCount(); i++) selected.add(data.getClipData().getItemAt(i).getUri());
                else if (data.getData() != null) selected.add(data.getData());
            }
            finishFileChoice(selected.isEmpty() ? null : selected.toArray(new Uri[0]));
        }
        if (request == SAVE_FILE && finishReply != null) {
            final JavaScriptReplyProxy reply = finishReply; final int id = finishId;
            if (result != RESULT_OK || data == null || data.getData() == null) {
                ack(reply,id,"已取消保存"); clearExport(); return;
            }
            final Uri destination = data.getData(); final File source = exportFile;
            files.execute(() -> {
                String error = null;
                try (InputStream in = new FileInputStream(source); OutputStream out = getContentResolver().openOutputStream(destination, "wt")) {
                    if (out == null) throw new IOException("无法写入所选位置");
                    byte[] buffer = new byte[65536]; int n;
                    while ((n = in.read(buffer)) != -1) out.write(buffer,0,n);
                } catch (Exception e) { error = e.getMessage(); }
                final String finalError = error;
                runOnUiThread(() -> { ack(reply,id,finalError); clearExport(); if(finalError == null) toast("文件已保存"); });
            });
        }
    }
    @Override public void onRequestPermissionsResult(int request, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(request, permissions, results);
        if (request == MICROPHONE && microphoneRequest != null) {
            if (results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) microphoneRequest.grant(new String[]{PermissionRequest.RESOURCE_AUDIO_CAPTURE});
            else microphoneRequest.deny(); microphoneRequest = null;
        }
    }
    private boolean trustedOrigin(Uri uri) {
        return "https".equals(uri.getScheme()) && "appassets.androidplatform.net".equals(uri.getHost()) && (uri.getPort() == -1 || uri.getPort() == 443);
    }
    static String[] acceptedMimeTypes(String[] accepts) {
        java.util.LinkedHashSet<String> types = new java.util.LinkedHashSet<>();
        for (String item : accepts) for (String raw : item.split(",")) {
            String type = raw.trim().toLowerCase(java.util.Locale.ROOT);
            if (type.startsWith(".")) type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(type.substring(1));
            if (type != null && type.matches("[a-z0-9.+*-]+/[a-z0-9.+*-]+")) types.add(type);
        }
        return types.toArray(new String[0]);
    }
    static Intent filePickerIntent(String action, String[] types, boolean multiple) {
        Intent pick = new Intent(action).addCategory(Intent.CATEGORY_OPENABLE);
        pick.setType(types.length == 1 ? types[0] : "*/*");
        if (types.length > 1) pick.putExtra(Intent.EXTRA_MIME_TYPES, types);
        pick.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, multiple);
        pick.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        return pick;
    }
    private void finishFileChoice(Uri[] value) {
        ValueCallback<Uri[]> callback = fileCallback; fileCallback = null;
        if (callback != null) callback.onReceiveValue(value);
    }
    private void openExternal(Uri uri) {
        String scheme = uri.getScheme();
        if (!"https".equals(scheme) && !"http".equals(scheme) && !"mailto".equals(scheme)) return;
        try { startActivity(new Intent(Intent.ACTION_VIEW, uri)); }
        catch (Exception e) { toast("没有可打开此链接的应用"); }
    }
    private void toast(String text) { Toast.makeText(this,text,Toast.LENGTH_SHORT).show(); }
    @Override public void onBackPressed() {
        web.evaluateJavascript("Boolean(window.qasAndroidBack && window.qasAndroidBack())", result -> {
            if (!"true".equals(result)){long now=System.currentTimeMillis();if(now-lastBack>2000){lastBack=now;toast("再按一次返回键退出");return;}new AlertDialog.Builder(this).setMessage("退出 QasDM？本机数据会保留。")
                .setNegativeButton("取消",null).setPositiveButton("退出",(d,w)->{stopService(new Intent(this,QasService.class));finish();}).show();}
        });
    }
    @Override protected void onPause(){super.onPause();emit("window.qasNativeBackground53=true");}
    @Override protected void onResume(){super.onResume();emit("window.qasNativeBackground53=false");}
    @Override protected void onDestroy() {
        if (fileCallback != null) fileCallback.onReceiveValue(null);
        if (microphoneRequest != null) microphoneRequest.deny();
        clearExport(); files.shutdown(); formulas.shutdown(); if(QasService.current==null){web.destroy();runtimeWeb=null;} super.onDestroy();
    }
}

