package com.spire.model;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.nfc.Tag;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.TextView;

import com.android.volley.*;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gcm.GCMRegistrar;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.spire.R;
import com.spire.activity_recognition.ActivityRecognitionIntentService;
import com.spire.debug.Debug;
import com.spire.model.orm.HelperFactory;
import com.spire.model.struct.DistinationMarker;
import com.spire.model.struct.JsonVars;
import com.spire.model.struct.Parking;
import com.spire.model.struct.Place;
import com.spire.model.struct.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Timer;

/* Copyright (C) Aalto University 2014
 *
 * Created by evgeniy on 24.07.13.
 */

public class Communications implements InterfaceTimerTick{

    private User user;

    private static final String TAG = "Communications";
    private RequestQueue queue;
    private Context context;
    private static Communications _instance;

    // MJR: Define slightly more persistent retransmission policy for volley (30 secs timeout, 5 retries)
    private RetryPolicy mVolleyPolicy = new DefaultRetryPolicy(30000,5,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

    private Timer requestTimer;

    private RequestTimerUpdate requestTimerUpdate;

//********************************************
    public ArrayList<Parking> parkingArrayList;

    public ArrayList<Parking> getParkingArrayList() {
        return parkingArrayList;
    }
    public void setParkingArrayList(ArrayList<Parking> parkingArrayList) {
        this.parkingArrayList = parkingArrayList;
        sendNotifyUpdate();
    }

    public void replaceParking(Parking parking){
        for (Parking current_parking : this.parkingArrayList){
            if (current_parking.getArea().equals(parking.getArea())){
                this.parkingArrayList.remove(current_parking);
                this.parkingArrayList.add(parking);
                break;
            }
        }
    }

    public Parking getPatkingForCoordinates(LatLng coordinates){

        for (Parking current_parking : this.parkingArrayList){
            if (current_parking.getLatitude() == coordinates.latitude && current_parking.getLongitude() == coordinates.longitude){
                return current_parking;
            }
        }
        return null;
    }

    public Parking getPatkingForArea(String area){

        for (Parking current_parking : this.parkingArrayList){
            if (current_parking.getArea().equals(area)){
                return current_parking;
            }
        }
        return null;
    }

    public boolean isUserNull(){
        if (user == null) return true;
        else return false;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    //********************************************
    public ArrayList<Place> placeArrayList;

    public void setPlaceArrayList(ArrayList<Place> placeArrayList) {
        this.placeArrayList = placeArrayList;
    }

    public ArrayList<Place> getPlaceArrayList() {
        return this.placeArrayList;
    }

    public ArrayList<String> suggestionArrayList;


    public ArrayList<String> getSuggestionArrayList() {
        return suggestionArrayList;
    }
    public void setSuggestionArrayList(ArrayList<String> suggestionArrayList) {
        this.suggestionArrayList = suggestionArrayList;
    }

    public LatLng placeCoordinats;

    public LatLng getPlaceCoordinats(){
        return placeCoordinats;
    }

    public void setPlaceCoordinats ( LatLng coordinats ){
        placeCoordinats = coordinats;
    }



    public static Communications getInstance(Context context){
        if (_instance == null){
            _instance = new Communications(context);


            Debug.log(TAG,"getInstance: New instance created.");
        } else {
            Debug.log(TAG, "getInstance: Respond with old instance.");
        }


        return _instance;
    }


    public Communications(Context context){
        Debug.log(TAG,"Class constructor executed; trying to restore user context");
        this.context = context;

        queue = Volley.newRequestQueue(context);


        user = new User();
        user.restore(context);


        //getUpdate(null,null,null);

    }

    // MJR: Auxiliary procedure to set a more persistent retransmission policy for volley transmissions to avoid losing data
    // based on: http://stackoverflow.com/questions/21277123/volley-requestqueue-timeout/21762518#21762518
    private void addToVolleyQueue(Request Req) {
        Req.setRetryPolicy(mVolleyPolicy);
        queue.add(Req);
    }

    // ViewUpdate request processing
    // The purpose of the timer solution is to avoid too frequent view updates:
    // Viewupdate parameters replace previous ones until a timer tick, at which point the request is queued
    private String getUpdate(LatLng sw, LatLng ne){

        String url  = context.getResources().getString(R.string.url) +"/viewupdate?user="+getUser().getEmail()+"&sw="+sw.latitude+","+sw.longitude+"&ne="+ne.latitude+","+ne.longitude+"&lang=eng";

        requestTimerUpdate.setLast_request_url(url);

        Debug.log(TAG, "getUpdate url: "+url);

        return null;
    }

    @Override
    public void run() {

        Debug.log(TAG, "TimerUpdate: timer tick ");

        if (requestTimerUpdate.getLast_request_url() != null && requestTimerUpdate.getLast_request_url().length() > 0){

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, requestTimerUpdate.getLast_request_url(), null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Debug.log(TAG,response.toString());
                    setParkingArrayList(getParkingArray(response));
                    sendNotifyStartService(true);
                }
            },new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });
            addToVolleyQueue(jsonObjectRequest);

