package com.thgmobi.catwalk.helper;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.thgmobi.catwalk.util.Common;

public class CheckPermissions {

    public void checkLocationPermission(Context context, Activity activity) {

        String[] permissions = {Common.FINE_LOCATION,
                Common.COURSE_LOCATION};

        if (ContextCompat.checkSelfPermission(context.getApplicationContext(), Common.FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(context.getApplicationContext(), Common.COURSE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED) {

                Common.mLocationPermissionsGranted = true;


            } else {
                ActivityCompat.requestPermissions(activity, permissions,
                        Common.LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(activity, permissions,
                    Common.LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    public boolean checkCameraPermission(Context context, Activity activity) {

        String[] permissions = {Common.CAMERA, Common.WRITE_EXTERNAL_STORAGE, Common.READ_EXTERNAL_STORAGE};

        if (ContextCompat.checkSelfPermission(context.getApplicationContext(),
                Common.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(context.getApplicationContext(),
                    Common.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(context.getApplicationContext(),
                            Common.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

                return true;

            }else {
                ActivityCompat.requestPermissions(activity, permissions, Common.CAMERA_PERMISSION_REQUEST_CODE);
                return false;

            }
        }else {
            ActivityCompat.requestPermissions(activity, permissions, Common.CAMERA_PERMISSION_REQUEST_CODE);
            return false;

        }
    }

}
