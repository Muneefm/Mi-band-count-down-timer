package mnf.android.mibandtimer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Created by muneef on 03/09/17.
 */

public class PreferenceHandler {

    SharedPreferences pref;

    SharedPreferences.Editor editor;
    Context c;
    int PRIVATE_MODE = 0;
    private static final String PREF_NAME = "settings_pref";
    final String count_down_time = "apptheme";
    final String click_count = "clicks";
    final String first_time_user = "first_time_user_timer";

    final String mac_address = "mac_address_timer";


    @SuppressLint("CommitPrefEdits")
    public PreferenceHandler(Context context) {
        this.c = context;
        pref = c.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void setThemeDark(String var){
        editor.putString(count_down_time, var);
        editor.commit();
    }

    public String getThemeDark(){
        return pref.getString(count_down_time, "");
    }

    public void setMacAddress(String var){
        editor.putString(mac_address, var);
        editor.commit();
    }

    public String getMacAddress(){
        return pref.getString(mac_address, "");
    }


    public void setFirstTimeUser(Boolean var){
        editor.putBoolean(first_time_user, var);
        editor.commit();
    }

    public Boolean getFirstTimeUser(){
        return pref.getBoolean(first_time_user, true);
    }




}
