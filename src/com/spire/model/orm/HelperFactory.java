/* Copyright (C) Aalto University 2014 */

package com.spire.model.orm;

import android.content.Context;

import com.j256.ormlite.android.apptools.OpenHelperManager;



public class HelperFactory{

    private static DatabaseHelper databaseHelper;

    public static DatabaseHelper GetHelper(){
        return databaseHelper;
    }
    public static void SetHelper(Context context){
        databaseHelper = OpenHelperManager.getHelper(context,DatabaseHelper.class);
    }
    public static void ReleaseHelper(){
        OpenHelperManager.releaseHelper();
        databaseHelper = null;
    }
}