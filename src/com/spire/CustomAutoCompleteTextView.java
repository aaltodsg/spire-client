package com.spire;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AutoCompleteTextView;
import com.spire.debug.Debug;

/* Copyright (C) Aalto University 2014
 *
 * Created by volodymyr on 12.08.13.
 */
public class CustomAutoCompleteTextView extends AutoCompleteTextView {

    private static final String TAG = "CustomAutoCompleteTextView";

    //was the text just cleared?
    boolean justCleared = false;


    public Drawable imgClearIcon = getResources().getDrawable(R.drawable.navigation_cancel);

    public CustomAutoCompleteTextView(Context context) {
        super(context);
        Debug.log(TAG, "Instance created with context.");
        init();
    }

    /* Required methods, not used in this implementation */
    public CustomAutoCompleteTextView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        Debug.log(TAG, "Instance created with context, attrs, defStyle.");
        init();
    }
    /* Required methods, not used in this implementation */
    public CustomAutoCompleteTextView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        Debug.log(TAG, "Instance created with context, attrs.");
        init();
    }

    void init()
    {
        Debug.log(TAG, "init() started.");

        // Set the bounds of the button
        this.setCompoundDrawablesWithIntrinsicBounds(null, null, imgClearIcon, null);

        // button should be hidden on first draw
        clrButtonHandler();

        //if the clear button is pressed, clear it. Otherwise do nothing
        this.setOnTouchListener(new OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {

                CustomAutoCompleteTextView et = CustomAutoCompleteTextView.this;

                if (et.getCompoundDrawables()[2] == null)
                    return false;

                if (event.getAction() != MotionEvent.ACTION_UP)
                    return false;

                if (event.getX() > et.getWidth() - et.getPaddingRight() - imgClearIcon.getIntrinsicWidth())
                {
                    et.setText("");
                    CustomAutoCompleteTextView.this.clrButtonHandler();
                    justCleared = true;
                }
                return false;
            }
        });
    }

    void clrButtonHandler()
    {
        Debug.log(TAG, "clrButtonHandler");

        if (this == null || this.getText().toString().equals("") || this.getText().toString().length() == 0)
        {
            //Log.d("CLRBUTTON", "=cleared");
            //remove clear button
            this.setCompoundDrawables(null, null, null, null);
        }
        else
        {
            //Log.d("CLRBUTTON", "=not_clear");
            //add clear button
            this.setCompoundDrawablesWithIntrinsicBounds(null, null, imgClearIcon, null);
        }
    }
}


