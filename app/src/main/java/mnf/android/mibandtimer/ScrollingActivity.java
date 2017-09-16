package mnf.android.mibandtimer;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.EditText;
import android.widget.TextView;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.github.lzyzsd.circleprogress.ArcProgress;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.philliphsu.bottomsheetpickers.time.BottomSheetTimePickerDialog;
import com.philliphsu.bottomsheetpickers.time.grid.GridTimePickerDialog;
import com.philliphsu.bottomsheetpickers.time.numberpad.NumberPadTimePickerDialog;
import com.shawnlin.numberpicker.NumberPicker;

import java.util.Timer;
import java.util.TimerTask;

import mnf.android.mibandtimer.Misc.BLEMiBand2Helper;
import mnf.android.mibandtimer.Misc.Config;
import rjsv.circularview.CircleView;
import rjsv.circularview.CircleViewAnimation;
import rjsv.circularview.enumerators.AnimationStyle;

public class ScrollingActivity extends AppCompatActivity implements BLEMiBand2Helper.BLEAction, BillingProcessor.IBillingHandler
{

    Timer timer;
    ArcProgress ArcProgress;
    TextView timerView,tvMin,tvSec,connStatus,macAddr;
    EditText edtMac;
    NumberPicker secPicker,minPicker;
    boolean isRunnig = false;

    Handler handler = new Handler(Looper.getMainLooper());
    static BLEMiBand2Helper helper = null;
    PreferenceHandler pref;
    FloatingActionButton fab;
    private InterstitialAd mInterstitialAd;
    int cycle = 0;
    BillingProcessor bp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);
        /*Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);*/
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        bp = new BillingProcessor(this, Config.base64, this);

        helper = new BLEMiBand2Helper(ScrollingActivity.this, handler);
        helper.addListener(this);
        pref = new PreferenceHandler(this);
        ArcProgress = (com.github.lzyzsd.circleprogress.ArcProgress) findViewById(R.id.arc_progress);
        secPicker = (NumberPicker) findViewById(R.id.sec_picker);
        minPicker = (NumberPicker) findViewById(R.id.min_picker);
        connStatus = findViewById(R.id.con_status_id);
        tvMin = findViewById(R.id.min_tv);
        tvSec = findViewById(R.id.sec_tv);
        macAddr = findViewById(R.id.mac_add_tv);
        edtMac = findViewById(R.id.mac_edt);
        Typeface face = Typeface.createFromAsset(this.getAssets(), "fonts/Poppins-Regular.ttf");
        tvMin.setTypeface(face);
        tvSec.setTypeface(face);
        connStatus.setTypeface(face);
        macAddr.setTypeface(face);
        if(pref.getMacAddress()!=null){
            macAddr.setText(pref.getMacAddress());
            edtMac.setText(pref.getMacAddress());
            connectBand();
        }

         fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Timer started", Snackbar.LENGTH_LONG)
                        .setAction("", null).show();


               Log.e("TAG","time set for "+minPicker.getValue()+" mins "+ secPicker.getValue());
                 int sec = secPicker.getValue()*1000;
                int min = minPicker.getValue()*60*1000;
                int totalSec = sec+min;
                if(isRunnig){
                    isRunnig = false;
                    fab.setImageResource(android.R.drawable.ic_media_play);
                    if(timer!=null){
                        timer.cancel();
                    }
                    if (mInterstitialAd.isLoaded()) {
                        mInterstitialAd.show();
                    } else {
                        Log.d("TAG", "The interstitial wasn't loaded yet.");
                    }
                }else{
                    fab.setImageResource(android.R.drawable.ic_media_pause);
                    startTimer(totalSec);
                }

            }
        });
        if(isRunnig){
            fab.setImageResource(android.R.drawable.ic_media_pause);
        }else{
            fab.setImageResource(android.R.drawable.ic_media_play);
        }

        minPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                 Log.e("TAG","time set for min "+newVal);

            }
        });
        secPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                Log.e("TAG","time set for sec "+newVal);

            }
        });

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                Log.i("Ads", "onAdLoaded");
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                // Code to be executed when an ad request fails.
                Log.i("Ads", "onAdFailedToLoad");
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when the ad is displayed.
                Log.i("Ads", "onAdOpened");
            }

            @Override
            public void onAdLeftApplication() {
                // Code to be executed when the user has left the app.
                Log.i("Ads", "onAdLeftApplication");
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when when the interstitial ad is closed.
                Log.i("Ads", "onAdClosed");
                mInterstitialAd.loadAd(new AdRequest.Builder().build());

            }
        });

     /*   CircleView circleView = (CircleView) findViewById(R.id.circle_view);
        CircleViewAnimation circleViewAnimation = new CircleViewAnimation(circleView)
                .setAnimationStyle(AnimationStyle.CONTINUOUS)
                .setDuration(100)
                .setCustomAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        // Animation Starts
                        Log.e("TAG","on animation start");
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        // Animation Ends
                        Log.e("TAG","on animation end");

                    }

                    @Overridewd
                    public void onAnimationRepeat(Animation animation) {
                        Log.e("TAG","on animation onAnimationRepeat");

                    }
                }).setTimerOperationOnFinish(new Runnable() {
                    @Override
                    public void run() {
                        // Run when the duration reaches 0. Regardless of the AnimationLifecycle or main thread.
                        // Runs and triggers on background.
                    }
                })
                .setCustomInterpolator(new LinearInterpolator());
        circleView.startAnimation(circleViewAnimation);*/
    }

    public void startTimer(final int totalSec){
        ArcProgress.setMax(totalSec/1000);
        timer = new Timer();
          cycle = 0;
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        cycle++;
                        Log.e("TAG","inside run");
                        isRunnig = true;
                        ObjectAnimator anim = ObjectAnimator.ofInt(ArcProgress, "progress",0, totalSec/1000);
                        anim.setInterpolator(new LinearInterpolator());
                        anim.setDuration(totalSec);
                        anim.start();
                           /* AnimatorSet set = (AnimatorSet) AnimatorInflater.loadAnimator(ScrollingActivity.this, R.animator.progress_anim);
                            set.setInterpolator(new DecelerateInterpolator());
                            set.setTarget(donutProgress);
                           set.start();*/
                        sendMessageToBand("cycle - "+cycle);

                    }
                });
            }
        }, 0, totalSec);
    }

    public void sendMessageToBand(String msg){
        helper.sendSms(msg);
    }
    public void connectBand(){
        Log.e("MiBand","mi band connection fun");
        if(helper!=null){
            Log.e("MiBand"," helper is not null ");
            helper.connect();
        }else{
            Log.e("MiBand"," helper is  null ");
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scrolling, menu);
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
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onDisconnect() {
        Log.e("MiBand","onDisconnect ");
        if(connStatus != null){
            connStatus.setText(R.string.band_not_connected);
        }
    }

    @Override
    public void onConnect() {
        Log.e("MiBand","onConnect ");
        if(connStatus != null){
            connStatus.setText(R.string.band_connected);
        }

    }

    @Override
    public void onRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {

    }

    @Override
    public void onWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {

    }

    @Override
    public void onNotification(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {

    }




    @Override
    public void onProductPurchased(@NonNull String productId, @Nullable TransactionDetails details) {
        Log.e("TAG","onProductPurchased product id = "+productId);
    }

    @Override
    public void onPurchaseHistoryRestored() {

    }

    @Override
    public void onBillingError(int errorCode, @Nullable Throwable error) {
        Log.e("TAG","onBillingError error code  = "+errorCode);

    }

    @Override
    public void onBillingInitialized() {

    }
}
