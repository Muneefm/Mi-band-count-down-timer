package mnf.android.mibandtimer;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.google.android.gms.ads.MobileAds;

/**
 * Created by muneef on 06/09/17.
 */

public class AppController extends Application {

     static Context c;
    @Override
    public void onCreate() {
        super.onCreate();
        c = this;
        MobileAds.initialize(this, "ca-app-pub-7269223551241818~1559479284");
    }

    public static Context getInstance(){
        return c;
    }
    public void sendMessageToBand(){

    }


}
