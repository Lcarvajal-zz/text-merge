package hophacks.omarkadry.textingmerge;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import static android.database.DatabaseUtils.dumpCursorToString;

public class TextMerge extends ActionBarActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    public final String DEBUG = "!!!DEBUG!!!";
    private static final int LOADER_ID = 1;
    Spinner groupSpinner;
    SimpleCursorAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_merge);

        //Initialize the GroupListLoader 
        getLoaderManager().initLoader(0, null, this);

        //Set the Adapter for the Groups and send it to the group spinner
        Log.i(DEBUG, "Attempting to set the Adapter");
        groupSpinner = (Spinner) findViewById(R.id.phoneGroups);
        mAdapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_list_item_1, null,
                new String[]{ContactsContract.Groups.TITLE}, new int[]{android.R.id.text1}, 0);
        groupSpinner = (Spinner) findViewById(R.id.phoneGroups);
        groupSpinner.setAdapter(mAdapter);

        //suggest first name, last name drop down
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, FIELDS);
        final AutoCompleteTextView textView = (AutoCompleteTextView)
                findViewById(R.id.text_message);
        textView.setAdapter(adapter);


        //send button
        Button sendButton = (Button)findViewById(R.id.sendButton);
        sendButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View arg0)
            {
                String textMessage = textView.getText().toString();

                Context context = getApplicationContext();
                CharSequence text = textMessage;
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }
        });
    }

    private static final String[] FIELDS = new String[]
            //strings of suggested text
            {
                    "@first_name", "@last_name"
            };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_text_merge, menu);
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
    //Creates the GroupListLoader and returns it
    public Loader onCreateLoader(int id, Bundle args) {
        Log.i(DEBUG, "onCreateLoader called");
        return new GroupListLoader(this);
    }

    @Override
    //Swaps the loaded loader into an adapter so the user can see the groups and select one
    public void onLoadFinished(Loader loader, Cursor cursor) {
            Log.i(DEBUG, "onLoadFinished called");
            mAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader loader) {
        Log.i(DEBUG, "onLoaderRest called");
        mAdapter.swapCursor(null);
    }
}
