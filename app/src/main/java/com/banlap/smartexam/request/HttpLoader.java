package com.banlap.smartexam.request;

import android.util.Base64;

import com.banlap.smartexam.VcomData;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Credentials;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.PATCH;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * @author Banlap on 2021/8/31
 */
public class HttpLoader {

    private static ApiService apiService = RetrofitServiceManager.getInstance().create(ApiService.class);
    public static String mClientId="";

    private static <T> void setSubscribe(Observable<T> observable, Observer<T> observer) {
        observable.subscribeOn(Schedulers.io())
                .subscribeOn(Schedulers.newThread())//子线程访问网络
                .observeOn(AndroidSchedulers.mainThread())//回调到主线程
                .subscribe(observer);
    }

    public static void getClientId(String clientId, Observer<ResponseBody> observer){
        mClientId = clientId;
        setSubscribe(apiService.getClientId(clientId), observer);
    }

    private interface ApiService{
        @GET("subscriptions/{clientId}")
        Observable<ResponseBody> getClientId(@Path("clientId") String clientId);
    }
}
