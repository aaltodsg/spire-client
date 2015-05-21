package com.spire.favorites;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.spire.R;
import com.spire.mapview.MapView;
import com.spire.model.Communications;
import com.spire.model.struct.Parking;

import java.util.ArrayList;
import java.util.List;


/* Copyright (C) Aalto University 2014
 *
 * Created with IntelliJ IDEA.
 * User: volodymyr
 * E-mail: volodymyr.n.paliy@gmail.com
 * Date: 10.07.13
 * Time: 09:25
 */
public class Adapter extends BaseAdapter {

    List<Parking> parkingList;
    LayoutInflater mLayoutInflater;
    Context mContext;

    @Override
    public int getCount() {
        return parkingList.size();
    }

    @Override
    public Parking getItem(int position) {
        Log.d( "Adapter", "getItem: " + position);
        return parkingList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return parkingList.indexOf(parkingList.get(position));
    }

    class ViewHolder {
        ImageView mImage;
        TextView mTitle;
        //TextView mSubTitle;
        ImageButton mButtonDest;
        View viewDivider;
    }


    public Adapter ( Context mContext, List<Parking> parkings ){
        this.mContext = mContext;
        parkingList = parkings;

        this.mLayoutInflater = (LayoutInflater) this.mContext
                .getSystemService(this.mContext.LAYOUT_INFLATER_SERVICE);
    }

    public void setNewData(List<Parking> parkings){
        parkingList.clear();
        parkingList = parkings;


//        ArrayList<String> area = new ArrayList<String>();
//        for (Parking parking : parkingList){
//            if (parking.getIsParking()){
//                area.add(parking.getArea());
//            }
//        }
//
//        Communications.getInstance(mContext).getAreaStatus(area);



//        Log.e("Adapter", "parkingList: " + parkingList.get(0).getParkid() + "; parkings: " + parkings.get(0).getParkid() );
    }

    public void updateParkingsStatus(){
        ArrayList<String> area = new ArrayList<String>();
        for (Parking parking : parkingList){
            if (parking.getIsParking()){
                area.add(parking.getArea());
            }
        }

        Communications.getInstance(mContext).getAreaStatus(area);

    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder mViewHolder;
        View mView = convertView;

        Parking currentParking = parkingList.get(position);

        if ( mView == null ){
            mView = mLayoutInflater.inflate( R.layout.favorites_list_item, null );
            mViewHolder = new ViewHolder();

            mViewHolder.mImage = ( ImageView ) mView.findViewById( R.id.imv_favorites_list_icon );
            mViewHolder.mTitle = ( TextView ) mView.findViewById( R.id.tv_favorites_list_title );
            //mViewHolder.mSubTitle = ( TextView ) mView.findViewById( R.id.tv_favorites_list_subtitle );

            mViewHolder.viewDivider = (View) mView.findViewById(R.id.view_divider);
            mViewHolder.mButtonDest = (ImageButton) mView.findViewById(R.id.ibtn_favorites_list_dest);


            mViewHolder.mButtonDest.setTag(currentParking);





            mView.setTag(mViewHolder);


            
        }else {
        	mViewHolder = ( ViewHolder ) mView.getTag();
        }

        mViewHolder.mTitle.setText(currentParking.getParkid());
        if (!currentParking.getIsParking()){
            mViewHolder.viewDivider = (View) mView.findViewById(R.id.view_divider);
            mViewHolder.viewDivider.setVisibility(View.GONE);
            mViewHolder.mButtonDest.setVisibility(View.GONE);

            mViewHolder.mImage.setImageResource(R.drawable.pointer);
        } else {
            mViewHolder.viewDivider.setVisibility(View.VISIBLE);
            mViewHolder.mButtonDest.setVisibility(View.VISIBLE);

            mViewHolder.mImage.setImageResource(getTheIcon (currentParking )) ;

        }

        mViewHolder.mButtonDest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Parking current = (Parking)view.getTag();

                if (MapView.getCurrentLocation() != null){
                    getDirection(new LatLng(MapView.getCurrentLocation().getLatitude(),MapView.getCurrentLocation().getLongitude()), new LatLng(current.getLatitude(),current.getLongitude()));
                }
            }
        });


        return mView;
    }

    private int getTheIcon ( Parking currentParking ){

        int icon;

        if (currentParking.getStatus() != null){
            switch (currentParking.getStatus()){
                case "Available":
                    icon = R.drawable.green_icon;
                    break;
                case "AlmostFull":
                    icon = R.drawable.orange_icon;
                    break;
                case "Full":
                    icon = R.drawable.red_icon;
                    break;
                case "Unknown":
                    icon = R.drawable.green_orange_icon;
                    break;
                default:
                    icon = R.drawable.green_orange_icon;
            }
        } else {
            icon = R.drawable.green_orange_icon;
        }
        return icon;
    }

    private void getDirection(LatLng user_position, LatLng position){
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                Uri.parse("http://maps.google.com/maps?saddr=" + user_position.latitude + "," + user_position.longitude + "&daddr=" + position.latitude + "," + position.longitude)).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
        mContext.getApplicationContext().startActivity(intent);

    }



}
