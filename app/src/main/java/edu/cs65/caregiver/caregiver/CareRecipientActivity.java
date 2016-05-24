package edu.cs65.caregiver.caregiver;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ellenli on 5/19/16.
 */
public class CareRecipientActivity extends Activity {

    // Menu Options
    private static final int ACCT = 0;
    private static final int FALL = 1;
    private static final int CHECK_IN = 2;
    public static Context mContext;

    private List<String> listValues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_care_recipient);
        mContext = getApplicationContext();

//        listValues = new ArrayList<String>();
//        listValues.add("10am");
//        listValues.add("1pm");
//        listValues.add("6pm");


        // initiate list adapter
        //ArrayAdapter<String> myAdapter = new ArrayAdapter<String>(this, R.layout.row_layout,
        //        R.id.listText, listValues);

        // assign the list adapter
        //setListAdapter(myAdapter);
    }


//    @Override
//    protected void onListItemClick(ListView list, View view, int position, long id) {
//        super.onListItemClick(list, view, position, id);
//
//        String selectedItem = (String) getListView().getItemAtPosition(position);
//
//        switch (selectedItem) {
//            case "10am":
//                displayDialog(CareGiverDialogFragment.DIALOG_10AM);
//                break;
//            case "1pm":
//                displayDialog(CareGiverDialogFragment.DIALOG_1PM);
//                break;
//            case "6pm":
//                displayDialog(CareGiverDialogFragment.DIALOG_6PM);
//                break;
//        }
//    }

    public void displayDialog(int id) {
        DialogFragment fragment = CareGiverDialogFragment.newInstance(id);
        fragment.show(getFragmentManager(),
                getString(R.string.app_name));
    }

    public void onMedClicked(View v){
        displayDialog(0);
    }


    public void onHelpClicked(View v) {
        Toast.makeText(getApplicationContext(), "CAREGIVER HAS BEEN ALERTED",
                Toast.LENGTH_LONG).show();
    }

    public void onMenuClicked(View v) {
        displayDialog(CareGiverDialogFragment.DIALOG_MENU);
    }

}
