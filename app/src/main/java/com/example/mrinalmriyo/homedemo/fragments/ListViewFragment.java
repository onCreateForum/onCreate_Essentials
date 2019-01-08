package com.example.mrinalmriyo.homedemo.fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;

import com.example.mrinalmriyo.homedemo.R;

import java.util.ArrayList;

public class ListViewFragment extends DialogFragment {

    /**
     * This displays the member details for the admin attendance page.
     *
     * Built by Irfan S
     */

    Button btn;
    ListView lv;
    SearchView sv;
    private final String TAG = "ListVFrag_OCE";
    String date_txt;
    ArrayAdapter<String> adapter;
    ArrayList<String> str= new ArrayList<>();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        View rootView=inflater.inflate(R.layout.fragment_layout, null);
        lv= rootView.findViewById(R.id.listView1);
        sv= rootView.findViewById(R.id.searchView1);
        btn= rootView.findViewById(R.id.dismiss);

        date_txt = getArguments().getString("date");
        //SET TITLE DIALOG TITLE
        getDialog().setTitle(date_txt);

        str = getArguments().getStringArrayList("ids");
        adapter=new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1,str);
        lv.setAdapter(adapter);

        Log.d(TAG,"Attendance List Fragment launched");


        //BUTTON,LISTVIEW,SEARCHVIEW INITIALIZATIONS

        //CREATE AND SET ADAPTER TO LISTVIEW


        //SEARCH
        sv.setQueryHint("Search..");
        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String txt) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean onQueryTextChange(String txt) {
                // TODO Auto-generated method stub

                adapter.getFilter().filter(txt);
                return false;
            }
        });

        //BUTTON
        btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                dismiss();
            }
        });

        return rootView;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        adapter.clear();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        adapter.clear();
    }
}

