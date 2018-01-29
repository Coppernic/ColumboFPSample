package fr.coppernic.sample.columbofp.interactor;

import android.content.Context;

import com.integratedbiometrics.ibscanultimate.IBScan;
import com.integratedbiometrics.ibscanultimate.IBScanDevice;
import com.integratedbiometrics.ibscanultimate.IBScanDeviceListener;
import com.integratedbiometrics.ibscanultimate.IBScanException;
import com.integratedbiometrics.ibscanultimate.IBScanListener;

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

public class FingerPrintInteractorImpl implements FingerprintInteractor, IBScanDeviceListener, IBScanListener {
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

    private boolean isOpen = false;

    public FingerPrintInteractorImpl(Context context){

        this.context = context;
        powerMgmt = PowerMgmtFactory.get().setContext(context).setNotifier(new PowerUtilsNotifier() {
            @Override
            public void onPowerUp(CpcResult.RESULT result, int i, int i1) {
                if (result == CpcResult.RESULT.OK && (i == CpcDefinitions.VID_FP_COLOMBO_READER && i1 == CpcDefinitions.PID_FP_COLOMBO_READER)) {
                    Timber.d("Fp reader powered on");
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
            isOpen = true;
        } catch (IBScanException e) {
            Timber.d("Failed to open devices");
        }
    }

    /**
     * Closes the communication with Finger Print reader
     */
    public void close(){
        Timber.d( "CLOSE");
        if (reader != null) {
            try {
                readerDevice.close();
            } catch (IBScanException e) {
                Timber.e("Issue during device close(exeception: " + e + ")");
                e.printStackTrace();
            }
        }
    }

    @Override
    public void captureFingerPrint(Listener listener) {
        this.listener = listener;
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

    /********************IB listener *******************/
    @Override
    public void deviceCommunicationBroken(IBScanDevice ibScanDevice) {

    }

    @Override
    public void deviceImagePreviewAvailable(IBScanDevice ibScanDevice, IBScanDevice.ImageData imageData) {
        /*
		 * Preserve aspect ratio of image while resizing.
		 */
        /*int dstWidth      = mainView.getFpWidth();
        int dstHeight     = mainView.getFpHeigth();
        int dstHeightTemp = (dstWidth * imageData.height) / imageData.width;
        if (dstHeightTemp > dstHeight)
        {
            dstWidth = (dstHeight * imageData.width) / imageData.height;
        }
        else
        {
            dstHeight = dstHeightTemp;
        }*/

		/*
		 * Display image result.
		 */
        listener.onImagePreviewAvailable(imageData.toBitmap());

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
    public void deviceImageResultAvailable(IBScanDevice ibScanDevice, IBScanDevice.ImageData imageData, IBScanDevice.ImageType imageType, IBScanDevice.ImageData[] imageData1) {
        if(listener != null) {
            listener.onAcquisitionCompleted(imageData.toBitmap());
        }
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
        listener.onReaderReady();

        try {
            IBScanDevice.ImageType imageType = IBScanDevice.ImageType.FLAT_SINGLE_FINGER;
            if ((readerDevice != null) && (readerDevice.isOpened())) {
                if (readerDevice.isCaptureAvailable(imageType, IBScanDevice.ImageResolution.RESOLUTION_500)) {
                    readerDevice.beginCaptureImage(imageType, IBScanDevice.ImageResolution.RESOLUTION_500,
                            IBScanDevice.OPTION_AUTO_CAPTURE | IBScanDevice.OPTION_AUTO_CONTRAST | IBScanDevice.OPTION_IGNORE_FINGER_COUNT);

                    readerDevice.setScanDeviceListener(this);

                }

            }
        }
        catch (IBScanException ibse)
        {
            Timber.e("Could not begin capturing Finger Print (exeception: " + ibse.getType() + ")");
        }

    }
}
