package com.example.coolweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.coolweather.gson.Forecast;
import com.example.coolweather.gson.Weather;
import com.example.coolweather.service.AutoUpdateService;
import com.example.coolweather.util.HttpUtil;
import com.example.coolweather.util.Utility;

import org.w3c.dom.Text;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {
    private ScrollView weatherLayout;
    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInforText;
    private TextView windInforText;
    private LinearLayout forecastLayout;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;
    private ImageView pic_img;
    public SwipeRefreshLayout swipeRefresh;
    public DrawerLayout drawerLayout;
    private Button home;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT>=21){
            View docorView = getWindow().getDecorView();
            docorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);
        weatherLayout = (ScrollView)findViewById(R.id.weather_layout);
        titleCity = (TextView)findViewById(R.id.title_city);
        titleUpdateTime = (TextView)findViewById(R.id.title_time);
        degreeText = (TextView)findViewById(R.id.degree_text);
        weatherInforText = (TextView)findViewById(R.id.weather_info_text);
        windInforText = (TextView)findViewById(R.id.wind_info_text);
        forecastLayout = (LinearLayout)findViewById(R.id.forecast_layout);
        aqiText = (TextView)findViewById(R.id.aqi_text);
        pm25Text = (TextView)findViewById(R.id.pm25_text);
        comfortText = (TextView)findViewById(R.id.comfort_text);
        carWashText = (TextView)findViewById(R.id.car_text);
        sportText = (TextView)findViewById(R.id.sport_text);
        pic_img = (ImageView)findViewById(R.id.pic_img);
        swipeRefresh = (SwipeRefreshLayout)findViewById(R.id.swip_refresh);
        swipeRefresh.setColorSchemeColors(R.color.colorPrimary);
        drawerLayout = (DrawerLayout)findViewById(R.id.drawer);
        home = (Button)findViewById(R.id.home);
        final SharedPreferences pre = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = pre.getString("weather", null);
        final String id;
        if(weatherString!=null){
            Weather weather = Utility.handleWeatherResponse(weatherString);
            id = weather.basic.weatherId;
            showWeather(weather);
        }else{
            id = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(id);
        }
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(Utility.handleWeatherResponse(pre.getString("weather", null)).basic.weatherId);
            }
        });
        String pic_Uri = pre.getString("pic_img",null);
        if(pic_Uri!=null) {
            Glide.with(this).load(pic_Uri).into(pic_img);
        }else{
            loadPic_img();
        }
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }
    public void requestWeather(final String id){
        String Uri = "https://free-api.heweather.com/v5/weather?city="+id+"&key=71eb49fa81c34ae18c67b54189e3d6b8";
        HttpUtil.sendHttpRequest(Uri, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this,"加载失败",Toast.LENGTH_SHORT).show();
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                 final String ResponseText = response.body().string();
                 final Weather weather = Utility.handleWeatherResponse(ResponseText);
                Log.d("gao",weather.toString());
                 runOnUiThread(new Runnable() {
                     @Override
                     public void run() {
                         if (weather != null && "ok".equals(weather.status)) {
                             SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                             editor.putString("weather", ResponseText);
                             editor.apply();
                             showWeather(weather);
                         } else {
                             Toast.makeText(WeatherActivity.this, "111加载失败", Toast.LENGTH_SHORT).show();
                         }
                         swipeRefresh.setRefreshing(false);
                     }
                 });
            }
        });
    }
    private void showWeather(Weather weather){
        String cityName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime.split(" ")[1];
        String degree = weather.now.temperature+"°C";
        String weatherInfo = weather.now.more.info;
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInforText.setText(weatherInfo);
        windInforText.setText(weather.now.wind.dir+weather.now.wind.sc+"级");
        forecastLayout.removeAllViews();
        for(Forecast forecast:weather.forecastList){
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item,forecastLayout,false);
            TextView dataText = (TextView)view.findViewById(R.id.data_text);
            TextView infoText = (TextView)view.findViewById(R.id.info_text);
            TextView maxText = (TextView)view.findViewById(R.id.max_text);
            TextView minText = (TextView)view.findViewById(R.id.min_text);
            Log.d("gao",forecast.date);
            dataText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max+"°C");
            minText.setText(forecast.temperature.min+"°C");
            forecastLayout.addView(view);
        }
        if(weather.aqi !=null){
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);

        }
        comfortText.setText("舒适度："+weather.suggestion.comfort.info);
        carWashText.setText("洗车指数："+weather.suggestion.carWash.info);
        sportText.setText("运动建议：" + weather.suggestion.sport.info);
        weatherLayout.setVisibility(View.VISIBLE);
        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
    }
    private void loadPic_img(){
        String requestUri = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendHttpRequest(requestUri, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String uri = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("pic_img",uri);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pic_img.setAlpha(0.5f);
                        Glide.with(WeatherActivity.this).load(uri).into(pic_img);
                        pic_img.setColorFilter(Color.RED, PorterDuff.Mode.DARKEN);
                    }
                });
            }
        });
    }
}
