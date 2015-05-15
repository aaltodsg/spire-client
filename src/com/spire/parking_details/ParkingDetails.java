package com.spire.parking_details;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.gcm.GCMRegistrar;
import com.google.android.gms.maps.model.LatLng;
import com.spire.FragmentActivitySurvey;
import com.spire.MainActivity;
import com.spire.R;
import com.spire.authentication.AuthActivity;
import com.spire.debug.Debug;
import com.spire.mapview.MapView;
import com.spire.model.Communications;
import com.spire.model.IntentActions;
import com.spire.model.orm.HelperFactory;
import com.spire.model.struct.Parking;

import java.sql.SQLException;
import java.util.ArrayList;

public class ParkingDetails extends AuthActivity {


    Button btnAvailability;
    Button btnGetDirection;

    ArrayList<ListItems> mListItems;

    Adapter mAdapter;
    Parking currentParking;

    TextView txt_title;
    TextView txt_info;
    TextView txt_information;

    TextView txt_availability_of_spaces;
    TextView txt_parking_capacity;

    private void fillData(){
        mListItems.add ( new ListItems( R.drawable.green_icon, getString(R.string.parking_space_avalable_plenty) ) );
        mListItems.add ( new ListItems( R.drawable.orange_icon, getString(R.string.parking_space_avalable_some) ) );
        mListItems.add ( new ListItems( R.drawable.red_icon, getString(R.string.parking_space_avalable_full) ) );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //LocalBroadcastManager.getInstance(this).registerReceiver(receiver_check_profile, new IntentFilter(getString(R.string.receiver_profile_check)));


        setContentView(R.layout.activity_parking_details);


        HelperFactory.SetHelper(this);


        setCurrentParking(((Parking)getIntent().getExtras().getSerializable("parking")));

        final LatLng position = new LatLng(currentParking.getLatitude(),currentParking.getLongitude());
        Location user_location = MapView.getCurrentLocation();



        txt_title = (TextView)findViewById(R.id.txt_header_title);
        txt_info = (TextView)findViewById(R.id.txt_header_info);
        txt_information = (TextView)findViewById(R.id.txt_info);



        update_parking();



//        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setDisplayShowHomeEnabled(false);
        getActionBar().setDisplayShowTitleEnabled(true);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle(getString(R.string.parking_details));




        btnAvailability = (Button) findViewById(R.id.btn_availability_of_spaces);

        btnAvailability.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowDialogAvailabilityOfSpavce( v.getContext() );
            }
        });

        btnGetDirection = (Button) findViewById(R.id.btn_get_direction);

        btnGetDirection.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                    if ( position != null ) {
                        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                                Uri.parse("http://maps.google.com/maps?daddr=" + position.latitude + "," + position.longitude));
                                intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                        startActivity(intent);
                    }
            }
        });

        Intent intent = getIntent();

        if ( intent.getAction() != null && intent.getAction().equals(IntentActions.ACTION_SHOW_AVAILABILITY_OF_SPACE)){
            ShowDialogAvailabilityOfSpavce( this );
        }



        LatLng user_position = null;
        if (user_location != null){
            user_position = new LatLng(user_location.getLatitude(),user_location.getLongitude());
            Communications.getInstance(getApplicationContext()).getDistance(
                    user_position, new LatLng(getCurrentParking().getLatitude(),getCurrentParking().getLongitude()), txt_info);
        }

    }

    private void ShowDialogAvailabilityOfSpavce ( Context context ){
        mListItems = new ArrayList<ListItems>();

        fillData();

        AlertDialog.Builder bulder = new AlertDialog.Builder(context);
                bulder  .setTitle(currentParking.getParkid())

        .setCancelable(true)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });


        mAdapter = new Adapter ( context, mListItems );
        bulder.setAdapter(mAdapter, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });



        final AlertDialog alert = bulder.create();
        alert.getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Debug.log("onItemClick",i+"");
                String status = "Unknown";
                switch (i){
                    case 0:
                        status = "Available";
                        break;
                    case 1:
                        status = "AlmostFull";
                        break;
                    case 2:
                        status = "Full";
                        break;
                }

                Communications.getInstance(getApplicationContext()).setNewAreaStatus(status,currentParking.getArea());
                alert.cancel();
            }
        });

        alert.show();
    }


    // almost the same method used at MapView;
    public boolean IsLocationServiceDisabled(){
        LocationManager lm = null;
        boolean gps_enabled = false,network_enabled = false;
        if(lm == null)
            lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        try{
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }catch(Exception ex){}
        try{
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        }catch(Exception ex){}

        AlertDialog.Builder dialog;
        if(!gps_enabled && !network_enabled){
            dialog = new AlertDialog.Builder( this );
//            dialog.set0
            dialog.setTitle(R.string.alert_main_location_title);
            dialog.setCancelable(true);

            dialog.setMessage(getResources().getString(R.string.alert_main_location_message));
            dialog.setPositiveButton(getResources().getString(R.string.alert_main_btn_positive), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                }
            });
            dialog.setNegativeButton(getString(R.string.alert_main_btn_negative), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    paramDialogInterface.cancel();

                }
            });
            dialog.show();

            return true;
        }
        return false;

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.parking_details_action_bar, menu);

        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent mIntent = new Intent ( this, MainActivity.class);
        startActivity( mIntent );

    }

    boolean bIsEdit = true;
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

    	switch ( item.getItemId()){
    	case android.R.id.home:
            Debug.log( "ParkingDetails", "home clicked");
    		onBackPressed();
    		break;
    	case R.id.menu_parking_details_addToFavorites:
    		if ( !isFavoritesParking(currentParking) ){

				item.setIcon(R.drawable.rating_important);
                addToFavorites(currentParking);


            }
			else {
				item.setIcon(R.drawable.rating_not_important);
                removeFromFavorites(currentParking);

                Debug.log("ParkingsDAO", "bIsEdit" );
            }


    		break;
    	default:
    		break;
    		
    	}
    	return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (isFavoritesParking(currentParking)){
            menu.getItem(0).setIcon(R.drawable.rating_important);
        } else {
            menu.getItem(0).setIcon(R.drawable.rating_not_important);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    private int getIconParking(Parking parking){
        int icon;

        if (parking.getStatus() != null){
            switch (parking.getStatus()){
                case "Available":
                    icon = R.drawable.green_mid;
                    break;
                case "AlmostFull":
                    icon = R.drawable.orange_mid;
                    break;
                case "Full":
                    icon = R.drawable.red_mid;
                    break;
                case "Unknown":
                    icon = R.drawable.green_orange_mid;
                    break;
                default:
                    icon = R.drawable.green_orange_mid;
            }
        } else {
            icon = R.drawable.green_orange_mid;
        }
        return icon;

    }


    private String getTextAvailabilityOfSpaces(Parking parking){
        String text = "";
        if (parking.getStatus() != null){
            switch (parking.getStatus()){
                case "Available":
                    text = getString(R.string.parking_space_avalable_plenty);
                    break;
                case "AlmostFull":
                    text = getString(R.string.parking_space_avalable_some);
                    break;
                case "Full":
                    text = getString(R.string.parking_space_avalable_full);
                    break;
                case "Unknown":
                    text = "";
                    break;
                default:
                    text = "";
            }
        } else {
            text = "";
        }
        return text;
    }



    private void addToFavorites(Parking parking){
        try {
            HelperFactory.GetHelper().getmParkingDAO().create(parking);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean isFavoritesParking(Parking parking){

        boolean stat = false;
        try {
            stat = HelperFactory.GetHelper().getmParkingDAO().isFavoritesParking(currentParking);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stat;
    }

    private void removeFromFavorites(Parking parking){
        try {
            Parking parkingForDelete = HelperFactory.GetHelper().getmParkingDAO().getParkingForCoordinates(Communications.getInstance(this).getUser().getEmail(),parking.getLatitude(),parking.getLongitude());
            HelperFactory.GetHelper().getmParkingDAO().delete(parkingForDelete);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setIconParking (Parking parking){
        ImageView img_parking = (ImageView) findViewById(R.id.img_parking_image);
        img_parking.setImageResource(getIconParking(parking));
    }

    public void updateCurrentParking(LatLng position){

        if (currentParking == null){
            HelperFactory.SetHelper(this);
            try {
                currentParking = HelperFactory.GetHelper().getmParkingDAO().getParkingForCoordinates(Communications.getInstance(this).getUser().getEmail(),Communications.round(position.latitude), Communications.round(position.longitude));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }

    private BroadcastReceiver updateMap = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getExtras().getString("cmd").equals("update_status")){

                Parking new_parking = (Parking)intent.getSerializableExtra("parking");
                if (currentParking.getArea().equals(new_parking.getArea())){
                    setCurrentParking((Parking)intent.getSerializableExtra("parking"));

                    update_parking();

                    Debug.log("setIconParking","receiver");

                }
            }
            if (intent.getExtras().getString("cmd").equals("update_area")){
                setCurrentParking((Parking)intent.getSerializableExtra("parking"));
                update_parking();
            }

        }
    };

    private void update_parking(){
        setIconParking(getCurrentParking());
        txt_title.setText(getCurrentParking().getParkid());
        if (getCurrentParking().getInfo() != null){
            txt_information.setText(Html.fromHtml(getCurrentParking().getInfo()));
        }


        txt_availability_of_spaces = (TextView) findViewById(R.id.txt_availability_of_spaces);
        txt_parking_capacity = (TextView) findViewById(R.id.txt_parking_capacity_value);

        txt_availability_of_spaces.setText(getTextAvailabilityOfSpaces(getCurrentParking()));

        if (currentParking.getSize() > 0){
            txt_parking_capacity.setText(String.valueOf(currentParking.getSize()));
        } else {
            ((TextView)findViewById(R.id.txt_parking_capacity)).setVisibility(View.GONE);
            txt_parking_capacity.setVisibility(View.GONE);
        }


        Debug.log("update_parking","update_parking");

    }

    @Override
    protected void onStart() {
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(updateMap,
                new IntentFilter(getString(R.string.receiver_update)));
        super.onStart();

        EasyTracker.getInstance().activityStart(this);

    }

    @Override
    protected void onStop() {
        super.onStop();

        EasyTracker.getInstance().activityStop(this);

    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (com.getUser() != null && com.getUser().getmPlusClient() != null){
            if (com.getUser().getmPlusClient().isConnected()){
                if (!com.getUser().isAuth()){
                    com.profileCheck(GCMRegistrar.getRegistrationId(this));
                    Debug.log("Auth","false");
                }
            }
        }



        Debug.log("Auth","onResume");
    }

    private void showRegisterDialog(String uri){
        FragmentActivitySurvey.showRegisterDialog(uri, this);
    }


    public Parking getCurrentParking() {
        return currentParking;
    }

    public void setCurrentParking(Parking currentParking) {
        this.currentParking = currentParking;
    }
}
