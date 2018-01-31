package fr.coppernic.sample.columbofp.presenter;

import android.content.Context;
import android.graphics.Bitmap;

import com.integratedbiometrics.ibscanultimate.IBScanDevice;

import fr.coppernic.sample.columbofp.R;
import fr.coppernic.sample.columbofp.business.MainView;
import fr.coppernic.sample.columbofp.interactor.FingerPrintInteractorImpl;
import fr.coppernic.sample.columbofp.interactor.FingerprintInteractor;

/**
 * Created by michael on 26/01/18.
 */

public class MainPresenterImpl implements MainPresenter, FingerprintInteractor.Listener {
    private FingerprintInteractor fingerprintInteractor;
    private MainView mainView;

    public MainPresenterImpl(MainView mainView) {
        this.mainView = mainView;
        mainView.showMessage(((Context)mainView).getString(R.string.wait_FP_powered));
        mainView.showFAB(false);
        fingerprintInteractor = new FingerPrintInteractorImpl((Context) mainView, this);
    }

    @Override
    public void captureFingerPrint() {
        mainView.startProgress();
        fingerprintInteractor.captureFingerPrint();
    }

    @Override
    public void setUp() {
        mainView.showFAB(false);
        fingerprintInteractor.powerOn(true);
    }

    @Override
    public void tearDown() {
        fingerprintInteractor.tearDown();
    }

    @Override
    public void onReaderReady(final IBScanDevice reader) {
        mainView.stopProgress();
        if(reader == null){//init reader failed
            mainView.showMessage(((Context)mainView).getString(R.string.FP_opened_error));
        }
    }

    @Override
    public void onAcquisitionCompleted(final Bitmap fingerPrint) {
        mainView.showFpImage(fingerPrint);
        fingerprintInteractor.endCapture();
    }

    @Override
    public void onReaderPoweredUp() {
        mainView.showFAB(true);
        mainView.showMessage(((Context)mainView).getString(R.string.press_FP_button));
    }
}
