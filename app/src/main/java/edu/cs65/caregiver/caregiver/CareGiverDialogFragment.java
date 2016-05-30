package edu.cs65.caregiver.caregiver;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CareGiverDialogFragment extends android.app.DialogFragment implements AdapterView.OnItemClickListener {

    // Dialog IDs
    public static final int DISPLAY_MED_LIST = 0;

    // Dialog ID key
    private static final String DIALOG_ID_KEY = "dialog_id";


    public Dialog onCreateDialog(Bundle SavedInstanceState) {
        int dialog_id = getArguments().getInt(DIALOG_ID_KEY);
        ArrayList<CareRecipientActivity.MedEntry> sortedMeds = CareRecipientActivity.sortedMeds;

        for (int i=0;i<sortedMeds.size();i++){
            String title = CareRecipientActivity.convertTime(sortedMeds.get(i).time);
            sortedMeds.get(i).label = title;
        }

        // Handling all dialogs
        switch (dialog_id) {
            case DISPLAY_MED_LIST:
                AlertDialog.Builder medBuilder = new AlertDialog.Builder(getActivity());
                final ExpandableListView medList = new ExpandableListView(getActivity());
                ExpandableListAdapter mAdapter;

                List<Map<String, String>> groupData = new ArrayList<Map<String, String>>();
                List<List<Map<String, String>>> childData = new ArrayList<List<Map<String, String>>>();

                for (int i=0;i<sortedMeds.size();i++){
                    CareRecipientActivity.MedEntry entry = sortedMeds.get(i);
                    Map<String, String> curgroupMap = new HashMap<String, String>();
                    groupData.add(curgroupMap);
                    curgroupMap.put("parent", entry.label);

                    List<Map<String,String>> children =new ArrayList<Map<String,String>>();
                    for (int j=0;j<entry.meds.size();j++){
                        Map<String, String> curChildMap = new HashMap<String, String>();
                        children.add(curChildMap);
                        curChildMap.put("child", entry.meds.get(j));
                    }

                    childData.add(children);
                }

                mAdapter = new SimpleExpandableListAdapter(getActivity(), groupData,
                        android.R.layout.simple_expandable_list_item_1, new String[] { "parent" },
                        new int[] { android.R.id.text1}, childData,
                        android.R.layout.simple_expandable_list_item_2, new String[] {"child"},
                        new int[] { android.R.id.text1 }
                ) {
                };
                medList.setAdapter(mAdapter);
                medBuilder.setView(medList);
                return medBuilder.create();

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
