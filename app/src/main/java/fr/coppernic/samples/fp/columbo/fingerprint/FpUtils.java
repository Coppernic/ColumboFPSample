package fr.coppernic.samples.fp.columbo.fingerprint;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class FpUtils {

    private static void scanFile(Context context, Uri imageUri) {
        Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        scanIntent.setData(imageUri);
        context.sendBroadcast(scanIntent);
    }
}
