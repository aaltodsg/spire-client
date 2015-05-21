package com.spire.favorites;

/* Copyright (C) Aalto University 2014
 *
 * Created with IntelliJ IDEA.
 * User: volodymyr
 * E-mail: volodymyr.n.paliy@gmail.com
 * Date: 10.07.13
 * Time: 10:22
 */
public class FavoritesItem {

    //const

    public FavoritesItem ( int mImageResource, String mTitle, String mSubTitle, int mImageDestResource ){
        this.mImageResource = mImageResource;
        this.mTitle = mTitle;
        this.mSubTitle = mSubTitle;
        this.mImageDestResource = mImageDestResource;

    }

    private String mTitle;
    private String mSubTitle;
    private int mImageResource; // Nav.point - park icon etc.
    private int mImageDestResource;



    //getters;
    public String getTite(){
        return this.mTitle;
    }

    public String getSubTitle (){
        return this.mSubTitle;
    }

    public int getImageResource (){
        return this.mImageResource;
    }

    public int getImageDestResource (){
        return mImageDestResource;
    }

    //setters;

    public void setTitle( String newTitle ){
        this.mTitle = newTitle;
    }

    public void setSubTitle( String newSubTitle ){
        this.mSubTitle = newSubTitle;
    }

    public void setImageResource( int newImageResource ){
        this.mImageResource = newImageResource;
    }

    public void setImageDestResource( int newImageDestResource ){
        this.mImageDestResource = newImageDestResource;
    }





}
