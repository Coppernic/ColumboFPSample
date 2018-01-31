package fr.coppernic.sample.columbofp.business;

import android.graphics.Bitmap;

/**
 * Created by michael on 26/01/18.
 */

public interface MainView {

    void showFpImage(Bitmap fingerPrint);

    void startProgress();

    void stopProgress();

    void showFAB(boolean value);

    void showMessage(String value);

}
