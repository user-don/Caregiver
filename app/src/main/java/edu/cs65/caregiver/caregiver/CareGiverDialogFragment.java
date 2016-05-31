package edu.cs65.caregiver.caregiver;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;


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

                mAdapter = new SimpleExpandableListAdapter(getActivity().getApplicationContext(), groupData,
                        android.R.layout.simple_expandable_list_item_1, new String[] { "parent"},
                        new int[] { android.R.id.text1 }, childData,
                        android.R.layout.simple_expandable_list_item_2, new String[] {"child"},
                        new int[] { android.R.id.text1 }
                ) {
                    @Override
                    public View getChildView(int groupPosition, int childPosition,
                                             boolean isLastChild, View convertView, ViewGroup parent) {
                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT, 150);

                        String child = getChild(groupPosition, childPosition).toString();
                        String med = child.substring(7,child.length()-1);
                        med = "          " + med;

                        TextView text = new TextView(CareRecipientActivity.mContext);
                        text.setLayoutParams(lp);
                        text.setText(med);
                        text.setTextColor(Color.DKGRAY);
                        text.setTextSize(23);
                        text.setGravity(Gravity.CENTER_VERTICAL);
                        return text;
                    }

                    @Override
                    public View getGroupView(int groupPosition, boolean isExpanded,
                                             View convertView, ViewGroup parent) {
                        // TODO Auto-generated method stub
                        TextView tv = (TextView) super.getGroupView(groupPosition, isExpanded, convertView, parent);
                        //change background of tv here
                        tv.setTextColor(Color.DKGRAY);
                        tv.setTextSize(25);

                        String tempString=tv.getText().toString();
                        SpannableString spanString = new SpannableString(tempString);
                        spanString.setSpan(new StyleSpan(Typeface.BOLD), 0, spanString.length(), 0);
                        tv.setText(spanString);
                        return tv;
                    }

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
