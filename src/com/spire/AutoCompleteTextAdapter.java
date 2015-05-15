package com.spire;

import android.content.Context;
import android.location.Location;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.spire.model.Communications;

import java.util.ArrayList;

/**
 * Created by volodymyr on 09.08.13.
 */
public class AutoCompleteTextAdapter extends ArrayAdapter<String> implements Filterable {
    ArrayList<String> mSuggestions = null;

    private static final String TAG = "AutoCompleteTextAdapter";
    private Context mContext = null;

    public AutoCompleteTextAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        mContext = context;
        mSuggestions = new ArrayList<String>();
    }

    @Override
    public int getCount() {
        if ( mSuggestions != null )
            return mSuggestions.size();
        return 0;
    }

    @Override
    public String getItem(int position) {
        return mSuggestions.get(position);
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
    /*    ((TextView)view).setText(mSuggestions.get(i));

//      LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//
//        final TextView view_ = (TextView) inflater.inflate(
//                android.R.layout.simple_dropdown_item_1line, viewGroup, false);
////        view_.setText(mSuggestions.get(i));
/**/
        View v = super.getView(i, view, viewGroup);
//        ((TextView)v).setTypeface(font);
        return v;

//        Log.e(TAG, "getView");
//        return view;
    }


    static class ViewHolder {
         TextView mTitle;
    }

    @Override
    public Filter getFilter(){
        Filter myFilter = new Filter() {

            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                FilterResults filterResults = new FilterResults();
                if ( charSequence != null ){

//                    mSuggestions.clear();
//                    for (int iter = 0; iter != 10; iter++)
//                        mSuggestions.add(iter+" "+iter);
////                    notifyDataSetChanged();

                    Communications com = Communications.getInstance(mContext);

                    com.getUser().restore(getContext());

                    Location location = new Location("");
                    location.setLongitude(Communications.getInstance(getContext()).getUser().getViewport_centre().longitude);
                    location.setLatitude(Communications.getInstance(getContext()).getUser().getViewport_centre().latitude);


                    com.getSearchSuggestions( location, 3*1000, charSequence.toString());
//                    new Location()
//

//
//                    mSuggestions = com.getSuggestionArrayList();
//                    filterResults.values = list;
//
                    Log.e(TAG, "performFiltering: finish: " + mSuggestions.size() + "; filterResults: " + filterResults );

                    // A class that queries a web API, parses the data and returns an ArrayList<Style>

                    // Now assign the values and count to the FilterResults object
                    if ( mSuggestions != null ){
                        filterResults.values = mSuggestions.toArray();
                        filterResults.count = mSuggestions.size();
                    }

                    Log.e(TAG, "performFiltering: finish;");

                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                Log.d(TAG, "publishResults: " + charSequence);
                if ( filterResults != null && filterResults.count > 0 )
                    notifyDataSetChanged();
                else
                    notifyDataSetInvalidated();
//                Communications com = Communications.getInstance(mContext);
//
//                ArrayList<String> list = new ArrayList<>();
//                list.add("bla");
//                list.add("bla_bla");
//                list.add("abla");

//                myFilter.
//                mSuggestions = com.getSuggestionArrayList();
//                Log.e(TAG, "performFiltering: mSuggestions: " /*+ mSuggestions*/);
//                filterResults.values = mSuggestions;
               // filterResults.count = mSuggestions.size();

//                notifyDataSetChanged();
            }
        };
        return myFilter;
    }



    public ArrayList<String>  getSuggestiong ( Location current_location, int radius, String query ){
        Communications com = Communications.getInstance(mContext);

        com.getSearchSuggestions(current_location, radius, query);

        return com.getSuggestionArrayList();
    }




    public void updateList ( ){

        Communications com = Communications.getInstance(mContext);
        mSuggestions = com.getSuggestionArrayList();


        notifyDataSetChanged();
    }


//    ////*
//


//    onItemCLick
    ///**//*///




}
