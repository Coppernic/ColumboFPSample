package fr.coppernic.samples.fp.columbo.fingerprint;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.integratedbiometrics.ibscanultimate.IBScan;
import com.integratedbiometrics.ibscanultimate.IBScanDevice;
import com.integratedbiometrics.ibscanultimate.IBScanException;
import com.integratedbiometrics.ibscanultimate.IBScanListener;

import java.lang.ref.WeakReference;

import fr.coppernic.samples.fp.columbo.settings.Settings;
import fr.coppernic.sdk.utils.core.CpcResult;
import timber.log.Timber;

/**
 * Created by michael on 26/01/18.
 */

public class IBScanFingerPrint implements FingerPrint {
    private final WeakReference<Context> context;
    private static final String PRODUCT_NAME = "COLUMBO";
    /**
     * A handle to the single instance of the IBScan class that will be the primary interface to
     * the library, for operations like getting the number of scanners (getDeviceCount()) and
     * opening scanners (openDeviceAsync()).
     */
    private IBScan reader;
    /**
     * A handle to the open IBScanDevice (if any) that will be the interface for getting data from
     * the open scanner, including capturing the image (beginCaptureImage(), cancelCaptureImage()),
     * and the type of image being captured.
     */
    private IBScanDevice readerDevice;

    private FingerPrint.Listener listener;

    private FpDialogManager fpDialogManager;

    private Handler updateUiHandler = new Handler(Looper.getMainLooper());

    public IBScanFingerPrint(Context context, final Listener listener) {
        this.listener = listener;
        this.context = new WeakReference<>(context);
    }

    /**
     * Instantiates a IBScan object.
     */
    private void initializeIbScanClass() {
        Context ctx = context.get();
        if (ctx == null) {
            listener.onReaderReady(CpcResult.RESULT.INVALID_CONTEXT);
        } else {
            reader = IBScan.getInstance(ctx);
            reader.setScanListener(ibScanListener);
            try {
                IBScan.DeviceDesc deviceDesc = reader.getDeviceDescription(0);
                Settings settings = new Settings(ctx);
                settings.setSdkVersion(reader.getSdkVersion().file);
                settings.setReaderName(deviceDesc.productName);
                settings.setFirmwareVersion(deviceDesc.fwVersion);
                settings.setSerialNumber(deviceDesc.serialNumber);
                settings.setProductionRevisionn(deviceDesc.devRevision);
            } catch (IBScanException e) {
                e.printStackTrace();
                Timber.e(e.getMessage());
                listener.onReaderReady(CpcResult.RESULT.ERROR);
            }
        }
    }

    /**
     * Set The Finger Print Reader Property
     */
    private void initializeDeviceProperty() {
        try {
            Settings settings = new Settings(context.get());
            readerDevice.setProperty(IBScanDevice.PropertyId.CAPTURE_TIMEOUT, settings.getCaptureTimeout());
            readerDevice.setProperty(IBScanDevice.PropertyId.ENABLE_POWER_SAVE_MODE, settings.getPowerSaveMode());
        } catch (IBScanException e) {
            Timber.e("Fail to set reader property");
            e.printStackTrace();
        }
    }


    private void openInternal() {
        try {
            //Index of device that will be initialized it is always 0 has there is only on Finger Print Reader
            IBScan.DeviceDesc deviceDesc = reader.getDeviceDescription(0);

            if (!deviceDesc.productName.contains(PRODUCT_NAME)) {
                return;
            }

            if (readerDevice != null) {
                if (readerDevice.isOpened())
                    close();
                readerDevice = null;
            }

            reader.openDeviceAsync(0);
        } catch (IBScanException e) {
            Timber.d("Failed to open devices");
        }
    }

    /**
     * Closes the communication with Finger Print reader
     */
    private void close() {
        Timber.d("CLOSE");
        if (readerDevice != null) {
            try {
                readerDevice.close();
            } catch (IBScanException e) {
                Timber.e("Issue during device close(exception: " + e + ")");
                e.printStackTrace();
            }
        }
    }

    @Override
    public void capture() {
        if (reader == null) {
            initializeIbScanClass();
        }
        openInternal();
    }

    @Override
    public void stopCapture() {
        close();
    }

    @Override
    public void tearDown() {
        if (reader != null) {
            close();
            reader.setScanListener(null);
            reader = null;
        }
        if (fpDialogManager != null) {
            fpDialogManager.dismiss();
        }
    }

    @Override
    public void setUp() {
        //initializeIbScanClass();
    }

    /********************IB listener *******************/
    private IBScanListener ibScanListener = new IBScanListener() {
        @Override
        public void scanDeviceAttached(int i) {
            Timber.d("New IB scanner device attached");
        }

        @Override
        public void scanDeviceDetached(int i) {
            Timber.d("New IB scanner device detached");
        }

        @Override
        public void scanDevicePermissionGranted(int i, boolean b) {
            Timber.d("scanDevicePermissionGranted");
        }

        @Override
        public void scanDeviceCountChanged(int i) {

        }

        @Override
        public void scanDeviceInitProgress(int i, int i1) {

        }

        @Override
        public void scanDeviceOpenComplete(int i, IBScanDevice ibScanDevice, IBScanException e) {
            Timber.d("scanDeviceOpenComplete");
            readerDevice = ibScanDevice;
            initializeDeviceProperty();
            updateUiHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (context.get() == null) {
                        listener.onReaderReady(CpcResult.RESULT.INVALID_CONTEXT);
                    } else {
                        listener.onReaderReady(CpcResult.RESULT.OK);
                        fpDialogManager = new FpDialogManager(context.get(), readerDevice);
                        fpDialogManager.show(listener);
                    }
                }
            });
        }
    };




}
