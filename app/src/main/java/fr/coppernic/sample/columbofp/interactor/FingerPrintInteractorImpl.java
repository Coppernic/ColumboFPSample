package fr.coppernic.sample.columbofp.interactor;

import android.app.Activity;
import android.content.Context;

import com.integratedbiometrics.ibscanultimate.IBScan;
import com.integratedbiometrics.ibscanultimate.IBScanDevice;
import com.integratedbiometrics.ibscanultimate.IBScanException;
import com.integratedbiometrics.ibscanultimate.IBScanListener;

import fr.coppernic.sample.columbofp.settings.Settings;
import fr.coppernic.sdk.power.impl.cone.ConePeripheral;
import fr.coppernic.sdk.powermgmt.PowerMgmt;
import fr.coppernic.sdk.powermgmt.PowerMgmtFactory;
import fr.coppernic.sdk.powermgmt.PowerUtilsNotifier;
import fr.coppernic.sdk.powermgmt.cone.identifiers.InterfacesCone;
import fr.coppernic.sdk.powermgmt.cone.identifiers.ManufacturersCone;
import fr.coppernic.sdk.powermgmt.cone.identifiers.ModelsCone;
import fr.coppernic.sdk.powermgmt.cone.identifiers.PeripheralTypesCone;
import fr.coppernic.sdk.utils.core.CpcDefinitions;
import fr.coppernic.sdk.utils.core.CpcResult;
import timber.log.Timber;

/**
 * Created by michael on 26/01/18.
 */

public class FingerPrintInteractorImpl implements FingerprintInteractor, IBScanListener {
    Context context;
    private static final String PRODUCT_NAME = "COLUMBO";
    private static final String TAG = "FingerPrintInteractorImpl";
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

    private PowerMgmt powerMgmt;

    private FingerprintInteractor.Listener listener;

    private FpDialogManager fpDialogManager;

    private Settings settings;


    public FingerPrintInteractorImpl(final Context context, final Listener listener){
        this.listener = listener;
        this.context = context;
        settings = new Settings(context);
        powerMgmt = PowerMgmtFactory.get().setContext(context).setNotifier(new PowerUtilsNotifier() {
            @Override
            public void onPowerUp(CpcResult.RESULT result, int i, int i1) {
                if (result == CpcResult.RESULT.OK && (i == CpcDefinitions.VID_FP_COLOMBO_READER && i1 == CpcDefinitions.PID_FP_COLOMBO_READER)) {
                    Timber.d("Fp reader powered on");
                    ((Activity)context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            listener.onReaderPoweredUp();
                        }
                    });
                }
            }

            @Override
            public void onPowerDown(CpcResult.RESULT result, int i, int i1) {

            }
        }).build();
    }

    /**
     * Instantiates a IBScan object.
     */
    public void initializeIbScanClass(){
        reader = IBScan.getInstance(context);
        reader.setContext(context);
        reader.setScanListener(this);
        try {
            IBScan.DeviceDesc deviceDesc = reader.getDeviceDescription(0);
            settings.setReaderName(deviceDesc.productName);
            settings.setFirmwareVersion(deviceDesc.fwVersion);
            settings.setSerialNumber(deviceDesc.serialNumber);
            settings.setProductionRevisionn(deviceDesc.devRevision);
        } catch (IBScanException e) {
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

            if(readerDevice != null) {
                if(readerDevice.isOpened())
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
    public void close(){
        Timber.d( "CLOSE");
        if (readerDevice != null) {
            try {
                readerDevice.close();
            } catch (IBScanException e) {
                Timber.e("Issue during device close(exeception: " + e + ")");
                e.printStackTrace();
            }
        }
    }

    @Override
    public void captureFingerPrint() {
        initializeIbScanClass();
        openInternal();
    }

    @Override
    public void endCapture() {
        close();
    }

    @Override
    public void powerOn(boolean on) {
        if(on) {
           // ConePeripheral.FP_IB_COLOMBO_USB.on(context);
            powerMgmt.setPower(PeripheralTypesCone.FingerPrintReader,
                    ManufacturersCone.IntegratedBiometrics, ModelsCone.Columbo,
                    InterfacesCone.UsbGpioPort, on);
        }else{
            ConePeripheral.FP_IB_COLOMBO_USB.off(context);
        }
    }

    @Override
    public void tearDown() {
        if(reader != null) {
            close();
            reader.setContext(null);
            reader.setScanListener(null);
            reader = null;
        }
        powerOn(false);
    }

    /********************IB listener *******************/

    @Override
    public void scanDeviceAttached(int i) {
        Timber.d("New IB scanner device attached" );
    }

    @Override
    public void scanDeviceDetached(int i) {
        Timber.d("New IB scanner device detached" );
    }

    @Override
    public void scanDevicePermissionGranted(int i, boolean b) {
        Timber.d("scanDevicePermissionGranted" );
    }

    @Override
    public void scanDeviceCountChanged(int i) {

    }

    @Override
    public void scanDeviceInitProgress(int i, int i1) {

    }

    @Override
    public void scanDeviceOpenComplete(int i, IBScanDevice ibScanDevice, IBScanException e) {
        Timber.d("scanDeviceOpenComplete" );
        readerDevice = ibScanDevice;


        /*((Activity)context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                fpDialogManager = new FpDialogManager(context, readerDevice);
                fpDialogManager.show(new FpDialogManager.FingerPrintListener() {
                    @Override
                    public void onFingerPrintImageAvailable(Bitmap bmp) {
                        listener.onAcquisitionCompleted(bmp);
                    }
                });
            }
        });*/

        ((Activity)context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                listener.onReaderReady(readerDevice);
                fpDialogManager = new FpDialogManager(context, readerDevice);
                fpDialogManager.show(listener);
            }
        });
    }
}
