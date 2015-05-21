package com.spire.drawerlist;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.spire.MainActivity;
import com.spire.R;


/* Copyright (C) Aalto University 2014
 *
 * Created with IntelliJ IDEA.
 * User: volodymyr
 * Date: 03.07.13
 * Time: 15:04
 */
public class DrawerAdapter extends BaseAdapter {

    static ArrayList<DrawerItem> mArrayOfItems;
    LayoutInflater mLayoutInflater;
    Context mContext;
    @Override
    public int getCount() {
        return mArrayOfItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mArrayOfItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    static class ViewHolder {
        TextView mTextViewFiled;
        ImageView mImageViewField;
    }

    // constr
    public DrawerAdapter ( Context mContext, ArrayList<DrawerItem> ArrayOfItems ){
        this.mContext = mContext;
        mArrayOfItems = ArrayOfItems;

        this.mLayoutInflater = (LayoutInflater) this.mContext
                .getSystemService(this.mContext.LAYOUT_INFLATER_SERVICE);
    }
    MainActivity ma = new MainActivity();
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder mViewHolder;
        View mView = convertView;
        if ( mView == null ){
            mView = mLayoutInflater.inflate( R.layout.drawer_item_list, null );

//            R.layout.
           // mView = mLayoutInflater.inflate(R.layout., parent, false);
          //  mViewHolder.mTextViewFiled = (TextView) convertView.findViewById(R.id)
        
            DrawerItem mDrawerItem = mArrayOfItems.get(position);

            mViewHolder = new ViewHolder();

            mViewHolder.mTextViewFiled = ( TextView ) mView.findViewById( R.id.tv_list_description );
            mViewHolder.mImageViewField = ( ImageView ) mView.findViewById( R.id.imv_list_icon);

            mViewHolder.mTextViewFiled.setText(mDrawerItem.getFieldName());
            mViewHolder.mImageViewField.setImageResource(mDrawerItem.getImage());
            
            mView.setTag(mViewHolder);

        } else {        	
        	mViewHolder = ( ViewHolder ) mView.getTag();        	
        }

        return mView;
    }
}
