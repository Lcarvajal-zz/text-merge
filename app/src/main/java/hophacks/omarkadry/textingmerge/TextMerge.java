package hophacks.omarkadry.textingmerge;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.Image;
import android.net.Uri;
import android.provider.ContactsContract;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.MultiAutoCompleteTextView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import java.util.ArrayList;

public class TextMerge extends Activity implements LoaderManager.LoaderCallbacks<Cursor>{

    public final String DEBUG = "!!!DEBUG!!!";
    SimpleCursorAdapter mAdapter;
    private static final String[] FIELDS = new String[]
            //strings of suggested text
            {
                    "@name", "@first_name", "@last_name"
            };
    MultiAutoCompleteTextView textComplete;
    Typeface doris_font;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ArrayAdapter<String> aaStr;
        ImageButton sendButton;
        Spinner groupSpinner;
        doris_font = Typeface.createFromAsset(getAssets(), "dosis-semibold.ttf");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_merge);

        TextView groupSelect = (TextView) findViewById(R.id.select_group_prompt);
        TextView enterMessage = (TextView) findViewById(R.id.enter_message_prompt);
        groupSelect.setTypeface(doris_font);
        enterMessage.setTypeface(doris_font);

        //Initialize the GroupListLoader
        getLoaderManager().initLoader(0, null, this);

        //Set the Adapter for the Groups and send it to the group spinner
        mAdapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_list_item_2,
                null,
                new String[]{ContactsContract.Groups.TITLE, ContactsContract.Groups.ACCOUNT_NAME,
                        ContactsContract.Groups.ACCOUNT_TYPE},
                new int[]{android.R.id.text1, android.R.id.text2},
                0);

        //If the Account_type is not a google account it is stored on the phone, so
        //We will change the ACCOUNT_NAME to say "Saved on Phone"
        //Account_name is not compared because I do not know if it varies between phones.
        //It is certain that the only non-google account type is the phone however, so it is used
        //to compare.
        mAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            public boolean setViewValue(View aView, Cursor aCursor, int aColumnIndex) {
                TextView spinnerText;

                //Change the Font of the titles
                if(aColumnIndex == GroupListLoader.TITLE){
                    spinnerText = (TextView) aView;
                    spinnerText.setTypeface(doris_font);
                    spinnerText.setTextColor(Color.WHITE);
                }

                if (aColumnIndex == GroupListLoader.ACCOUNT_NAME) {

                    String acc_name = aCursor.getString(GroupListLoader.ACCOUNT_NAME);
                    String acc_type = aCursor.getString(GroupListLoader.ACCOUNT_TYPE);

                    //Not a Google Account
                    if(!acc_type.equals("com.google")){
                        //Change to "Saved on Phone"
                        spinnerText = (TextView) aView;
                        spinnerText.setText("Saved on Phone");
                        spinnerText.setTypeface(doris_font);
                        spinnerText.setTextColor(Color.WHITE);
                    }
                    //Is a Google Account so display the E-mail
                    else{
                        spinnerText = (TextView) aView;
                        spinnerText.setText(acc_name);
                        spinnerText.setTypeface(doris_font);
                        spinnerText.setTextColor(Color.WHITE);
                    }
                    return true;
                }
                return false;
            }
        });

        //Set the Spinner Adapter
        groupSpinner = (Spinner) findViewById(R.id.phoneGroups);
        groupSpinner.setAdapter(mAdapter);

        //Set up Auto Complete
        textComplete = (MultiAutoCompleteTextView) this.findViewById(R.id.text_message);
        textComplete.setTypeface(doris_font);
        aaStr = new DorisArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line,
                FIELDS, doris_font);
        textComplete.setAdapter(aaStr);
        textComplete.setTokenizer(new SpaceTokenizer());


        //Send button
        //OnClick send button
        sendButton = (ImageButton)findViewById(R.id.sendButton);
        sendButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View arg0)
            //SEND BUTTON FUNCTION
            {
                sendTextAndExit();
            }
        });
    }

    private void sendTextAndExit() {
        ArrayList<Contact> contactList;
        String message = textComplete.getText().toString();

        //If the message is blank display an alert
        if(message.length() == 0){
            mtxtAlert alert=new mtxtAlert(this, "You cannot send an empty text message!");
            alert.show();
            return;
        }
        String messageToSend;
        Contact currentContact;

        contactList = getContacts(getGroupID());
        //If the group is empty display an alert
        if(contactList == null){
            mtxtAlert alert=new mtxtAlert(this, "This group has no Contacts!");
            alert.show();
            return;
        }

        //Go through all the contacts and send the text
        for(int i = 0; i < contactList.size(); i++){
            currentContact = contactList.get(i);
            messageToSend = message.replace("@first_name", currentContact.getFirstName());
            messageToSend = messageToSend.replace("@name", currentContact.getFullName());
            messageToSend = messageToSend.replace("@last_name", currentContact.getLastName());

            //Send the text to the Current Contact
            SmsManager smsText = SmsManager.getDefault();
            smsText.sendTextMessage(currentContact.getPhoneNumber(), null,
                    messageToSend, null, null);
            Log.i(DEBUG, "Text Message: '" + messageToSend + "' Sent to " + currentContact.getFullName());
        }

        mtxtAlert alert=new mtxtAlert(this, "You cannot send an empty text message!");
        alert.show();

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
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

    //Returns a list of all contacts (phone number, display name) based on group ID
    public ArrayList<Contact> getContacts(int groupID){
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
        if(contactList.size() == 0){return null;}
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
        return new GroupListLoader(this);
    }

    @Override
    //Swaps the loaded loader into an adapter so the user can see the groups and select one
    public void onLoadFinished(Loader loader, Cursor cursor) {
        mAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader loader) {
        mAdapter.swapCursor(null);
    }
}