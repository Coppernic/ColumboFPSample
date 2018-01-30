package fr.coppernic.sample.columbofp.presenter;

import android.content.Context;
import android.graphics.Bitmap;

import com.integratedbiometrics.ibscanultimate.IBScanDevice;

import fr.coppernic.sample.columbofp.business.MainView;
import fr.coppernic.sample.columbofp.interactor.FingerPrintInteractorImpl;
import fr.coppernic.sample.columbofp.interactor.FingerprintInteractor;
import fr.coppernic.sample.columbofp.interactor.FpDialogManager;

/**
 * Created by michael on 26/01/18.
 */

public class MainPresenterImpl implements MainPresenter, FingerprintInteractor.Listener {
    private static final String TAG = "MainPresenterImpl";
    private FingerprintInteractor fingerprintInteractor;
    private MainView mainView;
    FpDialogManager fpDialogManager;

    public MainPresenterImpl(MainView mainView){
        this.mainView =mainView;
        mainView.showFAB(false);
        fingerprintInteractor = new FingerPrintInteractorImpl((Context)mainView, this);
    }
    @Override
    public void captureFingerPrint() {
        mainView.startProgress();
        fingerprintInteractor.captureFingerPrint();
    }

    @Override
    public void setUp() {
        fingerprintInteractor.powerOn(true);
    }

    @Override
    public void tearDown() {
        fingerprintInteractor.tearDown();
    }

    @Override
    public void onReaderReady(final IBScanDevice reader) {
       /* ((Activity)mainView).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                fpDialogManager = new FpDialogManager((Context)mainView, reader);
                /*fpDialogManager.show(new FpDialogManager.FingerPrintListener() {
                    @Override
                    public void onFingerPrintImageAvailable(Bitmap bmp) {
                        onAcquisitionCompleted(bmp);
                    }
                });*/
      /*          fpDialogManager.show(MainPresenterImpl.this);
            }
        });*/

       /* ((Activity)mainView).runOnUiThread(new Runnable() {
            @Override
            public void run() {*/
                mainView.stopProgress();
      /*      }
        });*/
    }

    @Override
    public void onAcquisitionCompleted(final Bitmap fingerPrint) {
     /*   ((Activity)mainView).runOnUiThread(new Runnable() {
            @Override
            public void run() {*/
                mainView.showFpImage(fingerPrint);
      /*      }
        });*/
        fingerprintInteractor.endCapture();
    }

    @Override
    public void onReaderPoweredUp() {
        mainView.showFAB(true);
    }
}
