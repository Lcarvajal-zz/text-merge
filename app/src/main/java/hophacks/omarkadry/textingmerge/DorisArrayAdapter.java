package hophacks.omarkadry.textingmerge;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Created by Omar Kadry on 2/7/2015.
 * This is an Adapter to allow the Doris font on the suggestions for Autocorrect
 */
public class DorisArrayAdapter<T> extends ArrayAdapter<T> {

    Typeface font;

    public DorisArrayAdapter(Context context, int resources, T[] objects, Typeface font){
        super(context, resources, objects);
        this.font = font;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup)
    {
        View v = super.getView(position, view, viewGroup);
        ((TextView)v).setTypeface(font);
        ((TextView)v).setTextColor(Color.WHITE);
        return v;
    }
}
