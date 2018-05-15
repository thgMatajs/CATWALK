package com.thgmobi.catwalk.util;


import android.Manifest;


public class Common {

    public static final String CHILD_DB_USER = "user";
    public static final String CHILD_DB_PHOTO = "photo";
    public static final String CHILD_DB_USERID = "userId";

    public static final String BASE_URL = "http://api.openweathermap.org/data/2.5/";

    public static final float DEFAULT_ZOOM = 15f;
    public static final int GOOGLE_SIGNIN_CODE = 9;
    public static final int LOCATION_PERMISSION_REQUEST_CODE = 8;
    public static final int ERROR_DIALOG_REQUEST = 9001;
    public static final int PLACE_PICKER_REQUEST = 1;
    public static final int CAMERA_PERMISSION_REQUEST_CODE = 7;

    public static boolean mLocationPermissionsGranted = false;

    public static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    public static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    public static final String CAMERA = Manifest.permission.CAMERA;
    public static final String WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    public static final String READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE;


}