            Debug.log(TAG, "TimerUpdate: request " + requestTimerUpdate.getLast_request_url());


            requestTimerUpdate.setLast_request_url("");
        }

    }


    public void startRequestTimer(){
        requestTimer = new Timer();
        requestTimerUpdate = new RequestTimerUpdate(this);
        requestTimer.schedule(requestTimerUpdate ,0,1000);
    }

    public void stopRequestTimer(){
        requestTimer.cancel();
    }

    private ArrayList<Parking> getParkingArray (JSONObject jsonObject){

        ArrayList<Parking> parkings = new ArrayList<Parking>();

        try {
            JSONArray bindings = jsonObject.getJSONObject(JsonVars.results).getJSONArray(JsonVars.bindings);
            for (int iter = 0; iter < bindings.length(); iter++){
                JSONObject itemArray = bindings.getJSONObject(iter);
                parkings.add(createParkingObject(itemArray));

                sendNotifyUpdateStatus(createParkingObject(itemArray));

            }
            Debug.log(TAG,"parkings.size(): "+parkings.size());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return parkings;
    }

    private Parking createParkingObject(JSONObject jsonObject) throws JSONException {
        Parking parking = new Parking();

        if (!jsonObject.isNull(JsonVars.areatype))
            parking.setAreatype(jsonObject.getJSONObject(JsonVars.areatype).getString(JsonVars.value));

        if (!jsonObject.isNull(JsonVars.parkid))
            parking.setParkid(jsonObject.getJSONObject(JsonVars.parkid).getString(JsonVars.value));

        if (!jsonObject.isNull(JsonVars.area))
            parking.setArea(jsonObject.getJSONObject(JsonVars.area).getString(JsonVars.value));

        if (!jsonObject.isNull(JsonVars.longitude))
            parking.setLongitude(round(jsonObject.getJSONObject(JsonVars.longitude).getDouble(JsonVars.value)));

        if (!jsonObject.isNull(JsonVars.latitude))
            parking.setLatitude(round(jsonObject.getJSONObject(JsonVars.latitude).getDouble(JsonVars.value)));

        if (!jsonObject.isNull(JsonVars.status))
            parking.setStatus(jsonObject.getJSONObject(JsonVars.status).getString(JsonVars.value));

        if (!jsonObject.isNull(JsonVars.size))
            parking.setSize(jsonObject.getJSONObject(JsonVars.size).getInt(JsonVars.value));

        if (!jsonObject.isNull(JsonVars.info))
            parking.setInfo(jsonObject.getJSONObject(JsonVars.info).getString(JsonVars.value));

        if (!jsonObject.isNull(JsonVars.radius))
            parking.setRadius(jsonObject.getJSONObject(JsonVars.radius).getInt(JsonVars.value));

        parking.setEmail(getUser().getEmail());

        return parking;
    }

    public void updateParkings(LatLng sw, LatLng ne){
        getUpdate(sw,ne);
    }

    private void sendNotifyUpdate(){
        Intent intent = new Intent(context.getString(R.string.receiver_update));
        intent.putExtra("cmd","update");
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public void getDistance(LatLng from, LatLng to,final  Object marker){
        String url = context.getString(R.string.url_google_api);

        url = url+"/json?origin="+from.latitude+","+from.longitude+"&destination="+to.latitude+","+to.longitude+"&sensor=true";


        Debug.log(TAG, "getDistance url: " + url);

        JsonObjectRequest jsonObjectRequestRoute = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>(){
            @Override
            public void onResponse(JSONObject jsonObject) {
                //sendNotifyUpdateDistance(getDistanceFromJson(jsonObject));

                double distance = getDistanceFromJson(jsonObject);


                Debug.log(TAG, "Distance: "+jsonObject.toString());

                DecimalFormat df = new DecimalFormat("#.#");

                String text_distance = "";
                if ( distance != 0 ){
                    if (distance > 1000) text_distance+=df.format(distance/1000) + " km";
                    if (distance < 1000) text_distance+=df.format(distance) + " m";
                }


                if (marker instanceof Marker){
                    ((Marker)marker).setSnippet(text_distance);
                    if (((Marker) marker).isInfoWindowShown()){
                        ((Marker)marker).showInfoWindow();
                    }
                }
                if (marker instanceof TextView){
                    ((TextView) marker).setText(text_distance);
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        });

        addToVolleyQueue(jsonObjectRequestRoute);
    }

    private int getDistanceFromJson(JSONObject jsonObject){

        int distance = 0;
        if (!jsonObject.isNull(JsonVars.status)){
            try {
                if (jsonObject.getString(JsonVars.status).equals(JsonVars.g_api_status_ok)){
                    distance = jsonObject.getJSONArray(JsonVars.g_api_routes).getJSONObject(0).getJSONArray(JsonVars.g_api_legs).getJSONObject(0).getJSONObject(JsonVars.g_api_distance).getInt(JsonVars.value);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return distance;
    }

    private void sendNotifyUpdateDistance(int distance){
        Intent intent = new Intent(context.getString(R.string.receiver_update_distance));
        intent.putExtra(JsonVars.g_api_distance,distance);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public static double round(double value) {
        int scale = 6;
        return Math.round(value * Math.pow(10, scale)) / Math.pow(10, scale);
    }

    public void getSearchSuggestions(Location current_location, int radius, String query ){


        final Location  mLocation = current_location;
        final int mRadius = radius;
        String url = "https://maps.googleapis.com/maps/api/place/autocomplete/json?" +
                "input="+query+
                "&types=geocode"+
                "&location="+mLocation.getLatitude()+","+mLocation.getLongitude()+
                "&radius="+mRadius+
                "&sensor="+true+
                "&key=" + context.getString(R.string.google_api_search_key) +
                "&language=en";

        Debug.log (TAG, "getSearchSuggestions Places url: " + url );
        JsonObjectRequest jsonObjectRequestRoute = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>(){

            @Override
            public void onResponse(JSONObject jsonObject) {

                setSuggestionArrayList ( getSuggestionList (jsonObject) );
                Debug.log(TAG, "setSuggestionArrayList: completed");

                sendNotifyUpdateList();


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        });

        addToVolleyQueue(jsonObjectRequestRoute);

    }

    private ArrayList<String> getSuggestionList ( JSONObject jsonObject ){

        ArrayList<String> suggestions = new ArrayList<String>();

        try {

            JSONArray place = jsonObject.getJSONArray(JsonVars.g_place_predictions);
            // get next page token


            for (int iter = 0; iter < place.length(); iter++){
                JSONObject itemArray = place.getJSONObject(iter);
                suggestions.add(createSuggestionObject(itemArray));

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return suggestions;

    }

    private String createSuggestionObject(JSONObject jsonObject) throws JSONException {
        return (jsonObject.getString(JsonVars.g_place_description));
    }

    private String getPageToken ( JSONObject jsonObject ) {
        String str = null;

        try {

            if ( ! jsonObject.isNull(JsonVars.g_place_next_page_token))
                str = jsonObject.getString(JsonVars.g_place_next_page_token);
        }catch ( JSONException e ){
            Debug.log (TAG, "getPageToken ERROR: " + e.getMessage() );
            return null;
        }

        return str;
    }
    //local variable for check the availability of next page with results;
    private String strRefToNextPage = null;

    private ArrayList<Place> getPlacesList ( JSONObject jsonObject ){

        ArrayList<Place> places = new ArrayList<Place>();

        try {

            //
            // JSONArray place = jsonObject.getJSONArray(JsonVars.results);
            JSONArray place = jsonObject.getJSONArray(JsonVars.g_place_predictions);
            // get next page token


            for (int iter = 0; iter < place.length(); iter++){
                JSONObject itemArray = place.getJSONObject(iter);
                places.add(createPlaceObject(itemArray));

            }

            //strRefToNextPage = getPageToken ( jsonObject );



        } catch (JSONException e) {
            e.printStackTrace();
        }
        return places;

    }

    private Place createPlaceObject(JSONObject jsonObject) throws JSONException {
        Place place = new Place();

        Debug.log(TAG, "Places / createPlaceObject");

        if (!jsonObject.isNull(JsonVars.name)){
//            place.setName(jsonObject.getJSONObject(JsonVars.name).getString(JsonVars.value));
            place.setName(jsonObject.getString(JsonVars.name));
        }

/**/
//        if (!jsonObject.isNull(JsonVars.longitude)){
            place.setLongitude(round(jsonObject.getJSONObject(JsonVars.geometry).getJSONObject(JsonVars.location).getDouble(JsonVars.g_place_longitude)));
//            place.setLongitude(round(jsonObject.getJSONObject(JsonVars.longitude).getDouble(JsonVars.value)));
//        }

//        if (!jsonObject.isNull(JsonVars.latitude)){
            place.setLatitude(round(jsonObject.getJSONObject(JsonVars.geometry).getJSONObject(JsonVars.location).getDouble(JsonVars.g_place_latitude)));
//            place.setLatitude(round(jsonObject.getJSONObject(JsonVars.latitude).getDouble(JsonVars.value)));
//        }

//        strRefToNextPage = (jsonObject.getJSONObject(JsonVars.geometry).getJSONObject(JsonVars.location).getString(JsonVars.g_place_longitude));


/**/
        return place;
    }

    public void getPlaceCoordinats ( String address ){

        address = address.replace(" ", "+");

        try {
            address = URLEncoder.encode(address, "utf-8");

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String url = "https://maps.googleapis.com/maps/api/geocode/json?" +
                "address="+address+
                "&sensor="+true+
                "&language=en";


        Debug.log (TAG, "getPlaceCoordinats url: " + url );
        JsonObjectRequest jsonObjectRequestRoute = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>(){
            @Override
            public void onResponse(JSONObject jsonObject) {
                DistinationMarker distinationMarker = getPlaceCoodinats(jsonObject);

                Debug.log(TAG, "getPlaceCoordinats onResponse >>>"+jsonObject.toString());


                if (distinationMarker != null){
                    setPlaceCoordinats (distinationMarker.getCoordinats());
                    sendNotifyUpdateList(context.getString(R.string.receiver_update_search_dest_marker), distinationMarker.getAreaid() );
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        });

        addToVolleyQueue(jsonObjectRequestRoute);

//        return getPlaceArrayList();


    }

    private DistinationMarker getPlaceCoodinats (JSONObject jsonObject) {

        Debug.log(TAG, "getPlaceCoodinats json: "+jsonObject.toString());

        DistinationMarker distinationMarker = null;
        try {
            JSONArray results = jsonObject.getJSONArray(JsonVars.results);
            distinationMarker =  createPlaceCoodinatsObject(results.getJSONObject(0));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return distinationMarker;

    }

    private DistinationMarker createPlaceCoodinatsObject(JSONObject jsonObject) throws JSONException {

        DistinationMarker distinationMarker = new DistinationMarker();
        distinationMarker.setCoordinats(new LatLng(
                round(jsonObject.getJSONObject(JsonVars.geometry).getJSONObject(JsonVars.location).getDouble(JsonVars.g_place_latitude)),
                round(jsonObject.getJSONObject(JsonVars.geometry).getJSONObject(JsonVars.location).getDouble(JsonVars.g_place_longitude))
        ));

        distinationMarker.setAreaid(jsonObject.getString(JsonVars.g_formatted_address));

        return distinationMarker;


    }

    private void sendNotifyUpdateList( String extras, String areaid ){

        Intent intent = new Intent(context.getString(R.string.receiver_update_search_list));
        if ( extras != null )
            intent.putExtra( extras, areaid );

        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private void sendNotifyUpdateList( ){

        Intent intent = new Intent(context.getString(R.string.receiver_update_search_list));
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }


    public void setNewAreaStatus(String status,String area){
        String url = context.getString(R.string.url);
        url+="/newareastatus?user="+getUser().getEmail()+"&area="+area+"&status="+status+"&lang=eng";

        Debug.log(TAG, "setNewAreaStatus: "+url);

        JsonObjectRequest jsonObjectRequestRoute = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>(){
            @Override
            public void onResponse(JSONObject jsonObject) {
                Debug.log(TAG, "setNewAreaStatus jsonObject: "+jsonObject.toString());

                if (getParkingArray(jsonObject).size() > 0){
                    Parking parking = getParkingArray(jsonObject).get(0);
                    if (getParkingArrayList() != null)
                        replaceParking(parking);
                    sendNotifyUpdateStatus(parking);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        });

        addToVolleyQueue(jsonObjectRequestRoute);
    }

    public void updateAreaStatus(String area, String stat){
        if (parkingArrayList != null){
            Parking parking = getPatkingForArea(area);
            if (parking != null){
                parking.setStatus(stat);
                sendNotifyUpdateStatus(parking);
                Debug.log(TAG, "updateAreaStatus: "+parking.getStatus());
            }
        }
    }

    private void sendNotifyUpdateStatus(Parking parking){

        if (parking.getStatus() != null){
            Intent intent = new Intent(context.getString(R.string.receiver_update));
            intent.putExtra("cmd","update_status");
            intent.putExtra("status", parking.getStatus());

            intent.putExtra("latitude", parking.getLatitude());
            intent.putExtra("longitude", parking.getLongitude());


            intent.putExtra("parking",parking);

            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }
    }


    //Profile check
    public void profileCheck(String gcmRegId){
        String url = context.getString(R.string.url);
        url += "/profilecheck?user="+getUser().getEmail()+"&gcm_reg_id="+gcmRegId;

        Debug.log(TAG, "profileCheck url: "+url);


        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(url,new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray jsonArray) {
                Debug.log(TAG, "profileCheck_onResponse jsonArray:"+jsonArray.toString());


                    try {
                        JSONObject jsonObject = jsonArray.getJSONObject(0);

                        if (jsonObject.isNull(JsonVars.registration_uri)){

                            Debug.log(TAG, "profileCheck_onResponse checkProfile"+jsonObject.toString());

                            getUser().setUse_defined_point(jsonObject.getBoolean(JsonVars.use_defined_point));
                            getUser().setViewport_centre(new LatLng(
                                jsonObject.getJSONArray(JsonVars.viewport_centre).getJSONArray(0).getDouble(1),
                                jsonObject.getJSONArray(JsonVars.viewport_centre).getJSONArray(1).getDouble(1))
                            );

                            getUser().setZoom(jsonObject.getJSONArray(JsonVars.viewport_centre).getJSONArray(2).getInt(1));

                            Debug.log(TAG, "profileCheck_onResponse: "+user.getViewport_centre().latitude  + " " + user.getViewport_centre().longitude);

                            getUser().setAuth(true);

                            //getUser().save(context);

                            sendNotifyCheckProfile();
                            sendNotifyStartService(true);

                        } else {

                            getUser().setAuth(false);
                            sendNotifyCheckProfile(jsonObject.getString(JsonVars.registration_uri));
                        }

                      } catch (JSONException e) {
                      e.printStackTrace();
                      }


            }
        },new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        });


        queue.add(jsonArrayRequest); // MJR: This crashes if used through addToVolleyQueue?!?! - keep as is...


    }
    //Profile check


    //Parking procedure

    public void sendGeofenceCrossed(String[] area, String directrion){

        if (getUser() != null){
            for (int index = 0; index < area.length ; index++) {

                String url = context.getString(R.string.url);
                url+="/geofencecrossed?user="+getUser().getEmail()+"&area="+area[index]+"&direction="+directrion;

                Debug.log(TAG, "sendGeofenceCrossed index: " + index + " url: "+ url);

                JsonObjectRequest jsonObjectRequestRoute = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>(){

                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        Debug.log(TAG, "sendGeofenceCrossed jsonObject: "+jsonObject.toString());
                        parseParkedStatus(jsonObject);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {

                    }
                });

                addToVolleyQueue(jsonObjectRequestRoute);
            }
        }

    }

    public void sendTrackCoordinate(LatLng location){
        String url = context.getString(R.string.url);

        url+="/userlocation?user="+getUser().getEmail()+"&location="+location.latitude+","+location.longitude;

        Debug.log(TAG, "sendTrackCoordinate url: "+url);

        JsonObjectRequest jsonObjectRequestRoute = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>(){

            @Override
            public void onResponse(JSONObject jsonObject) {
                Debug.log(TAG, "sendTrackCoordinate jsonObject: "+jsonObject.toString());

                parseParkedStatus(jsonObject);
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        });

        addToVolleyQueue(jsonObjectRequestRoute);
    }

    public void sendUserActivity(String userActivity){
        String url = context.getString(R.string.url);


        url+="/useractivity?user="+getUser().getEmail()+"&activity="+userActivity;

        JsonObjectRequest jsonObjectRequestRoute = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>(){

            @Override
            public void onResponse(JSONObject jsonObject) {
                Debug.log(TAG, "sendUserActivity jsonObject: "+jsonObject.toString());
                parseParkedStatus(jsonObject);
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        });

        Debug.alert(context,url);


        addToVolleyQueue(jsonObjectRequestRoute);

    }

    public void parseParkedStatus(JSONObject js){
        Debug.log(TAG, "parseParkedStatus jsonObject: "+js.toString());

        try {
            JSONArray bindings = js.getJSONObject(JsonVars.results).getJSONArray(JsonVars.bindings);

            String message = "";
            if (!bindings.getJSONObject(0).isNull("status")){
                message += bindings.getJSONObject(0).getJSONObject("status").getString(JsonVars.value);

                sendNotifyStartService(getStatService(bindings.getJSONObject(0).getJSONObject(JsonVars.status).getString(JsonVars.value)));

            }
            if (!bindings.getJSONObject(0).isNull("reason")){

                message += " " +bindings.getJSONObject(0).getJSONObject("reason").getString(JsonVars.value);

                if (bindings.getJSONObject(0).getJSONObject(JsonVars.reason).getString(JsonVars.value).equals("Parked") && !isParked){
                    new com.spire.Notification( context,
                            getPatkingForArea(bindings.getJSONObject(0).getJSONObject("area").getString(JsonVars.value))
                    );
                    isParked = true;
                }

            }
            if (!bindings.getJSONObject(0).isNull("area")){
                message += " " +bindings.getJSONObject(0).getJSONObject("area").getString(JsonVars.value);
            }


            Intent intent = new Intent(context.getString(R.string.receiver_parked_notif));
            intent.putExtra("message", message);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);


        } catch (JSONException e) {
            e.printStackTrace();
        }


    }
    private boolean isParked = false;

    private boolean getStatService(String stat){
        boolean status = false;
        switch (stat){
            case "Active":
                status = true;
                isParked = false;
                break;
            case "Passive":
                status = false;
                break;
        }
        return status;
    }

    private void sendNotifyStartService(boolean stat){
        Intent intent = new Intent(context.getString(R.string.receiver_service_start));
        intent.putExtra("state",stat);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private void sendNotifyCheckProfile(String uri){
        Intent intent = new Intent(context.getString(R.string.receiver_profile_check));
        intent.putExtra("auth",false);
        intent.putExtra(JsonVars.registration_uri,uri);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private void sendNotifyCheckProfile(){
        Intent intent = new Intent(context.getString(R.string.receiver_profile_check));
        intent.putExtra("auth",true);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }


    public void getAreaUpdate(String area){
        String url = context.getString(R.string.url);
        url+="/areaupdate?user="+getUser().getEmail()+"&area="+area+"&lang=eng";

        Debug.log(TAG, "getAreaUpdate url: "+url);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Debug.log(TAG, "response jsonObject: "+response.toString());


                ArrayList<Parking> update = getParkingArray(response);

                if (update.size() > 0){
                    sendNotifyUpdateArea(update.get(0));
                }
            }
        },new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        addToVolleyQueue(jsonObjectRequest);
    }

    public void getAreaStatus(ArrayList<String> areas){
        String url = context.getString(R.string.url);

        if (areas.size() > 0){
            StringBuilder sb_area = new StringBuilder();

            for (String area : areas){
                sb_area.append(area);
                sb_area.append(",");
            }
            sb_area.deleteCharAt(sb_area.length()-1);


            url+="/areaupdate?user="+getUser().getEmail()+"&area="+sb_area+"&lang=eng";

            Debug.log(TAG, "getAreaStatus url: " + url);


            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Debug.log(TAG,"getAreaStatus response jsonObject: "+response.toString());


                    ArrayList<Parking> update = getParkingArray(response);

                    HelperFactory.SetHelper(context);

                    if (update.size() > 0){

                        try {
                            HelperFactory.GetHelper().getmParkingDAO().updateParking(update,getUser().getEmail());
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }

                        sendNotifyUpdateFavorites();


                    }
                }
            },new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });
            addToVolleyQueue(jsonObjectRequest);
        }
    }


    public void sendNotifyUpdateFavorites(){
        Intent intent = new Intent(context.getString(R.string.receiver_update_favorites));
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

    }

    public void sendNotifyUpdateArea(Parking parking){
            Intent intent = new Intent(context.getString(R.string.receiver_update));
            intent.putExtra("cmd","update_area");
            intent.putExtra("parking",parking);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public void testRequestSurvey(){
        String url = context.getString(R.string.url);

        String surevey_url = "";
        try {
            surevey_url = URLEncoder.encode("http://erdetfredag.dk/", "utf-8"); //URLDecoder.decode("http://erdetfredag.dk/", "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


        url+="/surveytest?registration-id="+GCMRegistrar.getRegistrationId(context)+"&survey-uri="+ surevey_url;//surevey_url;


        Debug.log(TAG, "testRequestSurvey url: "+url);

        JsonObjectRequest jsonObjectRequestRoute = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>(){
            @Override
            public void onResponse(JSONObject jsonObject) {
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        });

        addToVolleyQueue(jsonObjectRequestRoute);

    }


    public void logout(){
        String url = context.getString(R.string.url);
        url+="/logout?user="+getUser().getEmail();

        JsonObjectRequest jsonObjectRequestRoute = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>(){
            @Override
            public void onResponse(JSONObject jsonObject) {

                Debug.log(TAG,"logout jsonObject: " +  jsonObject.toString());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        });

        addToVolleyQueue(jsonObjectRequestRoute);

        Debug.log(TAG,"logout url: " +  url);

    }

}
