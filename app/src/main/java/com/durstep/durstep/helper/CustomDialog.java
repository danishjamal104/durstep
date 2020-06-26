package com.durstep.durstep.helper;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.durstep.durstep.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

public class CustomDialog {

    Dialog dialog;
    Context c;

    TextView title, body;
    TextInputLayout til;
    MaterialButton positive, negative;

    LinearLayout buttons_ll;

    boolean isInputEnable = false;

    public CustomDialog(Context context) {
        c = context;
       dialog = new Dialog(context);
       defaultSetup();
    }
    private void defaultSetup(){
        dialog.setContentView(R.layout.custom_alert_dialog);
        dialog.getWindow().setLayout(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCancelable(false);

        title = dialog.findViewById(R.id.title);
        body = dialog.findViewById(R.id.body);
        til = dialog.findViewById(R.id.til);
        positive = dialog.findViewById(R.id.positive);
        negative = dialog.findViewById(R.id.negative);
        //buttons_ll = dialog.findViewById(R.id.buttons_ll);

        positive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        negative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    public void setInputEnabled(boolean type){
        if(type){
            isInputEnable = true;
            body.setVisibility(View.GONE);
            til.setVisibility(View.VISIBLE);
            setButtonBelow(R.id.til);
        }else{
            isInputEnable = false;
            body.setVisibility(View.VISIBLE);
            til.setVisibility(View.GONE);
            setButtonBelow(R.id.body);
        }
    }
    public void setError(String error){
        if(isInputEnable){
            til.setError(error);
        }
    }
    public void removeError(){
        if(isInputEnable){
            til.setErrorEnabled(false);
        }
    }
    public void setHint(String hint){
        if(isInputEnable){
            til.setHint(hint);
        }
    }

    public void setPositive(String text, View.OnClickListener listener){
        positive.setText(text);
        positive.setOnClickListener(listener);
    }
    public void setNegative(String text, View.OnClickListener listener) {
        negative.setText(text);
        negative.setOnClickListener(listener);
    }

    public void setTitle(String text){
        title.setText(text);
    }
    public void setMessage(String text){
        if(!isInputEnable){
            body.setText(text);
        }
    }
    public void show(){
        dialog.show();
    }
    private void setButtonBelow(int id){
        ((RelativeLayout.LayoutParams)positive.getLayoutParams()).addRule(RelativeLayout.BELOW, id);
        ((RelativeLayout.LayoutParams)negative.getLayoutParams()).addRule(RelativeLayout.BELOW, id);
        //((RelativeLayout.LayoutParams)negative.getLayoutParams()).addRule(RelativeLayout.START_OF, R.id.positive);

        int px_32 = getPx(32);
        int px_16 = getPx(16);


        /*RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) positive.getLayoutParams();
        params.addRule(RelativeLayout.ALIGN_PARENT_END);
        params.setMargins(500, 0, 500, 0);
        positive.setLayoutParams(params);*/

        //((RelativeLayout.LayoutParams) buttons_ll.getLayoutParams()).addRule(RelativeLayout.BELOW, id);
       // buttons_ll.setPadding(0, 0, px_32, 0);

    }

    private int getPx(int dp){
        Resources r = c.getResources();
        return  (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                r.getDisplayMetrics());
    }

}
