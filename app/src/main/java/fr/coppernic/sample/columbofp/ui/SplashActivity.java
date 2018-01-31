package fr.coppernic.sample.columbofp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import fr.coppernic.sample.columbofp.business.MainActivity;

/**
 * Created by michael on 31/01/18.
 */

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Start home activity
        startActivity(new Intent(this, MainActivity.class));
        // close splash activity
        finish();
    }
}
