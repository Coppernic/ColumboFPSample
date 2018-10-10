package fr.coppernic.sample.columbofp.ui;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dmax.dialog.SpotsDialog;
import fr.coppernic.sample.columbofp.R;
import fr.coppernic.sample.columbofp.fingerprint.FingerPrint;
import fr.coppernic.sample.columbofp.fingerprint.IBScanFingerPrint;
import fr.coppernic.sample.columbofp.settings.PreferencesActivity;
import fr.coppernic.sdk.power.PowerManager;
import fr.coppernic.sdk.power.api.PowerListener;
import fr.coppernic.sdk.power.api.peripheral.Peripheral;
import fr.coppernic.sdk.power.impl.cone.ConePeripheral;
import fr.coppernic.sdk.utils.core.CpcResult;
import fr.coppernic.sdk.utils.helpers.CpcOs;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    private static final String FINGER_PRINT_PERMISSION = "fr.coppernic.permission.FINGER_PRINT";
    private static final int REQUEST_PERMISSION_CODE = 28;

    @BindView(R.id.fab)
    FloatingActionButton fab;
    @BindView(R.id.tvMessage)
    TextView tvMessage;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.imageFingerPrint)
    ImageView fingerPrintImage;

    private FingerPrint fingerprintReader;
    private SpotsDialog spotsDialog;

    private final PowerListener powerListener = new PowerListener() {
        @Override
        public void onPowerUp(CpcResult.RESULT result, Peripheral peripheral) {
            if (result == CpcResult.RESULT.OK) {
                Timber.d("Fp reader powered on");
                fingerprintReader.setUp();
                showFAB(true);
            }
            else{
                showMessage(getString(R.string.error_power_on));
            }
        }

        @Override
        public void onPowerDown(CpcResult.RESULT result, Peripheral peripheral) {
            Timber.d("Fp reader powered off");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        showFAB(false);

        fingerprintReader = new IBScanFingerPrint(this, fpListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            showSettings();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        Timber.d("onStart");
        super.onStart();
        showFAB(false);
        PowerManager.get().registerListener(powerListener);

        if (!checkPermission()) {
            requestPermission();
        } else {
            powerOn(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    powerOn(true);
                } else {
                    // For this sample, we ask permission again
                    requestPermission();
                }
            }
        }
    }

    @Override
    protected void onStop() {
        Timber.d("onStop");
        fingerprintReader.tearDown();
        powerOn(false);
        PowerManager.get().unregisterListener(powerListener);
        stopProgress();
        super.onStop();
    }

    @OnClick(R.id.fab)
    void captureFP(){
        startProgress();
        fingerprintReader.capture();
    }


    private void powerOn(boolean on) {
        if (on) {
            ConePeripheral.FP_IB_COLOMBO_USB.on(this);
        } else {
            ConePeripheral.FP_IB_COLOMBO_USB.off(this);
        }
    }


    public void showFpImage(Bitmap fingerPrint) {
        fingerPrintImage.setImageBitmap(fingerPrint);
    }


    public void startProgress() {
        dismissSpots();
        fab.setEnabled(false);
        spotsDialog = new SpotsDialog(this, R.style.ProgressDialog);
        spotsDialog.show();
        spotsDialog.setMessage(getString(R.string.opening_FP_reader));
    }


    public void stopProgress() {
        fab.setEnabled(true);
        dismissSpots();
    }


    public void showFAB(boolean value) {
        if (value) {
            fab.show();
            showMessage(getString(R.string.press_FP_button));
        } else {
            showMessage(getString(R.string.wait_FP_powered));
            fab.hide();
        }
    }


    public void showMessage(String value) {
        tvMessage.setText(value);
    }

    private void dismissSpots() {
        if (spotsDialog != null) {
            spotsDialog.dismiss();
        }
    }

    private void showSettings() {
        Intent intent = new Intent(this, PreferencesActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
    }

    FingerPrint.Listener fpListener = new FingerPrint.Listener() {
        @Override
        public void onReaderReady(CpcResult.RESULT res) {
            stopProgress();
            if (res != CpcResult.RESULT.OK) {//init reader failed
                showMessage(getString(R.string.FP_opened_error));
            }
        }

        @Override
        public void onAcquisitionCompleted(Bitmap fingerPrint) {
            showFpImage(fingerPrint);
            fingerprintReader.stopCapture();
        }
    };

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
}