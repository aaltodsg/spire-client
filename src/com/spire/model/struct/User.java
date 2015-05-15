package com.spire.model.struct;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.plus.PlusClient;
import com.spire.debug.Debug;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by evgeniy on 19.08.13.
 */
public class User {

    private static final String TAG = "User";

    private String email;
    private String username;
    private LatLng viewport_centre;
    private int zoom = 14;
    private boolean use_defined_point;
    private PlusClient mPlusClient;

    private boolean isAuth = false;

    public User (){
        Debug.log(TAG,"New instance of User");

    }

    public boolean isAuth() {
        return isAuth;
    }

    public void setAuth(boolean auth) {
        isAuth = auth;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setViewport_centre(LatLng viewport_centre) {
        this.viewport_centre = viewport_centre;
    }

    public void setZoom(int zoom) {
        this.zoom = zoom;
    }

    public void setUse_defined_point(boolean use_defined_point) {
        this.use_defined_point = use_defined_point;
    }

    public String getEmail() {
        return email;
    }

    public LatLng getViewport_centre() {
        return viewport_centre;
    }

    public int getZoom() {
        return zoom;
    }

    public boolean isUse_defined_point() {
        return use_defined_point;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public PlusClient getmPlusClient() {
        return mPlusClient;
    }

    public void setmPlusClient(PlusClient mPlusClient) {
        this.mPlusClient = mPlusClient;
    }


    public void save(Context context){
        SharedPreferences pref = context.getSharedPreferences( "com.spire.user", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString ("email", getEmail());
        editor.putString("username",getUsername());


        editor.putFloat("latitude",(float)getViewport_centre().latitude);
        editor.putFloat("longitude",(float)getViewport_centre().longitude);
        editor.putInt("zoom", getZoom());


        editor.commit();
        Debug.log(TAG,"saved: " + getUsername() + " " +getEmail() + " " + getViewport_centre().latitude + " " + getViewport_centre().longitude + " " + getZoom());
    }



    public void restore (Context context){
        SharedPreferences pref = context.getSharedPreferences("com.spire.user", Context.MODE_PRIVATE);
        if (!pref.getString("username","").isEmpty()){
            setUsername(pref.getString("username",""));
        }

        if (!pref.getString("email","").isEmpty()){
            setEmail(pref.getString("email",""));
        }


        //setEmail(pref.getString("email",""));


        LatLng viewportcenter = new LatLng(pref.getFloat("latitude",0),pref.getFloat("longitude",0));
        setViewport_centre(viewportcenter);
        setZoom(pref.getInt("zoom",14));


        /*String json_string = pref.getString("json_profile", "");

        Debug.log("Json_profile", json_string);

        if (!json_string.isEmpty()){
            try {
                parse_check_Profile(new JSONArray(json_string));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }*/

        Debug.log(TAG,"restored:" + " " + getUsername() + " " + getEmail() + " " + getViewport_centre().longitude + " " + getViewport_centre().latitude);

    }

    public void addUserProfile(Context context,String json_profile){
        SharedPreferences pref = context.getSharedPreferences( "com.spire.user", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString ("json_profile", json_profile);
        editor.commit();

        Debug.log(TAG,"added profile");

    }

    public void parse_check_Profile(JSONArray jsonArray){
        try {
            JSONObject jsonObject = jsonArray.getJSONObject(0);
            if (jsonObject.isNull(JsonVars.registration_uri)){
                Debug.log(TAG, "profileCheck_onResponse: "+jsonObject.toString());
                setUse_defined_point(jsonObject.getBoolean(JsonVars.use_defined_point));
                setViewport_centre(new LatLng(
                        jsonObject.getJSONArray(JsonVars.viewport_centre).getJSONArray(0).getDouble(1),
                        jsonObject.getJSONArray(JsonVars.viewport_centre).getJSONArray(1).getDouble(1))
                );
                setZoom(jsonObject.getJSONArray(JsonVars.viewport_centre).getJSONArray(2).getInt(1));
            }

            Debug.log(TAG, "profileCheck_onResponse: "+getViewport_centre().latitude + " " + getViewport_centre().longitude);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    }
