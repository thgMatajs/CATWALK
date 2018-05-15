package com.thgmobi.catwalk;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.thgmobi.catwalk.helper.CheckPermissions;
import com.thgmobi.catwalk.util.Common;
import com.thgmobi.catwalk.views.CalendarioFragment;
import com.thgmobi.catwalk.views.CameraFragment;
import com.thgmobi.catwalk.views.LoginActivity;
import com.thgmobi.catwalk.views.MapFragment;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private Context context;

    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
    private CheckPermissions checkPermissions = new CheckPermissions();
    public static String temp = "Isso é um teste";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        verificaUserLogado();
        if (isServicesGoogleOK()) {
            initVars();
            initActions();
            checkPermissions.checkLocationPermission(this, this);


            getSupportFragmentManager().beginTransaction().replace(R.id.container,
                    new CalendarioFragment()).commit();
        }


    }

    private void initVars() {

        context = getBaseContext();
        BottomNavigationView navigationView = findViewById(R.id.btn_nav_main);
        navigationView.setOnNavigationItemSelectedListener(this);
    }

    private void initActions() {

    }

    private void verificaUserLogado() {
        if (firebaseUser == null) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }
    }

    public boolean isServicesGoogleOK() {
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);

        if (available == ConnectionResult.SUCCESS) {
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, available, Common.ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        Fragment selectedFragment = null;

        switch (item.getItemId()) {

            case R.id.btn_nav_camera: {
                selectedFragment = new CameraFragment();
                break;
            }
            case R.id.btn_nav_calendar: {
                selectedFragment = new CalendarioFragment();
                break;
            }
            case R.id.btn_nav_location: {
                selectedFragment = new MapFragment();
                break;
            }


        }

        getSupportFragmentManager().beginTransaction().replace(R.id.container,
                selectedFragment).commit();

        return true;

    }

}
