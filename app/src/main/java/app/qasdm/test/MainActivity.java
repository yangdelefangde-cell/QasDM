package app.qasdm.test;

import android.Manifest;
import android.app.Activity;
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
    private ValueCallback<Uri[]> fileCallback;
    private PermissionRequest microphoneRequest;
    private File exportFile;
    private FileOutputStream exportStream;
    private String exportName, exportMime;
    private long exported;
    private int finishId;
    private JavaScriptReplyProxy finishReply;
    private final ExecutorService files = Executors.newSingleThreadExecutor();

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(0xff24283d);
        getWindow().setNavigationBarColor(0xff24283d);
        web = new WebView(this);
        setContentView(web);
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
                Intent pick = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                pick.addCategory(Intent.CATEGORY_OPENABLE);
                String[] types = params.getAcceptTypes();
                String type = types.length == 1 && types[0].contains("/") ? types[0] : "*/*";
                pick.setType(type);
                pick.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, params.getMode() == FileChooserParams.MODE_OPEN_MULTIPLE);
                try { startActivityForResult(pick, PICK_FILE); }
                catch (Exception e) { fileCallback.onReceiveValue(null); fileCallback = null; toast("无法打开文件选择器"); }
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
        web.loadUrl(ORIGIN + "/assets/www/index.html");
    }

    private void handleMessage(JSONObject m, JavaScriptReplyProxy reply) throws Exception {
        int id = m.getInt("id");
        try {
            switch (m.getString("action")) {
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
            fileCallback.onReceiveValue(selected.isEmpty() ? null : selected.toArray(new Uri[0])); fileCallback = null;
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
    private void openExternal(Uri uri) {
        String scheme = uri.getScheme();
        if (!"https".equals(scheme) && !"http".equals(scheme) && !"mailto".equals(scheme)) return;
        try { startActivity(new Intent(Intent.ACTION_VIEW, uri)); }
        catch (Exception e) { toast("没有可打开此链接的应用"); }
    }
    private void toast(String text) { Toast.makeText(this,text,Toast.LENGTH_SHORT).show(); }
    @Override public void onBackPressed() {
        web.evaluateJavascript("Boolean(window.qasAndroidBack && window.qasAndroidBack())", result -> {
            if (!"true".equals(result)) new AlertDialog.Builder(this).setMessage("退出 QasDM？本机数据会保留。")
                .setNegativeButton("取消",null).setPositiveButton("退出",(d,w)->finish()).show();
        });
    }
    @Override protected void onDestroy() {
        if (fileCallback != null) fileCallback.onReceiveValue(null);
        if (microphoneRequest != null) microphoneRequest.deny();
        clearExport(); files.shutdown(); web.destroy(); super.onDestroy();
    }
}
