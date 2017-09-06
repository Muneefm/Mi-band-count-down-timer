package mnf.android.mibandtimer.Fragments;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import agency.tango.materialintroscreen.SlideFragment;
import mnf.android.mibandtimer.PreferenceHandler;
import mnf.android.mibandtimer.R;

/**
 * Created by muneef on 12/06/17.
 */

public class LastSlide extends SlideFragment {
    TextView tvMacTag;
    EditText edtMac;
    PreferenceHandler pref;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.custom_slider, container, false);
        // = (CheckBox) view.findViewById(R.id.checkBox);
        Log.e("TAG","onCreateView custom fragment");
        tvMacTag = (TextView) view.findViewById(R.id.mac_tag);
        edtMac = (EditText) view.findViewById(R.id.edt_mac);
        Typeface face = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Jaldi-Regular.ttf");
        tvMacTag.setTypeface(face);
        pref = new PreferenceHandler(getContext());
        return view;
    }


    @Override
    public int backgroundColor() {
        return R.color.light_blue800;
    }


    @Override
    public int buttonsColor() {
        return R.color.light_blue900;
    }

    @Override
    public boolean canMoveFurther() {
       /* if(new ApplicationController().isAdminActive()) {
            return true;
        }else return false;*/
        Log.e("TAG","can move further");
       if(edtMac.getText()!=null){
           if(!edtMac.getText().toString().equals("")){
               Log.e("TAG"," yes can move further  = "+edtMac.getText().toString());
                if(pref!=null){
                    pref.setMacAddress(edtMac.getText().toString());
                }
               return true;

           }else{
               return false;
           }
       }else{
           return false;
       }
    }

    @Override
    public String cantMoveFurtherErrorMessage() {
        return "Please enter MiBand MAC address to continue ";
    }

}
