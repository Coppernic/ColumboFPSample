package fr.coppernic.sample.columbofp.interactor;

import android.graphics.Bitmap;

import com.integratedbiometrics.ibscanultimate.IBScanDevice;

/**
 * Created by michael on 26/01/18.
 */

public interface FingerprintInteractor {

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

    /**
     * Powers on/off FingerPrint reader
     *
     * @param on True on, false off
     */
    void powerOn(boolean on);

    void tearDown();
}
