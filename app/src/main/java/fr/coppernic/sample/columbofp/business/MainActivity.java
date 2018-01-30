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

import com.github.jorgecastilloprz.FABProgressCircle;

import fr.coppernic.sample.columbofp.R;
import fr.coppernic.sample.columbofp.presenter.MainPresenter;
import fr.coppernic.sample.columbofp.presenter.MainPresenterImpl;
import fr.coppernic.sample.columbofp.settings.PreferencesActivity;

public class MainActivity extends AppCompatActivity implements MainView {

    private ImageView fingerPrintImage;
    private MainPresenter mainPresenter;
    private FloatingActionButton fab;
    private FABProgressCircle fabProgressCircle;

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

        fabProgressCircle = findViewById(R.id.fabProgressCircle);

        fingerPrintImage = findViewById(R.id.imageFingerPrint);

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
        super.onStart();
        mainPresenter.setUp();
    }

    @Override
    protected void onStop() {
        mainPresenter.tearDown();
        stopProgress();
        super.onStop();
    }

    private void showSettings() {
        Intent intent = new Intent(this, PreferencesActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
    }

    @Override
    public void showFpImage(Bitmap fingerPrint) {
        fingerPrintImage.setImageBitmap(fingerPrint);
    }

    @Override
    public void startProgress() {
        fabProgressCircle.show();
        fab.setEnabled(false);
    }

    @Override
    public void stopProgress() {
        fab.setEnabled(true);
        fabProgressCircle.hide();
    }

    @Override
    public void showFAB(boolean value) {
        if (value) {
            fab.show();
        } else {
            fab.hide();
        }
    }


}
