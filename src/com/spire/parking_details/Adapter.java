package com.spire.parking_details;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.spire.R;

/* Copyright (C) Aalto University 2014
 *
 * Created with IntelliJ IDEA.
 * User: volodymyr
 * E-mail: volodymyr.n.paliy@gmail.com
 * Date: 18.07.13
 * Time: 16:30
 */
public class Adapter extends BaseAdapter {

    static ArrayList<ListItems> mArrayList;
    LayoutInflater mLayoutInflater;
    Context mContext;

    @Override
    public int getCount() {
        return mArrayList.size();  //To change body of implemented methods use File | Settings | File Templates.
    }
    
    @Override
    public Object getItem(int position) {
        return mArrayList.get(position);  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public long getItemId(int position) {
        return mArrayList.indexOf(mArrayList.get(position));  //To change body of implemented methods use File | Settings | File Templates.
    }

    static class ViewHolder {

        ImageView mImage;

        TextView mDescription;
    }

    public Adapter ( Context mContext, ArrayList<ListItems> ArrayOfItems ){
        this.mContext = mContext;
        mArrayList = ArrayOfItems;

        this.mLayoutInflater = (LayoutInflater) this.mContext
                .getSystemService(this.mContext.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder mViewHolder;
        View mView = convertView;
        
        this.mLayoutInflater = ( LayoutInflater ) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        ListItems mItem = mArrayList.get(position);
        if ( mView == null ){
        	
            mView = mLayoutInflater.inflate( R.layout.parking_details_alert_list_item, null );
        
        

            mViewHolder = new ViewHolder();

            mViewHolder.mImage = ( ImageView ) mView.findViewById( R.id.iv_parking_details_alert_field_image );
            mViewHolder.mDescription = ( TextView ) mView.findViewById( R.id.tv_parking_details_alert_field_description );

            mViewHolder.mDescription.setText(mItem.Description);
            mViewHolder.mImage.setImageResource(mItem.ImageResource);
            
            mView.setTag(mViewHolder);
        }
        else{ 
        	mViewHolder = ( ViewHolder ) mView.getTag();
        }
        

        
        return mView;
    }
    

    
}
