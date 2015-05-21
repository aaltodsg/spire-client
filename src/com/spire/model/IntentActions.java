package com.spire.model;

/* Copyright (C) Aalto University 2014
 *
 * Created by volodymyr on 08.08.13.
 */
public class IntentActions {

    /* LOG IN */
    //action
    public static final String ACTION_LOG_IN = "log_in";

    //extras
    public static final String EXTRA_ACCOUNT = "account";

    /* END: LOG IN */


    /* CREATE DEST MARKER */
    //action
    public static final String ACTION_CREATE_DESTINATION_MARKER = "create_destination_marker"; // send this action to map View for create the destination marker;
    public static final String ACTION_SHOW_SELECTED_PARKING = "show_selected_parking";

    /* SHOW AVAIL...*/
    public static final String ACTION_SHOW_AVAILABILITY_OF_SPACE = "SHOW_AVAILABILITY_OF_SPACE";


    // extras;
    public static final String EXTRA_POSITION = "position"; // position of marker
    public static final String EXTRA_MARKER_NAME = "marker_name"; // title of marker

    public static final String EXTRA_PARKING = "parking"; // title of marker


}
