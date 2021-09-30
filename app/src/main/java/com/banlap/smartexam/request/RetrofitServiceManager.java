package com.banlap.smartexam.request;

import android.util.Base64;

import com.banlap.smartexam.VcomData;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.sql.Time;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

/**
 * @author Banlap on 2021/8/31
 */
public class RetrofitServiceManager {

    private static final int DEFAULT_CONNECT_TIME = 10;  //默认连接时间
    private static final int DEFAULT_WRITE_TIME = 10;  //默认写操作时间
    private static final int DEFAULT_READ_TIME = 10;  //默认读操作时间

    private OkHttpClient okHttpClient;
    private Retrofit retrofit;

    private RetrofitServiceManager() {
        //身份验证
        String credentials = VcomData.USERNAME + ":" + VcomData.PASSWORD;
        String auth_string = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);

        okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    //header中添加身份验证
                    Request.Builder requestBuilder = original.newBuilder()
                            .header("Authorization", auth_string)
                            .header("Accept", "application/json")
                            .method(original.method(), original.body());

                    Request request = requestBuilder.build();
                    return chain.proceed(request);
                })
                .connectTimeout(DEFAULT_CONNECT_TIME, TimeUnit.SECONDS)
                .writeTimeout(DEFAULT_WRITE_TIME, TimeUnit.SECONDS)
                .readTimeout(DEFAULT_READ_TIME, TimeUnit.SECONDS)
                .build();

        retrofit = new Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl(VcomData.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())//添加转化库，默认是Gson
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())//添加回调库，采用RxJava
                .build();
    }

    private static class SingletonHolder {
        private static final RetrofitServiceManager INSTANCE = new RetrofitServiceManager();
    }

    /*
     * 获取RetrofitServiceManager
     **/
    public static RetrofitServiceManager getInstance() {
        return  SingletonHolder.INSTANCE;
    }

    public <T> T create(Class<T> service){
        return retrofit.create(service);
    }
}
