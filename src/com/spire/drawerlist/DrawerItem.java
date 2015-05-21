package com.spire.drawerlist;

/* Copyright (C) Aalto University 2014
 *
 * Created with IntelliJ IDEA.
 * User: volodymyr
 * E-mail: volodymyr.n.paliy@gmail.com
 * Date: 04.07.13
 * Time: 16:00
 */
public class DrawerItem {

    private String mFieldName;
    private int mImageResource;

    public String getFieldName (){
        return this.mFieldName;
    }

    public int getImage (){
        return this.mImageResource;
    }

    public void SetFieldName ( String mFieldName ){
        this.mFieldName = mFieldName;
    }

    public void SetImage ( int iImage ){
        this.mImageResource = iImage;
    }

    public DrawerItem ( int iImageRes, String mFieldName ){
    	
        this.mImageResource = iImageRes;
        this.mFieldName = mFieldName;
    }
}
