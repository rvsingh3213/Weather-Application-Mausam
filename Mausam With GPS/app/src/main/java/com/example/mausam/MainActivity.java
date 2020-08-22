package com.example.mausam;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.example.mausam.CurrentWeatherService.CurrentWeatherCallback;

import com.example.mausam.SearchWeatherCurrentLocation.CurrentWeatherCallbackGPS;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {

    //reference to CurrentWeatherService
    private CurrentWeatherService currentWeatherService;
    private SearchWeatherCurrentLocation searchWeatherCurrentLocation;
    private View weatherContainer;
    private ProgressBar weatherProgressBar;
    private TextView temperature,location,weatherCondition;
    private ImageView weatherConditionIcon;
    private EditText locationField;
    private FloatingActionButton fab;
    private boolean fetchingWeather=false;
    private int textCount=0;
    private String currentLocation="London"; //location will be updated by GPS

    //for getting Latitude and Longitude
    Double latitude,longitude;
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int UPDATE_INTERVAL = 5000; // 5 seconds

    FusedLocationProviderClient locationProviderClient;
    LocationRequest locationRequest;
    LocationCallback locationCallback;
    private Location currentLocationGPS;

    private int LOCATION_PERMISSION = 100;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
       // startGettingLocation();
        currentWeatherService=new CurrentWeatherService(this);
        searchWeatherCurrentLocation=new SearchWeatherCurrentLocation(this);
        temperature=(TextView)findViewById(R.id.temperature);
        location=(TextView)findViewById(R.id.location);
        weatherCondition=(TextView)findViewById(R.id.weather_condition);
        weatherConditionIcon=(ImageView)findViewById(R.id.weather_condition_icon);
        locationField=(EditText)findViewById(R.id.location_field);
        fab=(FloatingActionButton)findViewById(R.id.fab);

        weatherContainer=(View) findViewById(R.id.weather_container);
        weatherProgressBar=(ProgressBar) findViewById(R.id.weather_progress_bar);



        locationField.addTextChangedListener(new TextWatcher(){
            @Override
            public void beforeTextChanged(CharSequence s,int start,int count,int after){}

            @Override
            public void onTextChanged(CharSequence s,int start,int before,int count){
            count=s.toString().trim().length();
            fab.setImageResource(count==0?R.drawable.ic_refresh : R.drawable.ic_search);
            textCount=count;
            }
            @Override
            public void afterTextChanged(Editable s){}
        });




        fab.setOnClickListener(new View.OnClickListener() {
          @Override
            public void onClick(View v)   {
                    if(textCount==0)
                        refreshWeather();
                    else
                    {
                        searchForWeather(locationField.getText().toString());
                        locationField.setText("");
                    }
            }
        });

        locationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationCallback = new LocationCallback(){
            @Override
            public void onLocationAvailability(LocationAvailability locationAvailability) {
                super.onLocationAvailability(locationAvailability);
                if(locationAvailability.isLocationAvailable()){
                    Log.i(TAG,"Location is available");
                }else {
                    Log.i(TAG,"Location is unavailable");
                }
            }

            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Log.i(TAG,"Location result is available");
            }
        };

        //will call startGettingLocation to initialize Latitude and Longitude
       /* inside this method we have called our searchMethod at Current GPS position*/
       startGettingLocation();

    //Log.i(TAG,"RVpoint");
   // Log.i(TAG,"latm:"+latitude+" "+longitude);
    //   searchForCurrentLocationWeather();
       // Log.i(TAG,"RVpoint After");
    //   searchForWeather(currentLocation);
        //we can not write refreshWeather () and searchForWeather() here
        // as java doesnt support method in a method
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        currentWeatherService.cancel();
        searchWeatherCurrentLocation.cancel();
        stopLocationRequests();
    }

    private  void refreshWeather(){
         if(fetchingWeather)
         {    return;   }
         searchForWeather(currentLocation);
          // Toast.makeText(MainActivity.this,"Refresh!",Toast.LENGTH_SHORT).show();

        }

        private void searchForWeather(@NonNull final String location)
        {
         toggleProgress(true);
         fetchingWeather=true;
         currentWeatherService.getCurrentWeather(location,currentWeatherCallback);
         //   Toast.makeText(MainActivity.this,"Searching for "+location,Toast.LENGTH_SHORT).show();
        }
        // search for CurrentLocationWeather
        private void searchForCurrentLocationWeather()
        {
            toggleProgress(true);
            fetchingWeather=true;
            System.out.println("befre passing "+latitude+" "+longitude);
            searchWeatherCurrentLocation.getCurrentWeather(latitude,longitude,currentWeatherCallbackGPS);
        }
        private void toggleProgress(final boolean showProgress)
        {
                weatherContainer.setVisibility(showProgress? View.GONE : View.VISIBLE);
                weatherProgressBar.setVisibility(showProgress? View.VISIBLE: View.GONE);

        }
        //callback for Searched locationWeather
        private final CurrentWeatherCallback currentWeatherCallback=new CurrentWeatherCallback() {
            @Override
            public void onCurrentWeather(@NonNull CurrentWeather currentWeather) {
                    currentLocation=currentWeather.location;
                    temperature.setText(String.valueOf(currentWeather.getTempFahrenheit()));
                    location.setText(currentWeather.location);
                    weatherCondition.setText(currentWeather.weatherCondition);
                    weatherConditionIcon.setImageResource(CurrentWeatherUtils
                            .getWeatherIconResId(currentWeather.conditionId));
                    toggleProgress(false);
                    fetchingWeather=false;
            }

            @Override
            public void onError(@Nullable Exception exception) {
                toggleProgress(false);
                fetchingWeather=false;
                Toast.makeText(MainActivity.this,"There was an error fetching weather, "
                            +"try Again!",Toast.LENGTH_SHORT).show();

            }
        };

        //callback for CurrentLocation Weather

    private final CurrentWeatherCallbackGPS currentWeatherCallbackGPS=new CurrentWeatherCallbackGPS() {
        @Override
        public void onCurrentWeather(@NonNull CurrentWeather currentWeather) {
            currentLocation=currentWeather.location;
            temperature.setText(String.valueOf(currentWeather.getTempFahrenheit()));
            location.setText(currentWeather.location);
            weatherCondition.setText(currentWeather.weatherCondition);
            weatherConditionIcon.setImageResource(CurrentWeatherUtils
                    .getWeatherIconResId(currentWeather.conditionId));
            toggleProgress(false);
            fetchingWeather=false;
        }

        @Override
        public void onError(@Nullable Exception exception) {
            toggleProgress(false);
            fetchingWeather=false;
            Toast.makeText(MainActivity.this,"There was an error fetching weather, "
                    +"try Again!",Toast.LENGTH_SHORT).show();

        }
    };

    //for Latitude and Longitude

    private void startGettingLocation() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)== PackageManager.PERMISSION_GRANTED){
            locationProviderClient.requestLocationUpdates(locationRequest,locationCallback, MainActivity.this.getMainLooper());
            locationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    currentLocationGPS = location;
                    latitude=currentLocationGPS.getLatitude();//textViewLatitude.setText(""+currentLocation.getLatitude());
                    longitude=currentLocationGPS.getLongitude();//textViewLongitude.setText(""+currentLocation.getLongitude());
                    Log.i(TAG,"lat:"+latitude+" lon"+longitude);
                   // Log.i(TAG,"lon:"+longitude);
                  //  System.out.println("print lat"+latitude+" lon:"+longitude);

                    searchForCurrentLocationWeather();
                }
            });

            locationProviderClient.getLastLocation().addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.i(TAG, "Exception while getting the location: "+e.getMessage());
                }
            });


        }else {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){
                Toast.makeText(MainActivity.this, "Permission needed", Toast.LENGTH_LONG).show();
            } else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[] { Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        LOCATION_PERMISSION);
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        startGettingLocation();

    }
    //OnDestroy stopLocationRequests
    private void stopLocationRequests(){
        locationProviderClient.removeLocationUpdates(locationCallback);
    }
    //END for Latitude


}

