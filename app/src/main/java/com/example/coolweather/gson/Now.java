package com.example.coolweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by q on 2017/3/23.
 */
public class Now {
    @SerializedName("tmp")
    public String temperature;
    @SerializedName("cond")
    public More more;
    public class More{
        @SerializedName("txt")
        public String info;
    }
    public Wind wind;
    public class Wind{
        public String dir;
        public String sc;
    }
}
