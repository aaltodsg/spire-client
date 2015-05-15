package com.spire.model;

import java.util.TimerTask;

// Timer class to avoid flooding of ViewUpdate requests in Communications.java

/**
 * Created by evgeniy on 04.09.13.
 */
public class RequestTimerUpdate extends TimerTask{


    private String last_request_url;
    private InterfaceTimerTick tick;

    public RequestTimerUpdate(Object context) {
        tick = (Communications) context;
    }

    @Override
    public void run() {

        tick.run();
    }

    public void setLast_request_url(String last_request_url) {
        this.last_request_url = last_request_url;
    }

    public String getLast_request_url() {
        return last_request_url;
    }
}
