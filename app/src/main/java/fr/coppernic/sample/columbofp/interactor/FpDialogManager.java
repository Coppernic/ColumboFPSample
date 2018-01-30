package fr.coppernic.sample.columbofp.interactor;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.integratedbiometrics.ibscanultimate.IBScanDevice;
import com.integratedbiometrics.ibscanultimate.IBScanDeviceListener;
import com.integratedbiometrics.ibscanultimate.IBScanException;

import java.util.concurrent.atomic.AtomicBoolean;

import fr.coppernic.sample.columbofp.R;
import fr.coppernic.sdk.utils.util.Preconditions;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

public class FpDialogManager implements IBScanDeviceListener {

    private static final String TAG = "DialogManager";
    private final Context context;
    private final MaterialDialog dialog;
    private final AtomicBoolean retry = new AtomicBoolean(false);
    private final AtomicBoolean captureOk = new AtomicBoolean(false);
    private final Handler handler = new Handler();
    private final IBScanDevice reader;
    private FingerprintInteractor.Listener listener;

    private Bitmap currentImage;
    private Disposable disposable;
    private final Runnable disposeAndClose = new Runnable() {
        @Override
        public void run() {
            if (disposable != null) {
                disposable.dispose();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IBScanException e) {
                    e.printStackTrace();
                }
            }
        }
    };
    @SuppressWarnings("FieldCanBeLocal")
    private final MaterialDialog.SingleButtonCallback negative = new MaterialDialog.SingleButtonCallback() {
        @Override
        public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
            Log.v(TAG, "OnNegative");
           // Schedulers.io().scheduleDirect(disposeAndClose);
        }
    };
    private TextView fpMessage;
    private ImageView mIvView;
    private FingerPrintListener fingerPrintListener;

    @SuppressWarnings("FieldCanBeLocal")
    private final MaterialDialog.SingleButtonCallback positive = new MaterialDialog.SingleButtonCallback() {
        @Override
        public void onClick(@NonNull MaterialDialog materialDialog, @NonNull final DialogAction dialogAction) {
            Log.v(TAG, "OnPositive");
            if (!captureOk.get()) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        show(listener);
                    }
                }, 100);
            } else if (listener != null) {
                listener.onAcquisitionCompleted(currentImage);
            }
        }
    };

    public FpDialogManager(Context context, IBScanDevice r) {
        this.context = context;
        reader = r;
        MaterialDialog.Builder builder = new MaterialDialog.Builder(context);
        dialog = builder.title(R.string.fp_title)
            .customView(R.layout.dialog_finger_print, true)
            .cancelable(true)
            .negativeText(android.R.string.cancel)
            .positiveText(android.R.string.ok)
            .onNegative(negative)
            .onPositive(positive)
            .build();

        if (dialog.getWindow() != null) {
            View v = dialog.getWindow().getDecorView();
            fpMessage = v.findViewById(R.id.fpMessage);
            mIvView = v.findViewById(R.id.fingerPrintView);
        }
    }

    public void show(FingerprintInteractor.Listener listener) {
        this.listener = Preconditions.checkNotNull(listener);
        if (dialog != null) {
            dialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);
            mIvView.setImageResource(R.drawable.fingerprint);
            fpMessage.setText(R.string.fp_touch);
            dialog.show();
            startCapture();
        }

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                if (!captureOk.get()) {
                    try {
                        if (reader.isCaptureActive())
                            reader.cancelCaptureImage();
                    } catch (IBScanException e) {
                        e.printStackTrace();
                    }
                    mIvView.setImageResource(R.drawable.alert_circle_outline);
                    dialog.setActionButton(DialogAction.POSITIVE, R.string.dlg_retry);
                    dialog.getActionButton(DialogAction.POSITIVE).setEnabled(true);
                    retry.set(true);
                }
            }
        }, 5000);
    }

    public void dismiss() {
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    public Dialog getDialog() {
        return dialog;
    }

    public void startCapture(){
        try {
            captureOk.set(false);
            IBScanDevice.ImageType imageType = IBScanDevice.ImageType.FLAT_SINGLE_FINGER;
            if ((reader != null) && (reader.isOpened())) {
                if (reader.isCaptureAvailable(imageType, IBScanDevice.ImageResolution.RESOLUTION_500)) {
                    reader.beginCaptureImage(imageType, IBScanDevice.ImageResolution.RESOLUTION_500,
                            IBScanDevice.OPTION_AUTO_CAPTURE | IBScanDevice.OPTION_AUTO_CONTRAST | IBScanDevice.OPTION_IGNORE_FINGER_COUNT);
                    reader.setScanDeviceListener(this);
                }
            }
        }
        catch (IBScanException ibse)
        {
            Timber.e("Could not begin capturing Finger Print (exception: " + ibse.getType() + ")");
        }
    }

    @Override
    public void deviceCommunicationBroken(IBScanDevice ibScanDevice) {

    }

    @Override
    public void deviceImagePreviewAvailable(IBScanDevice ibScanDevice, final IBScanDevice.ImageData imageData) {
        ((Activity)context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mIvView.setImageBitmap(imageData.toBitmap());
            }
        });
    }

    @Override
    public void deviceFingerCountChanged(IBScanDevice ibScanDevice, IBScanDevice.FingerCountState fingerCountState) {

    }

    @Override
    public void deviceFingerQualityChanged(IBScanDevice ibScanDevice, IBScanDevice.FingerQualityState[] fingerQualityStates) {

    }

    @Override
    public void deviceAcquisitionBegun(IBScanDevice ibScanDevice, IBScanDevice.ImageType imageType) {

    }

    @Override
    public void deviceAcquisitionCompleted(IBScanDevice ibScanDevice, IBScanDevice.ImageType imageType) {

    }

    @Override
    public void deviceImageResultAvailable(IBScanDevice ibScanDevice, final IBScanDevice.ImageData imageData, IBScanDevice.ImageType imageType, IBScanDevice.ImageData[] imageData1) {

        ((Activity)context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                captureOk.set(true);
                mIvView.setImageResource(R.drawable.check);
                fpMessage.setText(R.string.fp_recorded);

                retry.set(false);
                dialog.getActionButton(DialogAction.POSITIVE).setEnabled(true);
                dialog.setActionButton(DialogAction.POSITIVE, android.R.string.ok);
                SystemClock.sleep(1000);
                currentImage = imageData.toBitmap();
            }
        });
    }

    @Override
    public void deviceImageResultExtendedAvailable(IBScanDevice ibScanDevice, IBScanException e, IBScanDevice.ImageData imageData, IBScanDevice.ImageType imageType, int i, IBScanDevice.ImageData[] imageData1, IBScanDevice.SegmentPosition[] segmentPositions) {

    }

    @Override
    public void devicePlatenStateChanged(IBScanDevice ibScanDevice, IBScanDevice.PlatenState platenState) {

    }

    @Override
    public void deviceWarningReceived(IBScanDevice ibScanDevice, IBScanException e) {

    }

    @Override
    public void devicePressedKeyButtons(IBScanDevice ibScanDevice, int i) {

    }

    public interface FingerPrintListener {
        void onFingerPrintImageAvailable(Bitmap bmp);
    }
}
