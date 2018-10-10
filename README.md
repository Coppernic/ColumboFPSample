# ColumboFPSample
Sample application for Fingerprint reader on C-One e-ID

## Prerequisites

For C-One e-ID, CpcSystemServices shall be installed on your device.
For C-One e-ID 2, Core Services must be installed on your device.

Please install the last version available on FDroid available on www.coppernic.fr/fdroid.apk


## Set up

### build.gradle

```groovy
repositories {
    jcenter()
    maven { url 'https://artifactory.coppernic.fr/artifactory/libs-release' }
}


dependencies {
// [...]
    // Coppernic
    implementation(group: 'fr.coppernic.sdk.cpcutils', name: 'CpcUtilsLib', version: '6.13.0', ext: 'aar')
    implementation 'fr.coppernic.sdk.core:CpcCore:1.3.0'
// [...]
}

```

### Runtime permissions 

For C-One e-ID 2 only, it is mandatory to handle runtime permission for fingerprint reader.

 * Add permission to AndroidManifest.xml
 
```xml

<uses-permission android:name="fr.coppernic.permission.FINGER_PRINT" />

```

 * Check if permission has been granted and ask for it if not
 
``` java

private static final String FINGER_PRINT_PERMISSION = "fr.coppernic.permission.FINGER_PRINT";
private static final int REQUEST_PERMISSION_CODE = 28;

...

if (!checkPermission()) {
    requestPermission();
} else {
    // OK
}

private boolean checkPermission() {
    if (CpcOs.isConeN()) {
        return ContextCompat.checkSelfPermission(this, FINGER_PRINT_PERMISSION) == PackageManager.PERMISSION_GRANTED;
    } else {
        return true;
    }
}

private void requestPermission() {
    if (ActivityCompat.shouldShowRequestPermissionRationale(this,
            FINGER_PRINT_PERMISSION)) {
        // For this sample we do not display rationale, we just ask for permission if not granted
        ActivityCompat.requestPermissions(this,
                new String[]{FINGER_PRINT_PERMISSION},
                REQUEST_PERMISSION_CODE);
    } else {
        // No explanation needed; request the permission
        ActivityCompat.requestPermissions(this,
                new String[]{FINGER_PRINT_PERMISSION},
                REQUEST_PERMISSION_CODE);
    }
}

```

 * Finally check request result
 
```java

@Override
public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    switch (requestCode) {
        case REQUEST_PERMISSION_CODE: {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // PErmission granted
            } else {
                // Permission rejected
                
            }
        }
    }
}

```

More information on [Android Developers](https://developer.android.com/training/permissions/requesting)

### Power management

 * Implements power listener

```java

  private final PowerListener powerListener = new PowerListener() {
        @Override
        public void onPowerUp(CpcResult.RESULT result, Peripheral peripheral) {
            if (result == CpcResult.RESULT.OK) {
                //FP reader is ON
            }
            else{
                //Error while powering fingerprint
            }
        }

        @Override
        public void onPowerDown(CpcResult.RESULT result, Peripheral peripheral) {
           //FP reader power off
        }
    };

```

 * Register the listener

```java
@Override
    protected void onStart() {
// [...]
        PowerManager.get().registerListener(powerListener);
// [...]
    }
```

 * Power reader on

```java
// Powers on Fingerprint reader
ConePeripheral.FP_IB_COLOMBO_USB.on(this);
// The listener will be called with the result
```

 * Power off when you are done

```java
// Powers off OCR reader
ConePeripheral.FP_IB_COLOMBO_USB.off(this);
// The listener will be called with the result
```

 * unregister listener resources

```java
@Override
    protected void onStop() {
// [...]
        PowerManager.get().unregisterListener(powerListener);
// [...]
    }
```

### Reader initialization

#### Create reader object
 * Declare a Reader and readerDevice object

```java
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
```
 * Create a listener 
 
```java
    /********************IB listener *******************/
    IBScanListener ibScanListener = new IBScanListener() {
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
            //Retrieve your ibScanDevice object here
            Timber.d("scanDeviceOpenComplete");
            readerDevice = ibScanDevice;
        }
    };
    
```
 * Instantiate Fingerprint object

```java

 private void initializeIbScanClass() {
            reader = IBScan.getInstance(ctx);
            reader.setContext(ctx);
            reader.setScanListener(ibScanListener);
            try {
                IBScan.DeviceDesc deviceDesc = reader.getDeviceDescription(0);
            } catch (IBScanException e) {
                e.printStackTrace();
                Timber.e(e.getMessage());
            }
        }
    }

```

### Get Fingerprint image

 * When your fingerprint is initialized and opened. Set scanDevice listener to your readerDevice
 
```java

//[...]

readerDevice.setScanDeviceListener(ibScanListener);

//[...]


private IBScanDeviceListener ibScanListener = new IBScanDeviceListener() {
        @Override
        public void deviceCommunicationBroken(IBScanDevice ibScanDevice) {
            Timber.d("deviceCommunicationBroken");
        }

        @Override
        public void deviceImagePreviewAvailable(IBScanDevice ibScanDevice, final IBScanDevice.ImageData imageData) {
            //display preview image here
            //get bitmap with imageData.toBitmap()
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
            Timber.d("deviceAcquisitionCompleted");
        }

        @Override
        public void deviceImageResultAvailable(IBScanDevice ibScanDevice, final IBScanDevice.ImageData imageData, IBScanDevice.ImageType imageType, IBScanDevice.ImageData[] imageData1) {
            Timber.d("deviceImageResultAvailable");
            //display imageData.toBitmap() ;
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
    };
```

