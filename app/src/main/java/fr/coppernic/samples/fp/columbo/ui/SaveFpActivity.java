package fr.coppernic.samples.fp.columbo.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;

import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.integratedbiometrics.ibscanultimate.IBScanDevice;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fr.coppernic.sample.columbofp.R;
import timber.log.Timber;

public class SaveFpActivity extends AppCompatActivity {

    private MaterialDialog dialog;
    private Bitmap currentBitmap;
    private String selectedFinger;
    private String selectedHand;

    @BindView(R.id.save_fp_button)
    Button savefpbutton;
    @BindView(R.id.cancel_button)
    Button cancelfpbutton;
    @BindView(R.id.file_edit_text)
    EditText filenameText;
    @BindView(R.id.right_thumb_rb)
    RadioButton rightThumb;
    @BindView(R.id.right_forefinger_rb)
    RadioButton rightForefinger;
    @BindView(R.id.right_middlefinger_rb)
    RadioButton rightMiddleFinger;
    @BindView(R.id.right_ringfinger_rb)
    RadioButton rightRingFinger;
    @BindView(R.id.right_littlefinger_rb)
    RadioButton rightLittleFinger;
    @BindView(R.id.left_radio_group)
    RadioGroup leftRadioGroup;
    @BindView(R.id.right_radio_group)
    RadioGroup rightRadioGroup;
    @BindView(R.id.left_hand)
    RadioButton leftHand;
    @BindView(R.id.right_hand)
    RadioButton rightHand;
    @BindView(R.id.save_fp_preview)
    ImageView saveFpPreview;
    @BindView(R.id.handtextview)
    TextView handTextView;
    @BindView(R.id.fingertextview)
    TextView fingerTextView;
    @BindView(R.id.extentionTextView)
    TextView extensionTextView;
    @BindView(R.id.unknown_hand_rb)
    RadioButton unknownHandButton;
    @BindView(R.id.unknow_finger_rb)
    RadioButton unknownFingerButton;

    String fileName = filenameText.getText().toString();
    String extension = extensionTextView.getText().toString();
    String handName = handTextView.getText().toString();
    String fingerName = fingerTextView.getText().toString();
    String userFilename = handName + fingerName + fileName + extension;


    private final MaterialDialog.SingleButtonCallback positive = new MaterialDialog.SingleButtonCallback() {
        @Override
        public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
            Timber.v("OnPositive");
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.savefp_activity);
        ButterKnife.bind(this);
        byte[] byteArray = getIntent().getByteArrayExtra("Fp");
        currentBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        showFpImage(currentBitmap);

        rightRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.right_thumb_rb) {
                    selectedFinger = rightThumb.getText().toString();
                    fingerTextView.setText(selectedFinger);
                } else if (checkedId == R.id.right_forefinger_rb) {
                    selectedFinger = rightForefinger.getText().toString();
                    fingerTextView.setText(selectedFinger);
                } else if (checkedId == R.id.right_middlefinger_rb) {
                    selectedFinger = rightMiddleFinger.getText().toString();
                    fingerTextView.setText(selectedFinger);
                } else if (checkedId == R.id.right_ringfinger_rb) {
                    selectedFinger = rightRingFinger.getText().toString();
                    fingerTextView.setText(selectedFinger);
                } else if (checkedId == R.id.right_littlefinger_rb) {
                    selectedFinger = rightLittleFinger.getText().toString();
                    fingerTextView.setText(selectedFinger);
                } else if (checkedId == R.id.unknow_finger_rb) {
                    selectedFinger = unknownFingerButton.getText().toString();
                    fingerTextView.setText(selectedFinger);
                }
            }
        });

        leftRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.left_hand) {
                    selectedHand = leftHand.getText().toString();
                    handTextView.setText(selectedHand);
                } else if (checkedId == R.id.right_hand) {
                    selectedHand = rightHand.getText().toString();
                    handTextView.setText(selectedHand);
                } else if (checkedId == R.id.unknown_hand_rb) {
                    selectedFinger = unknownHandButton.getText().toString();
                    fingerTextView.setText(selectedFinger);
                }
            }
        });
    }

    @OnClick(R.id.cancel_button)
    void cancelview() {
        finish();
    }

    @OnClick(R.id.save_fp_button)
    void saveFP() {

        BitmapDrawable bitmapDrawable = (BitmapDrawable) saveFpPreview.getDrawable();
        Bitmap bitmapFp = bitmapDrawable.getBitmap();

        if (saveFpPreview.getDrawable() != null && !isExternalStorageAvailable() && isChecked()) {
            Timber.d("External Storage Unavailable");
            saveToInternalStorage(bitmapFp);

        } else if (saveFpPreview.getDrawable() != null && isExternalStorageAvailable() && isChecked()) {
            Timber.d("External Storage Available");
            saveToExternalStorage(bitmapFp);

        } else if (saveFpPreview.getDrawable() != null && !isExternalStorageAvailable() && !isChecked()) {
            Timber.d("No finger Selected");
            MaterialDialog.Builder builder = new MaterialDialog.Builder(this);
            dialog = builder.title(R.string.error_title_save)
                    .customView(R.layout.dialog_savefp_error, true)
                    .cancelable(true)
                    .positiveText(android.R.string.ok)
                    .onPositive(positive)
                    .show();
        }
    }

    public void showFpImage(Bitmap fingerPrint) {
        saveFpPreview.setImageBitmap(fingerPrint);
    }

    public boolean isChecked() {
        return rightRadioGroup.getCheckedRadioButtonId() != -1 || leftRadioGroup.getCheckedRadioButtonId() != -1;
    }

    private void saveToInternalStorage(Bitmap bitmapImage) {

        File storageLoc = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File fpSave = new File(storageLoc, userFilename);
        String pathInternal = getString(R.string.success_save_internal) + storageLoc + userFilename;
        String storagePath = getString(R.string.external_device_unavailable) + storageLoc;
        FileOutputStream outputStream = null;
        Toast.makeText(this, storagePath, Toast.LENGTH_LONG).show();

        if (!fpSave.exists()) {
            Timber.d("path%s", storageLoc.toString());
        }
        try {
            //imageData.saveToFile(storageLoc, fileFormat);
            outputStream = new FileOutputStream(fpSave);
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
            Intent passIntent = new Intent(SaveFpActivity.this, MainActivity.class);
            passIntent.putExtra("path", pathInternal);
            startActivity(passIntent);
            Timber.d("Image internally save");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveToExternalStorage(Bitmap bitmapImage) {

        File storageExternalLoc = Environment.getExternalStorageDirectory();
        File fpSave = new File(storageExternalLoc, userFilename);
        FileOutputStream outputStream = null;
        String pathExternal = storageExternalLoc + userFilename;

        String storageExternalPath = getString(R.string.external_device_available) + storageExternalLoc;
        Toast.makeText(this, storageExternalPath, Toast.LENGTH_LONG).show();

        if (!fpSave.exists()) {
            Timber.d(storageExternalLoc.toString());
        }
        try {
            //imageData.saveToFile(storageExternalLoc, fileFormat);
            outputStream = new FileOutputStream(fpSave);
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
            Intent passIntent = new Intent(SaveFpActivity.this, MainActivity.class);
            passIntent.putExtra("Success", pathExternal);
            startActivity(passIntent);
            Timber.d("Store ImageView");
        } catch (FileNotFoundException e) {
            Timber.d("File Not Found");
            e.printStackTrace();
        } catch (IOException e) {
            Timber.d("IO Exception");
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        return !Environment.MEDIA_MOUNTED.equals(extStorageState);
    }

    private static boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState);
    }


    public void createInternalFolder() {
        final File f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "FpCapture");
        if (!f.exists()) {
            Timber.d("Folder doesn't exist, creating it...");
            boolean rv = f.mkdir();
            Timber.d("Folder creation %s", (rv ? "success" : "failed"));
        } else {
            Timber.d("Folder already exists.");
        }
    }


    public void createExternalFolder() {
        final File f = new File(Environment.getExternalStorageDirectory(), "FpCapture");
        if (!f.exists()) {
            Timber.d("Folder doesn't exist, creating it...");
            boolean rv = f.mkdir();
            Timber.d("Folder creation %s", (rv ? "success" : "failed"));
        } else {
            Timber.d("Folder already exists");
        }
    }


}
