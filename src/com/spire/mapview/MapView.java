package com.spire.mapview;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.spire.R;
import com.spire.debug.Debug;
import com.spire.model.Communications;
import com.spire.model.orm.HelperFactory;
import com.spire.model.struct.MapMarker;
import com.spire.model.struct.Parking;
import com.spire.parking_details.ParkingDetails;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: volodymyr
 * E-mail: volodymyr.n.paliy@gmail.com
 * Date: 10.07.13
 * Time: 11:22
 */
public class MapView extends SupportMapFragment implements com.google.android.gms.maps.GoogleMap.OnMapClickListener,  OnMarkerClickListener {

    private static final String TAG = "MapView";
    /***/


    Communications communications;
    /***/
    //private LocationManager mLocationManager = null;
    //private LocationListener mLocationListener = null;
    private String provider = null;
    //

    Marker mCurrentDestination;
    Circle mCurrentDestCircle;

    Marker mCurrentPark;

    /******///FIXNE:
    /**/
    private ViewGroup infoWindow;
    private TextView infoTitle;
    private TextView infoSnippet;
    //    private ImageView infoButton;
    private ImageView infoImage;

//    private OnInfoWindowElemTouchListener infoButtonListener;


    static private GoogleMap mMap;
    private SupportMapFragment mFragment;

    //Locations;

    private int zoom;

    public static Map<LatLng, MapMarker> markerMap;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        Debug.log(TAG, "onCreate");


        communications = Communications.getInstance(getActivity().getApplicationContext());

        zoom = communications.getUser().getZoom();



        HelperFactory.SetHelper(getActivity().getApplicationContext());
        markerMap =  new HashMap<>();


        this.infoWindow = (ViewGroup)getActivity().getLayoutInflater().inflate(R.layout.map_custom_info_window, null);
        this.infoTitle = (TextView)infoWindow.findViewById(R.id.title);
        this.infoSnippet = (TextView)infoWindow.findViewById(R.id.snippet);
        // TODO: Check what iv_map_info_window_image is used for. It doesn't exist in the layout, which is inflated
        // MJR: It exists in another layout map_custom_info_window_dest, but not the current one?!?!
        this.infoImage = (ImageView ) infoWindow.findViewById(R.id.iv_map_info_window_image);


        LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).registerReceiver(receiver_profile_check,
                new IntentFilter(getString(R.string.receiver_profile_check)));
        LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).registerReceiver(updateMap,
                new IntentFilter(getString(R.string.receiver_update)));

    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Debug.log(TAG, "onCreateView");

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();

        Debug.log(TAG, "onStart (show CurrentDestination, if needed)");

        if ( mCurrentDestination != null && mCurrentDestination.isInfoWindowShown() ) {
            mCurrentDestination.hideInfoWindow();
            mCurrentDestination.showInfoWindow();

        }
    }



    public void addParkings(){
        ArrayList<Parking> parkings = communications.getParkingArrayList();



        ArrayList<LatLng> marker_lat_lng = new ArrayList<LatLng>();

        ArrayList<LatLng> marker_lat_lng_for_delete = new ArrayList<LatLng>();



        for (Parking current_parking : parkings){
            marker_lat_lng.add(new LatLng(current_parking.getLatitude(),current_parking.getLongitude()));
        }

        for (LatLng marker_position : markerMap.keySet()){
            if (!marker_lat_lng.contains(marker_position)){
                marker_lat_lng_for_delete.add(marker_position);
            }
        }


        for (LatLng del : marker_lat_lng_for_delete){
            Debug.log(TAG, "addParkings / delete: "+markerMap.get(del).getParking().getParkid());
            markerMap.get(del).getMarker().remove();
            markerMap.remove(del);
        }



        if (parkings != null){
            for (Parking parking : parkings){
                addMarker(parking);

            }

        }

    }


    private void addMarker(Parking parking){

        BitmapDescriptor icon;

        if (parking.getStatus() != null){
            switch (parking.getStatus()){
                case "Available":
                    icon = BitmapDescriptorFactory.fromResource(R.drawable.green_icon);
                    break;
                case "AlmostFull":
                    icon = BitmapDescriptorFactory.fromResource(R.drawable.orange_icon);
                    break;
                case "Full":
                    icon = BitmapDescriptorFactory.fromResource(R.drawable.red_icon);
                    break;
                case "Unknown":
                    icon = BitmapDescriptorFactory.fromResource(R.drawable.green_orange_icon);
                    break;
                default:
                    icon = BitmapDescriptorFactory.fromResource(R.drawable.green_orange_icon);
            }
        } else {
            icon = BitmapDescriptorFactory.fromResource(R.drawable.green_orange_icon);
        }


        LatLng position = new LatLng(parking.getLatitude(),parking.getLongitude());

        if (markerMap.get(position) == null){
            Marker otaniemi = mMap.addMarker(
                    new MarkerOptions().position(
                            position)
                            .title(
                                    parking.getParkid()).snippet(""));
            otaniemi.setIcon(icon);


            MapMarker newMarker = new MapMarker();
            newMarker.setMarker(otaniemi);
            newMarker.setParking(parking);
            markerMap.put(position, newMarker);



            Debug.log(TAG, "addMarker / details: key: " + position.latitude + " " + otaniemi.getPosition().latitude);

        }

        Debug.log(TAG, "addMarker / parking id: "+parking.getParkid());
    }



    private void checkLocationService(){
        LocationManager lm = null;
        boolean gps_enabled = false,network_enabled = false;
        if(lm == null)
            lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        try{
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }catch(Exception ex){}
        try{
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        }catch(Exception ex){}

        AlertDialog.Builder dialog;
        if(!gps_enabled && !network_enabled){
            dialog = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), android.R.style.Theme_Holo_Light_Dialog));
            dialog.setTitle(R.string.alert_main_location_title);
            dialog.setCancelable(false);
            dialog.setMessage(getActivity().getResources().getString(R.string.alert_main_location_message));
            dialog.setPositiveButton(getActivity().getResources().getString(R.string.alert_main_btn_positive), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                }
            });
            dialog.setNegativeButton(getActivity().getString(R.string.alert_main_btn_negative), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                   paramDialogInterface.cancel();

                }
            });
            dialog.show();

        }

    }


    public boolean SetUpTheMap (){

        Debug.log(TAG, "SetUpTheMap");

        // Debug.log(TAG, "isGooglePlayServicesAvailable: " + GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity()));
        /**/
        mMap = getMap();
        // Debug.log(TAG, "mMap: " + mMap);

        if ( mMap != null ){

            mMap.setOnMarkerClickListener(this);

            mMap.setMyLocationEnabled(true);

            getMap().setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                @Override
                public boolean onMyLocationButtonClick() {

                    checkLocationService();

                    return false;
                }
            });

            mMap.getUiSettings().setMyLocationButtonEnabled(true);

            mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
                @Override
                public void onMyLocationChange(Location location) {
                    checkLocationService();
                }
            });


            // Debug.log(TAG,Communications.getInstance(getActivity()).getUser()==null?"null":"not null");
            // Debug.log(TAG, "MapActivity_user returns zoom level: "+communications.getUser().getZoom()+ "");

            communications.getUser().restore(getActivity().getApplicationContext());

            getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(
                    communications.getUser().getViewport_centre(),
                    communications.getUser().getZoom()));

            mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                @Override
                public View getInfoWindow(Marker marker) {
                    ContextThemeWrapper cw = new ContextThemeWrapper( getActivity().getApplicationContext(), R.style.Transparent);

                    LayoutInflater inflater = ( LayoutInflater ) cw.getSystemService(getActivity().getApplicationContext().LAYOUT_INFLATER_SERVICE);
                    View layout = null;
                    if ( mCurrentDestination != null && mCurrentDestination.getId().equals( marker.getId() ) ){
                        layout = inflater.inflate(R.layout.map_custom_info_window_dest, null);

                        infoImage = (ImageView)layout.findViewById(R.id.iv_map_info_window_image);

                        LatLng position = new LatLng (Communications.round(marker.getPosition().latitude),Communications.round(marker.getPosition().longitude));

                        Parking currentParking = new Parking();
                        currentParking.setLatitude(position.latitude);
                        currentParking.setLongitude(position.longitude);
                        currentParking.setIsParking(false);
                        currentParking.setParkid(marker.getTitle());
                        currentParking.setEmail(communications.getUser().getEmail());


                        if ( isFavoritesParking ( currentParking )  ){
                            Debug.log(TAG, "setted not rating");
                            infoImage.setImageResource(R.drawable.rating_important);
                        }else{

                            Debug.log(TAG, "setted rating");
                            infoImage.setImageResource(R.drawable.rating_not_important);
                        }

                    }else{

                        layout = inflater.inflate(R.layout.map_custom_info_window, null);
                    }
                    //set up the information here!
                    infoTitle = (TextView)layout.findViewById(R.id.title);
                    infoSnippet = (TextView)layout.findViewById(R.id.snippet);

                    infoTitle.setText(marker.getTitle());
                    infoSnippet.setText(marker.getSnippet());

                    return layout;
                }

                @Override
                public View getInfoContents(Marker marker) {

                    return infoWindow;
                }
            });

            mMap.setOnInfoWindowClickListener( new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {
                    if ( mCurrentDestination != null && marker.getId().equals( mCurrentDestination.getId() ) ){

                        LatLng position = new LatLng (Communications.round(marker.getPosition().latitude),Communications.round(marker.getPosition().longitude));


                        Parking currentParking = new Parking();
                        currentParking.setLatitude(position.latitude);
                        currentParking.setLongitude(position.longitude);
                        currentParking.setIsParking(false);
                        currentParking.setParkid(marker.getTitle());

                        currentParking.setEmail(communications.getUser().getEmail());

                        Debug.log(TAG, "currentParking: " + currentParking);



                        // if true - not rated;
                        if ( !isFavoritesParking ( currentParking ) ) {

                            addToFavorites(currentParking);

                        }else {

                            removeFromFavorites (currentParking);

                        }
//
                        mCurrentDestination.showInfoWindow();


                    }else {
                        Intent mIntent = new Intent ( getActivity().getApplicationContext(), ParkingDetails.class);
                        mIntent.putExtra("check",false);

                        mIntent.putExtra("parking",
                                markerMap.get(
                                        new LatLng(Communications.round(marker.getPosition().latitude),
                                                Communications.round(marker.getPosition().longitude)
                                        )
                                ).getParking());

                        startActivity(mIntent);
                    }
                }
            });

            mMap.setOnMapClickListener( this );

            return true;

        }

        return false;

    }


    private boolean isFavoritesParking(Parking parking){

        boolean stat = false;
        try {
            stat = HelperFactory.GetHelper().getmParkingDAO().isFavoritesParking(parking);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stat;
    }

    private void addToFavorites(Parking parking){
        try {
            HelperFactory.GetHelper().getmParkingDAO().create(parking);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void removeFromFavorites(Parking parking){
        try {
            Parking parkingForDelete = HelperFactory.GetHelper().getmParkingDAO().getParkingForCoordinates(communications.getUser().getEmail(), parking.getLatitude(), parking.getLongitude() );
            HelperFactory.GetHelper().getmParkingDAO().delete(parkingForDelete);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void HideCurrentInfoWindow (){
        if (mMap != null )
            if (
                    mCurrentPark != null &&
                    mCurrentPark.isInfoWindowShown() ) {

                mCurrentPark.hideInfoWindow();
                setSmallIcon(mCurrentPark);

                mCurrentPark = null;


            } else if (
                    mCurrentDestination != null &&
                    mCurrentDestination.isInfoWindowShown() ) {

                mCurrentDestination.hideInfoWindow();
            }
    }

    public void CreateDestinationMarker ( LatLng point, String name ){


        HideCurrentInfoWindow();

        String markerName;

        if ( name != null )
            markerName = name;
        else
            markerName = getString(R.string.map_destination_address);
        if ( mCurrentDestination != null ){
            //delete
            mCurrentDestination.remove();

        }
        if ( mCurrentDestCircle != null ){
            mCurrentDestCircle.remove();
        }
        //set
        mCurrentDestination = mMap.addMarker(
                new MarkerOptions()
                        .position(point)
                        .title(markerName)
                        .snippet("")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.pointer))
        );

        CircleOptions circleOptions = new CircleOptions()
                .center(point)
                .radius(100)
                .strokeWidth(2);

        mCurrentDestCircle = mMap.addCircle(circleOptions);
        mCurrentDestCircle.setFillColor(getResources().getColor(R.color.transparent_light));

        mCurrentDestination.showInfoWindow();
        try {
            communications.getDistance(
                    new LatLng(
                            mMap.getMyLocation().getLatitude(),
                            mMap.getMyLocation().getLongitude()),
                    mCurrentDestination.getPosition(),mCurrentDestination);
        } catch (Exception e ){
        }


        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point, zoom));
//		mCurrentDestination.set
    }


    public void CreateDestinationMarker ( LatLng point){

        HideCurrentInfoWindow();
//        mMap.in

        if ( mCurrentDestination != null ){
            //delete
            mCurrentDestination.remove();

        }
        if ( mCurrentDestCircle != null ){
            mCurrentDestCircle.remove();
        }

        mCurrentDestination = mMap.addMarker(
                new MarkerOptions()
                        .position(point)
                        .title(getString(R.string.map_destination_address))
                        .snippet("")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.pointer))
        );

        CircleOptions circleOptions = new CircleOptions()
                .center(point)
                .radius(100)
                .strokeWidth(2);

        mCurrentDestCircle = mMap.addCircle(circleOptions);
        mCurrentDestCircle.setFillColor(getResources().getColor(R.color.transparent_light));


		mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point, zoom));

    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Debug.log(TAG, "onActivityCreated");

        if ( SetUpTheMap() ){


            getMap().setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                @Override
                public void onCameraChange(CameraPosition cameraPosition) {
                    Debug.log(TAG, "onActivityCreated: "+cameraPosition.toString() + " " +
                            getMap().getProjection().getVisibleRegion().nearLeft + " " +
                            getMap().getProjection().getVisibleRegion().farRight + " "
                    );

                    communications.updateParkings(
                            getMap().getProjection().getVisibleRegion().nearLeft,
                            getMap().getProjection().getVisibleRegion().farRight);
                }
            });

        } else {
            Debug.log(TAG, "onActivityCreated return false");
        }
    }


    @Override
    public boolean onMarkerClick(Marker marker) {

        setUpTheIcon( marker );

        try {
            communications.getDistance(
                    new LatLng(
                            mMap.getMyLocation().getLatitude(),
                            mMap.getMyLocation().getLongitude()),
                    marker.getPosition(),marker);
        } catch (Exception e ){
            Debug.log(TAG, "onMarkerClick / Cannot get myLocation exception: " + e.getMessage());
        }

        return false;
    }

    // Enlarge parking lot icon, when clicked on
    private void setUpTheIcon ( Marker marker ){

        // Hide the current info window
        HideCurrentInfoWindow();

        if ( mCurrentDestination == null || !mCurrentDestination.getId().equals(marker.getId()) ){

            if ( mCurrentPark != null  ){
                if ( !mCurrentPark.getId().equals(marker.getId() ) ){
                    // clicked ! on current selected parking;
                    setSmallIcon ( mCurrentPark );

                    mCurrentPark = marker;

                    setBigIcon ( mCurrentPark );

                }
            }
            else {
                //previous was selected nothing;

                mCurrentPark = marker;
                setBigIcon ( mCurrentPark );
                mCurrentPark.showInfoWindow();
            }
        }else{
            //clicked not a parking;
            if ( mCurrentPark != null   ){

                setSmallIcon ( mCurrentPark );
                mCurrentPark = null;
            }
        }

        // show info window for clicked marker;
        marker.showInfoWindow();

    }

    private void setSmallIcon ( Marker marker ){

        LatLng position = new LatLng(Communications.round(marker.getPosition().latitude),Communications.round(marker.getPosition().longitude));

        if (markerMap.containsKey(position)){
            Parking currentParking = markerMap.get(position).getParking();
            BitmapDescriptor icon;

            if (currentParking.getStatus() != null){
                switch (currentParking.getStatus()){
                    case "Available":
                        icon = BitmapDescriptorFactory.fromResource(R.drawable.green_icon);
                        break;
                    case "AlmostFull":
                        icon = BitmapDescriptorFactory.fromResource(R.drawable.orange_icon);
                        break;
                    case "Full":
                        icon = BitmapDescriptorFactory.fromResource(R.drawable.red_icon);
                        break;
                    case "Unknown":
                        icon = BitmapDescriptorFactory.fromResource(R.drawable.green_orange_icon);
                        break;
                    default:
                        icon = BitmapDescriptorFactory.fromResource(R.drawable.green_orange_icon);
                }
            } else {
                icon = BitmapDescriptorFactory.fromResource(R.drawable.green_orange_icon);
            }

            try {
                marker.setIcon(icon);
            } catch (IllegalArgumentException e){
                e.printStackTrace();
            }

        }
    }

    private void setBigIcon ( Marker marker ){

        LatLng position = new LatLng(Communications.round(marker.getPosition().latitude),Communications.round(marker.getPosition().longitude));
        Parking currentParking = markerMap.get(position).getParking();
        BitmapDescriptor icon;

        if (currentParking.getStatus() != null){
            switch (currentParking.getStatus()){
                case "Available":
                    icon = BitmapDescriptorFactory.fromResource(R.drawable.green_icon_black);
                    break;
                case "AlmostFull":
                    icon = BitmapDescriptorFactory.fromResource(R.drawable.orange_icon_black);
                    break;
                case "Full":
                    icon = BitmapDescriptorFactory.fromResource(R.drawable.red_icon_black);
                    break;
                case "Unknown":
                    icon = BitmapDescriptorFactory.fromResource(R.drawable.green_orange_icon_black);
                    break;
                default:
                    icon = BitmapDescriptorFactory.fromResource(R.drawable.green_orange_icon_black);
            }
        } else {
            icon = BitmapDescriptorFactory.fromResource(R.drawable.green_orange_icon_black);
        }
        marker.setIcon(icon);
    }

    @Override
    public void onResume() {
        super.onResume();
        Debug.log(TAG, "onResume (does nothing)");
    }

    @Override
    public void onPause() {
        super.onPause();

        Debug.log(TAG, "onPause (does nothing)");


    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Debug.log(TAG, "onDestroy (unregisters receiver_profile_check and updateMap)");

        LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).unregisterReceiver(receiver_profile_check);
        LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).unregisterReceiver(updateMap);
    }

    public static Location mLocation;

    @Override
    public void onStop() {
        super.onStop();

        Debug.log(TAG, "onStop (does nothing)");

    }

    @Override
    public void onDestroyView() {

        Debug.log(TAG, "onDestroyView (does nothing)");

        super.onDestroyView();
    }

    private BroadcastReceiver updateMap = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            switch (intent.getExtras().getString("cmd")){
                case "update":
                    addParkings();
                    break;
                case "update_status":
                    LatLng coordinates = new LatLng(Communications.round(intent.getExtras().getDouble("latitude")),
                            Communications.round(intent.getExtras().getDouble("longitude")));



                    Debug.log(TAG, "updateMap status");

                    if (markerMap.containsKey(coordinates) /*&& !markerMap.get(coordinates).getParking().getStatus().equals(intent.getExtras().getString("status"))*/){
                        markerMap.get(coordinates).getParking().setStatus(intent.getExtras().getString("status"));


                        Debug.log(TAG, "updateMap new status: "+intent.getExtras().getString("status"));

                        if (markerMap.get(coordinates).getMarker().isInfoWindowShown()){

                            setBigIcon(markerMap.get(coordinates).getMarker());
                            markerMap.get(coordinates).getMarker().showInfoWindow();
                        }else {
                            setSmallIcon(markerMap.get(coordinates).getMarker());
                        }
                    }


                    break;
                default:
            }

        }
    };



    private BroadcastReceiver receiver_profile_check = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

        }
    };

    @Override
    public void onMapClick(LatLng latLng) {
        //CreateDestinationMarker(latLng);

        if ( mCurrentPark != null ){
            setSmallIcon(mCurrentPark);
            mCurrentPark = null;

        }
    }


    public static Location getCurrentLocation (){

        if (mMap!=null)
            mLocation = mMap.getMyLocation();
        return mLocation;
    }

    public void selectParking(Parking parking){

        LatLng position = new LatLng(
                parking.getLatitude(),
                parking.getLongitude());


        if (markerMap.containsKey(position)){
            setUpTheIcon(markerMap.get(position).getMarker());
        } else {
            communications.getParkingArrayList().add(parking);
            addParkings();
            setUpTheIcon(markerMap.get(position).getMarker());
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, zoom));

        try {
            communications.getDistance(
                    new LatLng(
                            mMap.getMyLocation().getLatitude(),
                            mMap.getMyLocation().getLongitude()),
                    markerMap.get(position).getMarker().getPosition(),markerMap.get(position).getMarker());
        } catch (Exception e ){
        }

    }
}
