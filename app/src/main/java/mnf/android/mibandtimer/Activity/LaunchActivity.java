package mnf.android.mibandtimer.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.FloatRange;
import android.view.View;

import agency.tango.materialintroscreen.MaterialIntroActivity;
import agency.tango.materialintroscreen.SlideFragmentBuilder;
import agency.tango.materialintroscreen.animations.IViewTranslation;
import agency.tango.materialintroscreen.animations.ViewTranslationWrapper;

import mnf.android.mibandtimer.Fragments.LastSlide;
import mnf.android.mibandtimer.PreferenceHandler;
import mnf.android.mibandtimer.R;
import mnf.android.mibandtimer.ScrollingActivity;

public class LaunchActivity extends MaterialIntroActivity {

    Context c;
    PreferenceHandler pref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        c = this;
        pref = new PreferenceHandler(c);
        if(!pref.getFirstTimeUser()){
            if(!getIntent().hasExtra("show_intro")) {
                Intent mainAct = new Intent(LaunchActivity.this, ScrollingActivity.class);
                startActivity(mainAct);
                finish();
            }
        }


       /* new DeviceAdmin().setDeviceAdminCallback(new DeviceAdminCallback() {
            @Override
            public void onAdminEnabled() {

                Intent mainAct = new Intent(LaunchActivity.this,PreferanceActivity.class);
                startActivity(mainAct);
                finish();
            }

            @Override
            public void onAdminDisabled() {

            }
        });
*/
        enableLastSlideAlphaExitTransition(true);

        getBackButtonTranslationWrapper()
                .setEnterTranslation(new IViewTranslation() {
                    @Override
                    public void translate(View view, @FloatRange(from = 0, to = 1.0) float percentage) {
                        view.setAlpha(percentage);
                    }
                });

        addSlide(new SlideFragmentBuilder()
                        .backgroundColor(R.color.cyan500)
                        .buttonsColor(R.color.cyan900)
                        .image(R.drawable.intro)

                        .title("Count down for Mi band 2! \n")
                        .description(getString(R.string.launch_string_one))
                        .build());


/*
        addSlide(new SlideFragmentBuilder()
                .backgroundColor(R.color.teal400)
                .buttonsColor(R.color.teal500)
                .image(R.drawable.ring)
                .title(" Silent your phone calls ! \n")
                .description(getString(R.string.launch_desc_two))
                .build());*/

        /*addSlide(new SlideFragmentBuilder()
                .backgroundColor(R.color.teal400)
                .buttonsColor(R.color.teal500)

                .image(R.mipmap.intro_lock)

                .title("Lock your device Remotely !")
                .description(getString(R.string.launch_desc_two))
                .build());
*/
        addSlide(new LastSlide());

        /*addSlide(new SlideFragmentBuilder()
                        .backgroundColor(R.color.curious_blue)
                        .buttonsColor(R.color.blue800)
                        .image(R.mipmap.icon)
                        .title("Please provide admin Permission.")
                        .description(getString(R.string.launch_desc_three))
                        .build(),
                new MessageButtonBehaviour(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!new ApplicationController().isAdminActive()) {
                            new ApplicationController().enableDeviceAdmin(c);
                        }else{
                            Intent mainAct = new Intent(LaunchActivity.this,PreferanceActivity.class);
                            startActivity(mainAct);
                        }
                    }
                }, "Give Admin Privilege"));
*/
   /*     addSlide(new SlideFragmentBuilder()
                .backgroundColor(R.color.pink800)
                .buttonsColor(R.color.grey500)
                .title("That's it")
                .description("Would you join us?")
                .build());
*/




    }

    @Override
    public ViewTranslationWrapper getNextButtonTranslationWrapper() {
        return super.getNextButtonTranslationWrapper();
    }
    @Override
    public void onFinish() {
        /*if(new ApplicationController().isAdminActive()) {
            Intent intent = new Intent(LaunchActivity.this,PreferanceActivity.class);
            startActivity(intent);
        }else{
           // showMessage("Please provide admin privilege ");
            new ApplicationController().enableDeviceAdmin(c);

        }*/

        Intent intent = new Intent(LaunchActivity.this,ScrollingActivity.class);
        startActivity(intent);


    }

}
