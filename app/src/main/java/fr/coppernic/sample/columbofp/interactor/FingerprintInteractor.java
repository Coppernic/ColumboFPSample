package fr.coppernic.sample.columbofp.interactor;

import android.graphics.Bitmap;

/**
 * Created by michael on 26/01/18.
 */

public interface FingerprintInteractor {

    interface Listener {
        /**
         * FingerPrint reader is ready to use
         */
        void onReaderReady();

        void onImagePreviewAvailable(Bitmap fingerPrintPreview);

        void onAcquisitionCompleted(Bitmap fingerPrint);
    }


    /**
     * Starting a FingerPrint capture
     */
    void captureFingerPrint(Listener listener);

    void endCapture();
    /**
     * Powers on/off FingerPrint reader
     * @param on True on, false off
     */
    void powerOn(boolean on);
}
