package fr.coppernic.sample.columbofp.presenter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;

import fr.coppernic.sample.columbofp.business.MainView;
import fr.coppernic.sample.columbofp.interactor.FingerPrintInteractorImpl;
import fr.coppernic.sample.columbofp.interactor.FingerprintInteractor;

/**
 * Created by michael on 26/01/18.
 */

public class MainPresenterImpl implements MainPresenter, FingerprintInteractor.Listener {
    private static final String TAG = "MainPresenterImpl";
    private FingerprintInteractor fingerprintInteractor;
    private MainView mainView;

    public MainPresenterImpl(MainView mainView){
        this.mainView =mainView;
        fingerprintInteractor = new FingerPrintInteractorImpl((Context)mainView);
    }
    @Override
    public void captureFingerPrint() {
        fingerprintInteractor.captureFingerPrint(this);
    }

    @Override
    public void setUp() {
        fingerprintInteractor.powerOn(true);
    }

    @Override
    public void tearDown() {
        fingerprintInteractor.powerOn(false);
    }

    @Override
    public void onReaderReady() {

    }

    @Override
    public void onImagePreviewAvailable(final Bitmap fingerPrinPreview) {
        ((Activity)mainView).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mainView.showFpImage(fingerPrinPreview);
            }
        });

    }

    @Override
    public void onAcquisitionCompleted(final Bitmap fingerPrint) {
        ((Activity)mainView).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mainView.showFpImage(fingerPrint);
            }
        });
        fingerprintInteractor.endCapture();
    }
}
