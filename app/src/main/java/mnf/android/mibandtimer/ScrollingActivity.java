package mnf.android.mibandtimer;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Typeface;
import android.os.Bundle;
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
import android.widget.TextView;

import com.github.lzyzsd.circleprogress.ArcProgress;
import com.philliphsu.bottomsheetpickers.time.BottomSheetTimePickerDialog;
import com.philliphsu.bottomsheetpickers.time.grid.GridTimePickerDialog;
import com.philliphsu.bottomsheetpickers.time.numberpad.NumberPadTimePickerDialog;
import com.shawnlin.numberpicker.NumberPicker;

import java.util.Timer;
import java.util.TimerTask;

import rjsv.circularview.CircleView;
import rjsv.circularview.CircleViewAnimation;
import rjsv.circularview.enumerators.AnimationStyle;

public class ScrollingActivity extends AppCompatActivity {

    Timer timer;
    ArcProgress ArcProgress;
    TextView timerView,tvMin,tvSec;
    NumberPicker secPicker,minPicker;
    boolean isRunnig = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);
        /*Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);*/
        ArcProgress = (com.github.lzyzsd.circleprogress.ArcProgress) findViewById(R.id.arc_progress);
        secPicker = (NumberPicker) findViewById(R.id.sec_picker);
        minPicker = (NumberPicker) findViewById(R.id.min_picker);
        tvMin = findViewById(R.id.min_tv);
        tvSec = findViewById(R.id.sec_tv);
        Typeface face = Typeface.createFromAsset(this.getAssets(), "fonts/Poppins-Regular.ttf");
        tvMin.setTypeface(face);
        tvSec.setTypeface(face);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();


               Log.e("TAG","time set for "+minPicker.getValue()+" mins "+ secPicker.getValue());
                 int sec = secPicker.getValue()*1000;
                int min = minPicker.getValue()*60*1000;
                int totalSec = sec+min;
                if(isRunnig){
                    isRunnig = false;
                    if(timer!=null){
                        timer.cancel();
                    }
                }else{
                    startTimer(totalSec);
                }

            }
        });

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

                    @Override
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
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
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
                        sendMessageToBand();

                    }
                });
            }
        }, 0, totalSec);
    }
    public void sendMessageToBand(){

    }
    public void connectMiBand(){

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


}
