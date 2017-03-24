package com.example.coolweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.coolweather.WeatherActivity;
import com.example.coolweather.gson.Weather;
import com.example.coolweather.util.HttpUtil;
import com.example.coolweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AutoUpdateService extends Service {
    public AutoUpdateService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
       return null;
    }
    private void updateWeather(){
        SharedPreferences pre = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = pre.getString("weather",null);
        if(weatherString!=null) {
            Weather weather = Utility.handleWeatherResponse(weatherString);
            String weatherId = weather.basic.weatherId;
            String Uri = "https://free-api.heweather.com/v5/weather?city=" + weatherId + "&key=71eb49fa81c34ae18c67b54189e3d6b8";
            HttpUtil.sendHttpRequest(Uri, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Toast.makeText(AutoUpdateService.this, "加载失败", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    final String ResponseText = response.body().string();
                    final Weather weather = Utility.handleWeatherResponse(ResponseText);
                    if (weather != null && "ok".equals(weather.status)) {
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                        editor.putString("weather", ResponseText);
                        editor.apply();
                    }

                }
            });
        }
    }
    private void updateBingPic(){
        String requestUri = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendHttpRequest(requestUri, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String uri = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                editor.putString("pic_img", uri);
                editor.apply();
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateBingPic();
        updateWeather();
        AlarmManager manager = (AlarmManager)getSystemService(ALARM_SERVICE);
        int hour = 8*60*60*1000;
        long triggerAtTime = SystemClock.elapsedRealtime() + hour;
        Intent i = new Intent(this,AutoUpdateService.class);
        PendingIntent pi = PendingIntent.getService(this,0,i,0);
        manager.cancel(pi);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pi);
        return super.onStartCommand(intent, flags, startId);
    }
}
