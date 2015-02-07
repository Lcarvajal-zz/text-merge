package hophacks.omarkadry.textingmerge;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
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
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

import static android.database.DatabaseUtils.dumpCursorToString;

public class TextMerge extends ActionBarActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    public final String DEBUG = "!!!DEBUG!!!";
    private static final int LOADER_ID = 1;
    Spinner groupSpinner;
    SimpleCursorAdapter mAdapter;

    private static final String[] FIELDS = new String[]
        //strings of suggested text
        {
            "@first_name", "@last_name"
        };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Button sendButton;
        ArrayAdapter<String> adapter;


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_merge);

        //Initialize the GroupListLoader
        getLoaderManager().initLoader(0, null, this);

        //Set the Adapter for the Groups and send it to the group spinner
        Log.i(DEBUG, "Attempting to set the Adapter");
        groupSpinner = (Spinner) findViewById(R.id.phoneGroups);
        mAdapter = new SimpleCursorAdapter(this,
            android.R.layout.simple_list_item_2,
            null,
            new String[]{ContactsContract.Groups.TITLE, ContactsContract.Groups.ACCOUNT_NAME,
                ContactsContract.Groups.ACCOUNT_TYPE, ContactsContract.Groups._ID},
            new int[]{android.R.id.text1, android.R.id.text2},
            0);

        //If the Account_type is not a google account it is stored on the phone, so
        //We will change the ACCOUNT_NAME to say "Saved on Phone"
        //Account_name is not compared because I do not know if it varies between phones.
        //It is certain that the only non-google account type is the phone however, so it is used
        //to compare.

        mAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            public boolean setViewValue(View aView, Cursor aCursor, int aColumnIndex) {
            if (aColumnIndex == GroupListLoader.ACCOUNT_NAME) {

                String acc_name = aCursor.getString(GroupListLoader.ACCOUNT_NAME);
                String acc_type = aCursor.getString(GroupListLoader.ACCOUNT_TYPE);
                TextView spinnerText;

                //Not a Google Account
                if(!acc_type.equals("com.google")){
                    //Change to "Saved on Phone"
                    spinnerText = (TextView) aView;
                    spinnerText.setText("Saved on Phone");
                }
                //Is a Google Account so display the E-mail
                else{
                    spinnerText = (TextView) aView;
                    spinnerText.setText(acc_name);
                }
                return true;

            }
            return false;
            }
        });

        //Set the Spinner Adapter
        groupSpinner = (Spinner) findViewById(R.id.phoneGroups);
        groupSpinner.setAdapter(mAdapter);

        //suggest first name, last name drop down
        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, FIELDS);
        final AutoCompleteTextView textView = (AutoCompleteTextView)
                findViewById(R.id.text_message);
        textView.setAdapter(adapter);


        //Send button listener
        sendButton = (Button)findViewById(R.id.sendButton);
        sendButton.setOnClickListener(new View.OnClickListener()
        {
            Toast toast;
            public void onClick(View arg0)
            {
                String textMessage = textView.getText().toString();

                Context context = getApplicationContext();
                CharSequence text = textMessage;
                int duration = Toast.LENGTH_SHORT;

                toast = Toast.makeText(context, text, duration);
                toast.show();
            }
        });
    }

    //Based on the Selected Group will return the ID of that group.
    //Use the loader because its already there???
    //Could try using when the spinner has a selection store what's currently selected's ID.
    //I assumed that getCursor will give us the currently selected item in the spinner
    private int getGroupID(){
       int groupID = Integer.parseInt(mAdapter.getCursor().getString(GroupListLoader.GROUP_ID));
       Log.i(DEBUG, "Group ID is: " + groupID);
       return groupID;
    }

    //Based on the GroupID returns an ArrayList of Contacts with their Display name and Phone Number
    private ArrayList<Contact> getContacts(int groupID){
        ArrayList<Contact> contactList = new ArrayList<Contact>();

        //Query for all contacts_ids in that group
        Uri groupURI = ContactsContract.Data.CONTENT_URI;
        String[] projection = new String[] {
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.GroupMembership.CONTACT_ID };

        Cursor c = getContentResolver().query(
            groupURI,
            projection,
            ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID
                + "=" + groupID, null, null);

        //For each Contact ID get the contact information
        while (c.moveToNext()) {
            String id = c
                .getString(c
                    .getColumnIndex(ContactsContract.CommonDataKinds.GroupMembership.CONTACT_ID));

            //Projection will make it so only 1 Contact with any given name is returned.
            //For some reason contacts were returned multiple times due to slightly different meta
            //data
            Cursor pCur = getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER},
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                new String[] { id },
                null);

            Contact data;
            String name;
            String phoneNumber;
            //Save the Contact Data in a wrapper object then store it in an array
            while (pCur.moveToNext()) {
                name = pCur
                    .getString(pCur
                        .getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));

                phoneNumber = pCur
                    .getString(pCur
                        .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                data = new Contact(phoneNumber, name);
                Log.i(DEBUG, data.toString());
                contactList.add(data);
            }
            pCur.close();
        }

        return contactList;
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
