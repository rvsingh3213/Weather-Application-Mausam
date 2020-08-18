package com.example.mausam;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.example.mausam.CurrentWeatherService.CurrentWeatherCallback;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {

    //reference to CurrentWeatherService
    private CurrentWeatherService currentWeatherService;

    private View weatherContainer;
    private ProgressBar weatherProgressBar;
    private TextView temperature,location,weatherCondition;
    private ImageView weatherConditionIcon;
    private EditText locationField;
    private FloatingActionButton fab;
    private boolean fetchingWeather=false;
    private int textCount=0;
    private String currentLocation="bangalore";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        currentWeatherService=new CurrentWeatherService(this);

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

        searchForWeather(currentLocation);
        //we can not write refreshWeather () and searchForWeather() here
        // as java doesnt support method in a method
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        currentWeatherService.cancel();
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
        private void toggleProgress(final boolean showProgress)
        {
                weatherContainer.setVisibility(showProgress? View.GONE : View.VISIBLE);
                weatherProgressBar.setVisibility(showProgress? View.VISIBLE: View.GONE);

        }
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
}

