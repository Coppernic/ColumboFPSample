package fr.coppernic.sample.columbofp.fingerprint;

import android.graphics.Bitmap;

import com.integratedbiometrics.ibscanultimate.IBScanDevice;

/**
 * Created by michael on 26/01/18.
 */

public interface FingerPrintInterface {

    interface Listener {
        /**
         * FingerPrint reader is ready to use
         */
        void onReaderReady(IBScanDevice reader);

        void onAcquisitionCompleted(Bitmap fingerPrint);

        void onReaderPoweredUp();
    }


    /**
     * Starting a FingerPrint capture
     */
    void captureFingerPrint();

    void endCapture();

    void setUp();

    void tearDown();
}
