package com.spire.favorites;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.gms.maps.model.LatLng;
import com.spire.MainActivity;
import com.spire.R;
import com.spire.authentication.AuthActivity;
import com.spire.debug.Debug;
import com.spire.model.Communications;
import com.spire.model.IntentActions;
import com.spire.model.orm.HelperFactory;
import com.spire.model.struct.Parking;
import com.spire.parking_details.ParkingDetails;

import java.sql.SQLException;
import java.util.List;

/* Copyright (C) Aalto University 2014
 *
 * Created with IntelliJ IDEA.
 * User: volodymyr
 * Date: 10.07.13
 * Time: 09:23
 */
public class MyFavorites extends AuthActivity {

    private Adapter mAdapter;
    private ListView mListView;
    private Context mContext = this;
    private List<Parking> favoritesParking;
    private static final String TAG = "MyFavorites";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);
        mListView = (ListView) findViewById(R.id.lv_favorites_list);

        mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        HelperFactory.SetHelper(this);
        List<Parking> favoritesParking = null;
        try {
            favoritesParking = HelperFactory.GetHelper().getmParkingDAO().getAllParking(Communications.getInstance(this).getUser().getEmail());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        mAdapter = new Adapter(getApplicationContext(),favoritesParking);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG, "Click");
                Parking parking = mAdapter.getItem(position);
                parking.setEmail(Communications.getInstance(getApplicationContext()).getUser().getEmail());

                if ( parking.getIsParking() ){
                    //showParkingDetails(parking);
                    showParking(parking);
                }
                else{
                    createDustinationMarker(parking);
                }
            }
        });
        mListView.setAdapter(mAdapter);

        mListView.setEmptyView(findViewById(R.id.txt_empty));

                //set up action bar -> set the back button true;
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setDisplayShowHomeEnabled(false);
//        getActionBar().setDisplayUseLogoEnabled(true);
        getActionBar().setDisplayShowTitleEnabled(true);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle(getString(R.string.my_favorites));

        final SwipeDismissListViewTouchListener swipeDismissListViewTouchListener =
                new SwipeDismissListViewTouchListener ( mListView,
                        new SwipeDismissListViewTouchListener.DismissCallbacks() {
            @Override
            public boolean canDismiss(int position) {
                return position < mAdapter.getCount();
            }

            @Override
            public void onDismiss(ListView listView, int[] reverseSortedPositions) {
                for ( int position : reverseSortedPositions ){
                    removeFromFavorites(mAdapter.getItem(position));

                }
            }
        });

        mListView.setOnScrollListener(swipeDismissListViewTouchListener.makeScrollListener());
        mListView.setOnTouchListener( new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return swipeDismissListViewTouchListener.onTouch( v, event);
            }
        });


        mAdapter.updateParkingsStatus();
    }

    /*private void fillData(){
        mListItems.add( new FavoritesItem( R.drawable.green_icon, "test", "test", R.drawable.location_directions ));
        mListItems.add( new FavoritesItem( R.drawable.green_orange_icon, "test 1", "test 1", R.drawable.location_directions ));

    }*/
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    
    	switch ( item.getItemId()){
    	case android.R.id.home:
    		onBackPressed();
    		break;
    	default:
    		break;
    	}
    	return super.onOptionsItemSelected(item);
    }

    private void update(){
        HelperFactory.SetHelper(this);
        try {
            favoritesParking = HelperFactory.GetHelper().getmParkingDAO().getAllParking(Communications.getInstance(this).getUser().getEmail());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        mAdapter.setNewData(favoritesParking);
        mAdapter.notifyDataSetChanged();

    }

    private void showParkingDetails(Object item){

        if (item instanceof Parking){
                double latitude = ((Parking) item).getLatitude();
                double longitude = ((Parking) item).getLongitude();

                Intent mIntent = new Intent ( getApplicationContext(), ParkingDetails.class);
                /*mIntent.putExtra("position",new LatLng(Communications.round(latitude),Communications.round(longitude)));
                mIntent.putExtra("user_position", MapView.getCurrentLocation());*/
                mIntent.putExtra("parking",(Parking) item);
                startActivity(mIntent);

        }

    }

    @Override
    protected void onResume() {
        //update();
        super.onResume();
    }

    private void removeFromFavorites(Parking parking){
        Log.e(TAG, "parking name: " + parking.getParkid());
        try {
          //  Parking parkingForDelete = HelperFactory.GetHelper().getmParkingDAO().getParkingForCoordinates(parking.getLatitude(),parking.getLongitude());
           // Log.d(TAG, "parking for delete: " + parkingForDelete.getParkid() );
            HelperFactory.GetHelper().getmParkingDAO().delete(parking);
            update();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void createDustinationMarker(Parking parking){
        Intent mIntent = new Intent ( mContext, MainActivity.class);
        mIntent.setAction(IntentActions.ACTION_CREATE_DESTINATION_MARKER);
        mIntent.putExtra(IntentActions.EXTRA_POSITION, new LatLng( Communications.round( parking.getLatitude() ), Communications.round( parking.getLongitude() ) ) );
        mIntent.putExtra(IntentActions.EXTRA_MARKER_NAME, parking.getParkid() );
        Log.e(TAG, "send intent; position: " + new LatLng( parking.getLatitude(), parking.getLongitude() ) + " ;marker title: " + parking.getParkid() );
        startActivity(mIntent);
    }
    public void showParking(Parking parking){
        Intent mIntent = new Intent ( mContext, MainActivity.class);
        mIntent.setAction(IntentActions.ACTION_SHOW_SELECTED_PARKING);
        mIntent.putExtra(IntentActions.EXTRA_PARKING, parking);
        startActivity(mIntent);

    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver_update_favorites,
                new IntentFilter(getString(R.string.receiver_update_favorites)));

        EasyTracker.getInstance().activityStart(this);

    }

    @Override
    protected void onStop() {
        super.onStop();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver_update_favorites);

        EasyTracker.getInstance().activityStop(this);

    }

    private BroadcastReceiver receiver_update_favorites = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            update();

            Debug.log("Favorites","receiver_update_favorites");
        }
    };


}
