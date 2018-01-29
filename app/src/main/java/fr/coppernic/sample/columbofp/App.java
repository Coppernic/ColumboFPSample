package fr.coppernic.sample.columbofp;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

/**
 * Created by michael on 26/01/18.
 */

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        LeakCanary.install(this);
    }
}
