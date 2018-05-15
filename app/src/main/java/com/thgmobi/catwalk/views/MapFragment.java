package com.thgmobi.catwalk.views;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.thgmobi.catwalk.R;
import com.thgmobi.catwalk.adapters.CustomInfoWindowAdapter;
import com.thgmobi.catwalk.adapters.PlaceAutocompleteAdapter;
import com.thgmobi.catwalk.helper.CheckPermissions;
import com.thgmobi.catwalk.models.PlaceInfo;
import com.thgmobi.catwalk.util.Common;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static android.support.constraint.Constraints.TAG;


public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener,
GoogleApiClient.ConnectionCallbacks{

    private AutoCompleteTextView edtSearch;

    private ImageButton btnMyLocation;
    private ImageButton btnPlaceInfo;
    private ImageButton btnPlacePicker;

    private PlaceAutocompleteAdapter placeAutocompleteAdapter;
    private PlaceInfo placeInfo;
    private GoogleMap mMap;
//    private boolean mLocationPermissionsGranted = false;
    private static GoogleApiClient googleApiClient;
    private Marker marker;
    private CheckPermissions checkPermissions = new CheckPermissions();

    private static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(new LatLng(-40, -168),
            new LatLng(71, 136));

    public MapFragment() {
        // Required empty public constructor
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (Common.mLocationPermissionsGranted) {

            getDevicePosition();

            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        googleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(Places.GEO_DATA_API).addApi(Places.PLACE_DETECTION_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();


        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initVars();
        initActions();
        googleApiClient.connect();
        checkPermissions.checkLocationPermission(getContext(), getActivity());
        if (Common.mLocationPermissionsGranted){
            initMap();

        }else{
            checkPermissions.checkLocationPermission(getContext(), getActivity());
        }

    }

    private void initMap(){
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void initVars() {

        edtSearch = getActivity().findViewById(R.id.map_edt_search);
        btnMyLocation = getActivity().findViewById(R.id.map_btn_myLocation);
        btnPlaceInfo = getActivity().findViewById(R.id.map_btn_placeinfo);
        btnPlacePicker = getActivity().findViewById(R.id.map_btn_placePick);

        placeAutocompleteAdapter = new PlaceAutocompleteAdapter(getContext(),googleApiClient, LAT_LNG_BOUNDS, null );

    }

    private void initActions(){
        edtSearch.setAdapter(placeAutocompleteAdapter);
        edtSearch.setOnItemClickListener(autocompleteClickListener);

        edtSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {

                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER){

                    //executa o metodo de buscar endereço:
                    searchAddress(edtSearch.getText().toString(), null);

                    return true;

                }

                return false;
            }
        });

        btnMyLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                getDevicePosition();

                getDevicePosition();
            }
        });

        btnPlaceInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    if (marker.isInfoWindowShown()){
                        marker.hideInfoWindow();
                    }else{
                        marker.showInfoWindow();
                    }
                }catch (NullPointerException e){
                    e.getMessage();
                }
            }
        });

        btnPlacePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    startActivityForResult(builder.build(getActivity()), Common.PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });

        hideSoftKeyboard();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode != RESULT_OK){
            if (requestCode == Common.PLACE_PICKER_REQUEST){
                try{
                    Place place = PlacePicker.getPlace(getContext(), data);

                    PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                            .getPlaceById(googleApiClient, place.getId());
                    placeResult.setResultCallback(updatePlaceDetailsCallback);
                }catch (NullPointerException e){
                    e.getMessage();
                }

            }

        }else{
            Toast.makeText(getContext(), "Erro", Toast.LENGTH_SHORT).show();
        }
    }

    private void searchAddress(String searchString, PlaceInfo placeInfo){

        Geocoder geocoder = new Geocoder(getContext());
        List<Address> list = new ArrayList<>();
        try{
            list = geocoder.getFromLocationName(searchString, 1);

        }catch (IOException e){
            Log.e(TAG, e.getMessage());
        }

        if (list.size() > 0){
            Address address = list.get(0);


            if (placeInfo == null){
                moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), Common.DEFAULT_ZOOM,
                        address.getAddressLine(0));
            }else{
                moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), Common.DEFAULT_ZOOM,
                        placeInfo);
            }

            edtSearch.setText("");

        }
    }

    private void getDevicePosition() {

        FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());

        try {
            if (Common.mLocationPermissionsGranted) {
                final Task location = fusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Location currentLocation = (Location) task.getResult();

                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
                                    Common.DEFAULT_ZOOM, getString(R.string.minha_localizacao));

                        } else {
                            Toast.makeText(getContext(), "Não foi possivel localizar", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

        } catch (SecurityException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private void moveCamera(LatLng latLng, float zoom, PlaceInfo placeInfo) {

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        mMap.clear();

        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(getActivity()));
        if (placeInfo != null){
            try{
                String snippet = "Endereço: " + placeInfo.getAddress() + "\n" +
                        "Telefone: " + placeInfo.getPhoneNumber() + "\n" +
                        "Classificação: " + placeInfo.getRating() + "\n";

                MarkerOptions options = new MarkerOptions()
                        .position(latLng)
                        .title(placeInfo.getName())
                        .snippet(snippet);

                marker = mMap.addMarker(options);

            }catch (NullPointerException e){
                e.getMessage();
            }
        }else{
            mMap.addMarker(new MarkerOptions().position(latLng));
        }
        hideSoftKeyboard();
    }

    private void moveCamera(LatLng latLng, float zoom, String title) {

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        if (!title.equals(getString(R.string.minha_localizacao))){

            MarkerOptions options = new MarkerOptions()
                    .position(latLng)
                    .title(title);
            mMap.addMarker(options);

        }

        hideSoftKeyboard();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        Common.mLocationPermissionsGranted = false;

        switch (requestCode){
            case Common.LOCATION_PERMISSION_REQUEST_CODE:{
                if (grantResults.length > 0){
                    for (int i = 0; i< grantResults.length; i++){
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            Common.mLocationPermissionsGranted = false;
                            return;
                        }
                    }
                    Common.mLocationPermissionsGranted = true;
                    initMap();

                }
            }
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(getContext(), "Erro:" + connectionResult, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Toast.makeText(getContext(), "Connected", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(getContext(), "onConnectionSuspended", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onStop() {
        super.onStop();

        googleApiClient.disconnect();
    }

    private void hideSoftKeyboard(){


        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }


    //
    private AdapterView.OnItemClickListener autocompleteClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            hideSoftKeyboard();

            final AutocompletePrediction item = placeAutocompleteAdapter.getItem(i);
            final String placeId = item.getPlaceId();

            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(googleApiClient, placeId);
            placeResult.setResultCallback(updatePlaceDetailsCallback);
        }
    };
    private ResultCallback<PlaceBuffer> updatePlaceDetailsCallback = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(@NonNull PlaceBuffer places) {
            if (!places.getStatus().isSuccess()){
                places.release();
                return;
            }
            final Place place = places.get(0);


            try {
                placeInfo = new PlaceInfo();
                placeInfo.setName(place.getName().toString());
                placeInfo.setAddress(place.getAddress().toString());
//                placeInfo.setAttributions(place.getAttributions().toString());
                placeInfo.setId(place.getId());
                placeInfo.setLatLng(place.getLatLng());
                placeInfo.setWebSite(place.getWebsiteUri());
                placeInfo.setPhoneNumber(place.getPhoneNumber().toString());
                placeInfo.setRating(place.getRating());
            }catch (NullPointerException e){
                e.getMessage();
            }

            searchAddress(placeInfo.getAddress(), placeInfo);
            edtSearch.setText("");
            places.release();

        }
    };



}
