package mnf.android.mibandtimer;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
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
    Context c;
    int premiumCounter = 0;
    Button btnPro,cntBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);
        /*Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);*/
        c = this;

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        bp = new BillingProcessor(this, Config.base64, this);

        helper = new BLEMiBand2Helper(ScrollingActivity.this, handler);
        helper.addListener(this);
        pref = new PreferenceHandler(this);
        ArcProgress = (com.github.lzyzsd.circleprogress.ArcProgress) findViewById(R.id.arc_progress);
        secPicker =  findViewById(R.id.sec_picker);
        minPicker =  findViewById(R.id.min_picker);
        connStatus = findViewById(R.id.con_status_id);
        btnPro = findViewById(R.id.pro_btn);
        tvMin = findViewById(R.id.min_tv);
        tvSec = findViewById(R.id.sec_tv);
        macAddr = findViewById(R.id.mac_add_tv);
        edtMac = findViewById(R.id.mac_edt);
        cntBtn = findViewById(R.id.mi_connect_btn);
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


        btnPro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //bp.purchase(ScrollingActivity.this, Config.productIdAds);
            }
        });
        cntBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(edtMac.getText()!=null){
                    if(!edtMac.equals("")){
                        String macAd = edtMac.getText().toString();
                        if(pref!=null){
                            pref.setMacAddress(macAd);
                        }
                        connectBand();

                    }
                }
            }
        });


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
                    premiumCounter++;
                    if(premiumCounter>Config.PREMIUM_DIALOGUE_LIMIT){
                        showPremiumDialogue();
                        premiumCounter = 0;
                    }
                    Log.e("TAG", " pemium counter limit = "+premiumCounter);
                }else{
                    fab.setImageResource(android.R.drawable.ic_media_pause);
                    if(totalSec!=0)
                    startTimer(totalSec);
                    else{
                        Snackbar.make(view, "Select something more than zero. ", Snackbar.LENGTH_LONG)
                                .setAction("", null).show();
                    }
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
        //helper.alerta();
    }
    public void connectBand(){
        Log.e("MiBand","mi band connection fun");
        if(helper!=null){
            Log.e("MiBan"," helper is not null ");
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
            if(pref!=null){
                macAddr.setText(pref.getMacAddress());
            }
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
        Log.e("bill","onProductPurchased product id= "+productId);
        Log.e("bill","onProductPurchased transation details = "+details.purchaseToken);
        if(pref!=null){
            pref.setUserPaidOrNot(true);
            setProVisibility(pref);
        }
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

    public void showPremiumDialogue() {

        new MaterialDialog.Builder(c)
                .title(R.string.upgrade_pro)
                .content(R.string.upgrade_desc)
                .positiveText(R.string.upgrade)
                .negativeText(R.string.cancel)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if(bp!=null){
                            //bp.purchase(ScrollingActivity.this, Config.productIdAds);
                        }
                    }
                })
                .icon(c.getResources().getDrawable(R.mipmap.ic_launcher))
                .show();


    }


    public void setProVisibility(PreferenceHandler pref){



    }
}
