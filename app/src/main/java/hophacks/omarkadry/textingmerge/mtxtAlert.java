package hophacks.omarkadry.textingmerge;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import hophacks.omarkadry.textingmerge.R;

public class mtxtAlert extends Dialog implements
        android.view.View.OnClickListener {

    public Activity c;
    public Dialog d;
    public Button close;
    String message;

    public mtxtAlert(Activity a, String message) {
        super(a);
        // TODO Auto-generated constructor stub
        this.c = a;
        this.message = message;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Typeface doris_font = Typeface.createFromAsset(c.getAssets(), "dosis-semibold.ttf");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.custom_alert);
        close = (Button) findViewById(R.id.close);
        close.setOnClickListener(this);
        close.setTypeface(doris_font);
        TextView message = (TextView) findViewById(R.id.alert);
        message.setTypeface(doris_font);
        message.setText(this.message);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.close:
                dismiss();
                break;
            default:
                break;
        }
        dismiss();
    }
}