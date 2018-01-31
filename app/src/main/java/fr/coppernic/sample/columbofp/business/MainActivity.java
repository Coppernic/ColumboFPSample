package fr.coppernic.sample.columbofp.business;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import fr.coppernic.sample.columbofp.R;
import fr.coppernic.sample.columbofp.presenter.MainPresenter;
import fr.coppernic.sample.columbofp.presenter.MainPresenterImpl;
import fr.coppernic.sample.columbofp.settings.PreferencesActivity;
import dmax.dialog.SpotsDialog;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements MainView {

    private ImageView fingerPrintImage;
    private MainPresenter mainPresenter;
    private FloatingActionButton fab;
    private SpotsDialog spotsDialog;
    private TextView tvMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainPresenter.captureFingerPrint();
            }
        });

        fingerPrintImage = findViewById(R.id.imageFingerPrint);

        tvMessage = findViewById(R.id.tvMessage);

        mainPresenter = new MainPresenterImpl(this);
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
    protected void onStart() {
        Timber.d("onStart");
        super.onStart();
        mainPresenter.setUp();
    }

    @Override
    protected void onStop() {
        Timber.d("onStop");
        mainPresenter.tearDown();
        stopProgress();
        super.onStop();
    }

    @Override
    public void showFpImage(Bitmap fingerPrint) {
        fingerPrintImage.setImageBitmap(fingerPrint);
    }

    @Override
    public void startProgress() {
        dismissSpots();
        fab.setEnabled(false);
        spotsDialog = new SpotsDialog(this, R.style.ProgressDialog);
        spotsDialog.show();
        spotsDialog.setMessage(getString(R.string.opening_FP_reader));
    }

    @Override
    public void stopProgress() {
        fab.setEnabled(true);
        dismissSpots();
    }

    @Override
    public void showFAB(boolean value) {
        if (value) {
            fab.show();
        } else {
            fab.hide();
        }
    }

    @Override
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

}
