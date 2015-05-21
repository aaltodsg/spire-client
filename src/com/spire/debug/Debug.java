package com.spire.debug;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

/* Copyright (C) Aalto University 2014
 *
 * Created by evgeniy on 17.08.13.
 */

public class Debug {
    private static boolean isDebug = false; // MJR: Was false
    private static boolean debugActivity = true; // MJR: special toasts for activity recognition debugging
    private static boolean showLog = false;
    private static boolean showToast = true; // MJR: Was false
    private static boolean showDialog = false; // MJR: Was false




    public static void toast(Context context, String message){
        if (isDebug){
            if (showToast){
                Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }

    // MJR added 4.3.2014 to debug activity recognition
    public static void activityToast(Context context, String message){
        if (debugActivity){
            if (showToast){
                Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }


    public static void log(String tag, String message){
        if (isDebug){
            if (showLog){
                Log.i(tag,message);
            }
        }
    }

    public static void alert(Context context, String message){
        if (isDebug){
            if (showDialog){
                Intent intent = new Intent("android.intent.action.MAIN");
                intent.setClass(context, ServiceDialog.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("text", message);
                context.startActivity(intent);
            }
        }
    }

}
