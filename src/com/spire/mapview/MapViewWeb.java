package com.spire.mapview;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.spire.R;

/* Copyright (C) Aalto University 2014
 *
 * Created with IntelliJ IDEA.
 * User: volodymyr
 * E-mail: volodymyr.n.paliy@gmail.com
 * Date: 08.07.13
 * Time: 13:45
 */
public class MapViewWeb extends Fragment {

    private WebView webView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mainView = (View) inflater.inflate(R.layout.map_view, container, false);
        WebView webView = (WebView) mainView.findViewById(R.id.map_view);
//        webView.loadUrl("http://www.google.com");

//        String customHtml = "<html><body><h1>Hello, WebView</h1></body></html>";
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        webView.loadData(readTextFromResource(R.raw.custom), "text/html", "UTF-8");


        return mainView;


    }

    private String readTextFromResource(int resourceID)
    {
        InputStream raw = getResources().openRawResource(resourceID);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        int i;
        try
        {
            i = raw.read();
            while (i != -1)
            {
                stream.write(i);
                i = raw.read();
            }
            raw.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return stream.toString();
    }



}
