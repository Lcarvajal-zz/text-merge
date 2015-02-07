package hophacks.omarkadry.textingmerge;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.MultiAutoCompleteTextView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TextMerge extends ActionBarActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    public final String DEBUG = "!!!DEBUG!!!";
    SimpleCursorAdapter mAdapter;
    private static final String[] FIELDS = new String[]
            //strings of suggested text
            {
                    "@name", "@first_name", "@last_name"
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final MultiAutoCompleteTextView textComplete;
        ArrayAdapter<String> aaStr;
        Button sendButton;
        Spinner groupSpinner;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_merge);

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
                if (aColumnIndex == GroupListLoader.ACCOUNT_NAME) {

                    String acc_name = aCursor.getString(GroupListLoader.ACCOUNT_NAME);
                    String acc_type = aCursor.getString(GroupListLoader.ACCOUNT_TYPE);
                    TextView spinnerText;

                    //Not a Google Account
                    if (!acc_type.equals("com.google")) {
                        //Change to "Saved on Phone"
                        spinnerText = (TextView) aView;
                        spinnerText.setText("Saved on Phone");
                    }
                    //Is a Google Account so display the E-mail
                    else {
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


        textComplete = (MultiAutoCompleteTextView) this.findViewById(R.id.text_message);
        aaStr = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, FIELDS);
        textComplete.setAdapter(aaStr);
        textComplete.setTokenizer(new SpaceTokenizer());

        //Send button
        //OnClick send button
        sendButton = (Button) findViewById(R.id.sendButton);
        sendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0)
            //SEND BUTTON FUNCTION
            {
                ArrayList<Contact> contactList;
                String message = textComplete.getText().toString();

                //Break apart the string based on '@' escape characters
                List<String> textApart = new ArrayList<>();
                Scanner scanM = new Scanner(message);

                //first name locations in text message
                int[] fNameL = new int[10];
                fNameL[0] = -1;
                int i = 0;

                //last name locations in text message
                int[] lNameL = new int[10];
                lNameL[0] = -1;
                int j = 0;

                //full name locations in text message
                int[] nameL = new int[10];
                nameL[0] = -1;
                int k = 0;

                //all '@' locations to reset to default
                //1/3 fNameL, 1/3 lNameL, 1/3 nameL
                int[] namesLo = new int[30];


                while (scanM.hasNext())
                //scan text for '@' names
                {
                    String word = scanM.next();
                    if (word.equals("@first_name")) {
                        textApart.add(word);
                        fNameL[i] = textApart.size() - 1;
                        namesLo[i] = textApart.size() - 1;
                        i++;
                    } else if (word.equals("@last_name")) {
                        textApart.add(word);
                        lNameL[j] = textApart.size() - 1;
                        namesLo[j + 10] = textApart.size() - 1;
                        j++;
                    } else if (word.equals("@name")) {
                        textApart.add(word);
                        nameL[k] = textApart.size() - 1;
                        namesLo[k + 20] = textApart.size() - 1;
                        k++;
                    } else {
                        textApart.add(word);
                    }
                }

                List<String> textIndividual = new ArrayList<>();

                for (int a = 0; a < textApart.size(); a++)
                //copy array so that only parts have to be replaced later
                {
                    textIndividual.add(textApart.get(a));
                }

                //Get list of contacts
                //contactList = getContacts(getGroupID());

                for (int z = 0; z < 1; z++) {
                    String fn = "first";
                    String lasn = "last";
                    String nam = "name";

                    String text_message = new String();

                    if (nameL[0] != -1)
                    //insert all full names if needed
                    {
                        if (nameL[0] == 0) {
                            textIndividual.set(0, nam);
                        }

                        for (int a = 0; a < 10; a++) {
                            if (nameL[a] != 0)
                                textIndividual.set(nameL[a], nam);
                        }
                    }

                    if (fNameL[0] != -1)
                    //insert all first names if needed
                    {
                        if (fNameL[0] == 0)
                            textIndividual.set(0, fn);

                        for (int a = 0; a < 10; a++) {
                            if (fNameL[a] != 0)
                                textIndividual.set(fNameL[a], fn);
                        }
                    }

                    if (lNameL[0] != -1)
                    //insert all last names if needed
                    {
                        if (lNameL[0] == 0)
                            textIndividual.set(0, lasn);

                        for (int a = 0; a < 10; a++) {
                            if (lNameL[a] != 0)
                                textIndividual.set(lNameL[a], lasn);
                        }
                    }

                    for (int b = 0; b < textIndividual.size(); b++)
                    //display contents before sending text FOR TESTING ONLY
                    {
                        Context context = getApplicationContext();
                        CharSequence text = textIndividual.get(b);
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(context, text, duration);
                        toast.show();
                    }

                    //send the text to current contact
                    SmsManager smsText = SmsManager.getDefault();
                    smsText.sendTextMessage("phone#", null, "message", null, null);

                    for (int a = 0; a < textApart.size(); a++)
                    //display contents  after sending text FOR TESTING ONLY
                    {
                        if (namesLo[a] > 0) {
                            if (a < 10)
                                textIndividual.set(namesLo[a], "@first_name");
                            else if (a < 20)
                                textIndividual.set(namesLo[a], "@last_name");
                            else
                                textIndividual.set(namesLo[a], "@name");

                        } else if (namesLo[a] == 0 && a % 10 == 0) {
                            if (a == 0)
                                textIndividual.set(namesLo[a], "@first_name");
                            else if (a == 10)
                                textIndividual.set(namesLo[a], "@last_name");
                            else
                                textIndividual.set(namesLo[a], "@name");
                        }

                        for (int b = 0; b < textIndividual.size(); b++) {
                            Context context = getApplicationContext();
                            CharSequence text = "a" + textIndividual.get(b);
                            int duration = Toast.LENGTH_SHORT;

                            Toast toast = Toast.makeText(context, text, duration);
                            toast.show();
                        }
                    }

                }
            }   //END OF SEND BUTTON FUNCTION
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