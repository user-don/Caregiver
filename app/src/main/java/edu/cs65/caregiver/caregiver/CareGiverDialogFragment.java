package edu.cs65.caregiver.caregiver;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.SimpleExpandableListAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ellenli on 5/19/16.
 */
public class CareGiverDialogFragment extends android.app.DialogFragment implements AdapterView.OnItemClickListener {

    // Dialog IDs
    public static final int DISPLAY_MED_LIST = 0;
    public static final int DIALOG_10AM = 1;
    public static final int DIALOG_1PM = 2;
    public static final int DIALOG_6PM = 3;
    public static final int DIALOG_MENU = 4;

    // Dialog ID key
    private static final String DIALOG_ID_KEY = "dialog_id";
    private static final String[] medicationTimeList = {"10:00AM", "3:00PM", "7:00PM", "10:00PM"};
    private static final String[] medicationList = {"tylenol","advil","nyQuill"};


    public Dialog onCreateDialog(Bundle SavedInstanceState) {
        int dialog_id = getArguments().getInt(DIALOG_ID_KEY);

        // Handling all dialogs
        switch (dialog_id) {
            case DISPLAY_MED_LIST:
                AlertDialog.Builder medBuilder = new AlertDialog.Builder(getActivity());
                final ExpandableListView medList = new ExpandableListView(getActivity());
                ExpandableListAdapter mAdapter;

                List<Map<String, String>> groupData = new ArrayList<Map<String, String>>();
                List<List<Map<String, String>>> childData = new ArrayList<List<Map<String, String>>>();

                for (int i=0;i<medicationTimeList.length;i++){
                    Map<String, String> curgroupMap = new HashMap<String, String>();
                    groupData.add(curgroupMap);
                    curgroupMap.put("parent", medicationTimeList[i]);

                    List<Map<String,String>> children =new ArrayList<Map<String,String>>();
                    for (int j=0;j<medicationList.length;j++){
                        Map<String, String> curChildMap = new HashMap<String, String>();
                        children.add(curChildMap);
                        curChildMap.put("child", medicationList[j]);
                    }

                    childData.add(children);
                }

                mAdapter = new SimpleExpandableListAdapter(getActivity(), groupData,
                        android.R.layout.simple_expandable_list_item_1, new String[] { "parent" },
                        new int[] { android.R.id.text1}, childData,
                        android.R.layout.simple_expandable_list_item_2, new String[] {"child"},
                        new int[] { android.R.id.text1 }
                );
                medList.setAdapter(mAdapter);
                medBuilder.setView(medList);
                return medBuilder.create();

            case DIALOG_10AM:
                final ArrayList selectedItems = new ArrayList();
                String meds[] = {"Tylenol", "Claritin", "Flexeril", "Ambien"};
                AlertDialog.Builder alertDialog1 = new AlertDialog.Builder(getActivity());
                alertDialog1.setTitle("10am Medications");
                alertDialog1.setMultiChoiceItems(meds, null, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {

                        if (isChecked) {
                            selectedItems.add(which);
                        } else if (selectedItems.contains(which)) {
                            selectedItems.remove(Integer.valueOf(which));
                        }

                    }
                }).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });

                return alertDialog1.create();

            case DIALOG_1PM:
                final ArrayList selectedItems2 = new ArrayList();
                String meds2[] = {"Tylenol", "Claritin", "Flexeril", "Ambien"};
                AlertDialog.Builder alertDialog2 = new AlertDialog.Builder(getActivity());
                alertDialog2.setTitle("10pm Medications");
                alertDialog2.setMultiChoiceItems(meds2, null, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {

                        if (isChecked) {
                            selectedItems2.add(which);
                        } else if (selectedItems2.contains(which)) {
                            selectedItems2.remove(Integer.valueOf(which));
                        }

                    }
                }).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });

                return alertDialog2.create();

            case DIALOG_6PM:
                final ArrayList selectedItems3 = new ArrayList();
                String meds3[] = {"Tylenol", "Claritin", "Flexeril", "Ambien"};
                AlertDialog.Builder alertDialog3 = new AlertDialog.Builder(getActivity());
                alertDialog3.setTitle("6pm Medications");
                alertDialog3.setMultiChoiceItems(meds3, null, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {

                        if (isChecked) {
                            selectedItems3.add(which);
                        } else if (selectedItems3.contains(which)) {
                            selectedItems3.remove(Integer.valueOf(which));
                        }

                    }
                }).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });

                return alertDialog3.create();

            case DIALOG_MENU:
                AlertDialog.Builder functionsDialog = new AlertDialog.Builder(getActivity());
                functionsDialog.setTitle("Additional Functions");
                functionsDialog.setPositiveButton("Check-In", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AlertDialog.Builder checkinDialog = new AlertDialog.Builder(getActivity());
                        checkinDialog.setTitle("Check-in Today!");
                        checkinDialog.setPositiveButton("Check-In", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                            }
                        });
                        checkinDialog.create();
                        checkinDialog.show();
                    }
                }).setNegativeButton("Fall Alert", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent fallIntent = new Intent(getActivity(), FallActivity.class);
                        getActivity().startActivity(fallIntent);
                    }
                });
                return functionsDialog.create();

            default:
                return null;

        }

    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    public static CareGiverDialogFragment newInstance(int dialog_id) {
        CareGiverDialogFragment frag = new CareGiverDialogFragment();
        Bundle args = new Bundle();
        args.putInt(DIALOG_ID_KEY, dialog_id);
        frag.setArguments(args);
        return frag;
    }
}
