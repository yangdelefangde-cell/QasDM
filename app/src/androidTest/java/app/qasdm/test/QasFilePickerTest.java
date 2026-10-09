package app.qasdm.test;

import android.content.Intent;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class QasFilePickerTest {
    @Test public void sourcesRetainMimeTypesMultipleSelectionAndReadAccess() {
        String[] types = MainActivity.acceptedMimeTypes(new String[]{"image/*,video/*", ".txt", "audio/*", "image/*"});
        assertArrayEquals(new String[]{"image/*", "video/*", "text/plain", "audio/*"}, types);
        for (String action : new String[]{Intent.ACTION_OPEN_DOCUMENT, Intent.ACTION_GET_CONTENT}) {
            Intent intent = MainActivity.filePickerIntent(action, types, true);
            assertEquals(action, intent.getAction()); assertEquals("*/*", intent.getType());
            assertArrayEquals(types, intent.getStringArrayExtra(Intent.EXTRA_MIME_TYPES));
            assertTrue(intent.getBooleanExtra(Intent.EXTRA_ALLOW_MULTIPLE, false));
            assertTrue(intent.hasCategory(Intent.CATEGORY_OPENABLE));
            assertTrue((intent.getFlags() & Intent.FLAG_GRANT_READ_URI_PERMISSION) != 0);
        }
        assertEquals("image/*", MainActivity.filePickerIntent(Intent.ACTION_OPEN_DOCUMENT, new String[]{"image/*"}, false).getType());
        assertEquals("*/*", MainActivity.filePickerIntent(Intent.ACTION_GET_CONTENT, new String[]{}, false).getType());
    }
}
