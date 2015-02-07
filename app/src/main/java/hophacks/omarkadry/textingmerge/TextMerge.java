package hophacks.omarkadry.textingmerge;

import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

//Test
public class TextMerge extends ActionBarActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    public final String DEBUG = "!!!DEBUG!!!";
    private static final int LOADER_ID = 1;
    Spinner groupSpinner;
    SimpleCursorAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_merge);

        groupSpinner = (Spinner) findViewById(R.id.phoneGroups);

        String[] groups = new String[]{ContactsContract.Groups.TITLE};
        int[] to = new int[] { R.id.phoneGroups };
        mAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, null, groups, to, 0);
        Log.i(DEBUG, "Attempting to set the Adapter");
        groupSpinner.setAdapter(mAdapter);
    }


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
        return new GroupListLoader(this);
    }

    @Override
    //Swaps the loader into an adapter so the user can see the groups and select one
    public void onLoadFinished(Loader loader, Cursor cursor) {
        switch(loader.getId()){
            case LOADER_ID:
                mAdapter.swapCursor(cursor);
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {

    }
}
