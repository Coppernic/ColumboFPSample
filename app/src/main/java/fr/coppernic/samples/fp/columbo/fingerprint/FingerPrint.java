package fr.coppernic.samples.fp.columbo.fingerprint;

import android.graphics.Bitmap;

import fr.coppernic.sdk.utils.core.CpcResult;

/**
 * Created by michael on 26/01/18.
 */

public interface FingerPrint {

    interface Listener {
        /**
         * IBScanFingerPrint reader is ready to use
         */
        void onReaderReady(CpcResult.RESULT res);

        void onAcquisitionCompleted(Bitmap fingerPrint);
    }


    /**
     * Starting a IBScanFingerPrint capture
     */
    void capture();

    /**
     * Stopping a IBScan Fingerprint capture
     */
    void stopCapture();

    void setUp();

    void tearDown();
}
