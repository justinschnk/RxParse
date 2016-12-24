package rx.parse.app;

import android.app.Application;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.parse.Parse;

//import android.support.multidex.MultiDexApplication;
//import timber.log.Timber;
//import com.sromku.simple.fb.Permission;
//import com.sromku.simple.fb.SimpleFacebook;
//import com.sromku.simple.fb.SimpleFacebookConfiguration;

public class RxParseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        //Timber.plant(new Timber.DebugTree());

        //ParseACL defaultACL = new ParseACL();
        //ParseACL.setDefaultACL(defaultACL, true);

        //Parse.enableLocalDatastore(getApplicationContext());

        Parse.initialize(this, getString(R.string.parse_app_id), getString(R.string.parse_client_key));
        //ParseInstallation.getCurrentInstallation().saveInBackground();

        //ParseFacebookUtils.initialize(getString(R.string.app_id));
        //
        //ParseInstallation.getCurrentInstallation().saveInBackground();

        Fresco.initialize(this);
    }
}
