package mnf.android.mibandtimer;

import android.app.Application;
import android.content.Context;
import android.util.Log;

/**
 * Created by muneef on 06/09/17.
 */

public class AppController extends Application {

     static Context c;
    @Override
    public void onCreate() {
        super.onCreate();
        c = this;
    }

    public static Context getInstance(){
        return c;
    }
    public void sendMessageToBand(){

    }


}
