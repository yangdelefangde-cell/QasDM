package app.qasdm.test;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.Base64;
import org.json.JSONObject;
import org.scilab.forge.jlatexmath.TeXFormula;
import ru.noties.jlatexmath.JLatexMathDrawable;
import java.io.ByteArrayOutputStream;

/** A bounded, transparent native formula renderer. Run on one worker thread. */
final class QasLatex {
    private static boolean fontsReady;

    static JSONObject render(JSONObject request) throws Exception {
        String source = request.getString("formula");
        if (source.length() > 6000) throw new IllegalArgumentException("公式过长");
        if (source.matches("(?s).*\\\\(?:includegraphics|input|include|write|openout|read)\\b.*"))
            throw new IllegalArgumentException("公式不支持读取外部文件");
        if (!fontsReady) {
            for (Character.UnicodeBlock block : new Character.UnicodeBlock[]{
                    Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS,
                    Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A,
                    Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION,
                    Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS})
                TeXFormula.registerExternalFont(block, "sans-serif", "serif");
            fontsReady = true;
        }
        // bgcolour aliases used by the web editor share the native colourbox command.
        source = source.replaceAll("\\\\bgcolor\\b", "\\\\colorbox");
        float size = (float)Math.max(12, Math.min(128, request.optDouble("size", 32)));
        int foreground = Color.parseColor(request.optString("color", "#242631"));
        JLatexMathDrawable drawable = JLatexMathDrawable.builder(source)
                .textSize(size).color(foreground).padding(2).build();
        int width = drawable.getIntrinsicWidth(), height = drawable.getIntrinsicHeight();
        if (width <= 0 || height <= 0 || width > 8192 || height > 4096 || (long)width * height > 6000000)
            throw new IllegalArgumentException("公式尺寸超过渲染上限");
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try {
            drawable.draw(new Canvas(bitmap));
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, bytes);
            if (bytes.size() > 4000000) throw new IllegalArgumentException("公式图片过大");
            JSONObject result = new JSONObject();
            result.put("width", width); result.put("height", height);
            result.put("baseline", drawable.icon().getBaseLine());
            result.put("data", "data:image/png;base64," + Base64.encodeToString(bytes.toByteArray(), Base64.NO_WRAP));
            result.put("engine", "JLaTeXMath Android 0.2.0");
            return result;
        } finally { bitmap.recycle(); bytes.close(); }
    }
}
