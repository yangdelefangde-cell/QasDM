package app.qasdm.test;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class QasLatexTest {
    @Test public void nativeCommandsAndChineseProduceVisibleTransparentPixels() throws Exception {
        String[] formulas = {"\\frac{a+b}{c}", "\\text{你好，Qas}",
            "\\textcolor{blue}{\\text{测试}}", "\\colorbox{red}{\\text{文本内容}}",
            "\\bgcolor{white}{\\textcolor{black}{\\text{测试}}}",
            "\\scalebox{1.2}{\\rotatebox{15}{\\fbox{\\text{旋转}}}}",
            "\\ovalbox{\\text{测试}}", "\\begin{array}{l}a+b\\\\c+d\\end{array}"};
        for (String formula : formulas) {
            JSONObject result = QasLatex.render(new JSONObject().put("formula", formula).put("size", 32).put("color", "#e7e9f1"));
            String url = result.getString("data");
            byte[] bytes = Base64.decode(url.substring(url.indexOf(',') + 1), Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            assertNotNull(formula, bitmap); assertTrue(result.getDouble("baseline") > 0);
            boolean visible = false;
            for (int y = 0; y < bitmap.getHeight(); y++) for (int x = 0; x < bitmap.getWidth(); x++)
                visible |= (bitmap.getPixel(x, y) >>> 24) > 0;
            assertTrue("Empty rendering: " + formula, visible); bitmap.recycle();
        }
    }
    @Test public void oversizedOrExternalFormulaIsRejected() throws Exception {
        for (String source : new String[]{new String(new char[6001]).replace('\0','a'), "\\includegraphics{/sdcard/a.png}"}) {
            try { QasLatex.render(new JSONObject().put("formula", source)); fail("Unsafe or oversized formula accepted"); }
            catch (IllegalArgumentException expected) { assertNotNull(expected.getMessage()); }
        }
    }
}
