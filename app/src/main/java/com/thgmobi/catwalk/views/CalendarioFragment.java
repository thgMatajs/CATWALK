package com.thgmobi.catwalk.views;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.squareup.timessquare.CalendarPickerView;
import com.thgmobi.catwalk.MainActivity;
import com.thgmobi.catwalk.R;
import com.thgmobi.catwalk.models.weather.WeatherData;
import com.thgmobi.catwalk.service.WeatherService;
import com.thgmobi.catwalk.util.Common;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;
import net.steamcrafted.materialiconlib.MaterialIconView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class CalendarioFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private TextView tvUserCity;
    private TextView tvTemp;
    private TextView tvHora;

    private MaterialIconView icWeather;

    private ProgressBar progressBarWeather;

    private CalendarView calendarView;

    private double latitude;
    private double longitude;

    protected GoogleApiClient googleApiClient;
    protected Location lastLocation;

    BroadcastReceiver broadcastReceiver;
    private final SimpleDateFormat simpleDateFormatTime = new SimpleDateFormat("HH:mm");


    public CalendarioFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        initVars();
        initActions();
        startBroadcast();


        return inflater.inflate(R.layout.fragment_calendario, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initVars();
        initActions();
        buildGoogleApiClient();

        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm");

        tvHora.setText(dateFormat.format(new Date()).toString());

        googleApiClient.connect();
        startBroadcast();
    }

    private void startBroadcast() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().compareTo(Intent.ACTION_TIME_TICK) == 0) {
                    tvHora.setText(simpleDateFormatTime.format(new Date()));
                }
            }
        };

        getContext().registerReceiver(broadcastReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
    }

    @Override
    public void onPause() {
        super.onPause();
        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

    @SuppressLint("NewApi")
    private void initVars() {

        tvUserCity = getActivity().findViewById(R.id.calendario_tv_nome_cidade);
        tvTemp = getActivity().findViewById(R.id.calendario_tv_temp);
        tvHora = getActivity().findViewById(R.id.calendario_tv_hora);
        icWeather = getActivity().findViewById(R.id.calendario_iv_clima);
        progressBarWeather = getActivity().findViewById(R.id.calendario_progress_weather);
        calendarView = getActivity().findViewById(R.id.calendario_calendar_v);


    }

    private void initActions() {

    }

    protected synchronized void buildGoogleApiClient() {
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API).build();
        }
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        getLocation();
    }

    private void changeWeatherIcon(String conditions) {


        if (conditions.equalsIgnoreCase("rain")) {
            icWeather.setIcon(MaterialDrawableBuilder.IconValue.WEATHER_POURING);
        } else if (conditions.equalsIgnoreCase("clouds")) {
            icWeather.setIcon(MaterialDrawableBuilder.IconValue.WEATHER_CLOUDY);
        } else if (conditions.equalsIgnoreCase("clear")) {
            icWeather.setIcon(MaterialDrawableBuilder.IconValue.WEATHER_SUNNY);
        } else if (conditions.equalsIgnoreCase("snow")) {
            icWeather.setIcon(MaterialDrawableBuilder.IconValue.WEATHER_SNOWY);
        } else if (conditions.equalsIgnoreCase("fog")) {
            icWeather.setIcon(MaterialDrawableBuilder.IconValue.WEATHER_FOG);
        } else if (conditions.equalsIgnoreCase("hail")) {
            icWeather.setIcon(MaterialDrawableBuilder.IconValue.WEATHER_HAIL);
        } else if (conditions.equalsIgnoreCase("lightning")) {
            icWeather.setIcon(MaterialDrawableBuilder.IconValue.WEATHER_LIGHTNING);
        } else if (conditions.equalsIgnoreCase("partly cloudy")) {
            icWeather.setIcon(MaterialDrawableBuilder.IconValue.WEATHER_PARTLYCLOUDY);
        } else if (conditions.equalsIgnoreCase("windy")) {
            icWeather.setIcon(MaterialDrawableBuilder.IconValue.WEATHER_WINDY);
        }
    }

    private void getLocation() {

        if (ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

        if (lastLocation != null) {
            latitude = lastLocation.getLatitude();
            longitude = lastLocation.getLongitude();

            try {

                Geocoder geocoder = new Geocoder(getActivity().getApplicationContext(), Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                if (addresses.isEmpty()) {
                    tvUserCity.setText("Aguarde...");
                } else {
                    if (addresses.size() > 0) {
                        tvUserCity.setText(addresses.get(0).getLocality());
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            getWeatherUpdateCall(latitude, longitude);
        }
    }

    public void getWeatherUpdateCall(double latitude, double longitude) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Common.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        WeatherService weatherService = retrofit.create(WeatherService.class);
        Call<WeatherData> weatherDataCall = weatherService.getCurrentWeather(latitude, longitude);

        weatherDataCall.enqueue(new Callback<WeatherData>() {
            @Override
            public void onResponse(Call<WeatherData> call, Response<WeatherData> response) {
                double temp = response.body().getMain().getTemp();
                String conditions = response.body().getWeather().get(0).getMain();

                tvTemp.setVisibility(View.VISIBLE);
                icWeather.setVisibility(View.VISIBLE);
                progressBarWeather.setVisibility(View.GONE);
                changeWeatherIcon(conditions);

                temp = temp - 273.15;
                String tempConv = String.format("%.0f", temp)  + "°C";
                tvTemp.setText(tempConv);
                MainActivity.temp = String.valueOf(tempConv);

            }

            @Override
            public void onFailure(Call<WeatherData> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }


    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(getContext(), "Error:" + connectionResult, Toast.LENGTH_SHORT).show();
    }
}
