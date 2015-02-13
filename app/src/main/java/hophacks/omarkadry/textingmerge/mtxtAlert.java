package hophacks.omarkadry.textingmerge;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class mtxtAlert extends Dialog implements
        android.view.View.OnClickListener {

    public Activity activity;
    public Button close;
    String message;

    //Constructor to allow creation of alert box. Message is the message to display
    public mtxtAlert(Activity activity, String message) {
        super(activity);
        // TODO Auto-generated constructor stub
        this.activity = activity;
        this.message = message;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Set the fonts to doris_font
        Typeface doris_font = Typeface.createFromAsset(activity.getAssets(), "dosis-semibold.ttf");

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.custom_alert);

        //Set up the close button
        close = (Button) findViewById(R.id.close);
        close.setOnClickListener(this);
        close.setTypeface(doris_font);

        //Set up the Dialog Message
        TextView message = (TextView) findViewById(R.id.alert);
        message.setTypeface(doris_font);
        message.setText(this.message);

    }

    @Override
    public void onClick(View v) {
            dismiss();
    }
}