package com.xgw.mybaselib.rxhttp.helper;

import android.text.TextUtils;
import android.util.Log;

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by XieGuangwei on 2017/11/6.
 * 单个请求配置帮助类（区别与全局GlobalConfig，不同于其他请求的单独请求配置）
 */

public class SingleConfig {
    private static SingleConfig instance;
    private static String baseUrl;
    private static boolean isLogShow = true;
    private static long readTimeout;
    private static long writeTimeout;
    private static long connectTimeout;
    private static OkHttpClient okHttpClient;

    public static SingleConfig getInstance() {
        baseUrl = null;
        isLogShow = true;
        readTimeout = 0;
        writeTimeout = 0;
        connectTimeout = 0;
        okHttpClient = null;
        if (instance == null) {
            synchronized (SingleConfig.class) {
                instance = new SingleConfig();
            }
        }
        return instance;
    }

    public SingleConfig setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    public SingleConfig isLogShow(boolean isLogShow) {
        this.isLogShow = isLogShow;
        return this;
    }

    public void setReadTimeout(long readTimeout) {
        this.readTimeout = readTimeout;
    }

    public void setWriteTimeout(long writeTimeout) {
        this.writeTimeout = writeTimeout;
    }

    public void setConnectTimeout(long connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public void setOkHttpClient(OkHttpClient okHttpClient) {
        this.okHttpClient = okHttpClient;
    }

    /**
     * 创建自定义单个请求
     *
     * @param cls
     * @param <K>
     * @return
     */
    public <K> K createApi(Class<K> cls) {
        return getSingleRetrofitBuilder().build().create(cls);
    }

    /**
     * 获取单个请求retrofitbuilder
     *
     * @return
     */
    private Retrofit.Builder getSingleRetrofitBuilder() {
        Retrofit.Builder retrofitBuilder = new Retrofit.Builder();
        retrofitBuilder
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(baseUrl)
                .client(okHttpClient == null ? getSingleOkHttpClientBuilder().build() : okHttpClient);
        return retrofitBuilder;
    }

    /**
     * 获取单个请求okhttpclientbuilder
     *
     * @return
     */
    private OkHttpClient.Builder getSingleOkHttpClientBuilder() {
        OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder();
        if (isLogShow) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
                @Override
                public void log(String message) {
                    Log.e("RxHttpUtils", message);
                }
            });
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            okHttpClientBuilder.addInterceptor(loggingInterceptor);
        }
        okHttpClientBuilder.readTimeout(readTimeout > 0 ? readTimeout : 10, TimeUnit.SECONDS);

        okHttpClientBuilder.writeTimeout(writeTimeout > 0 ? writeTimeout : 10, TimeUnit.SECONDS);

        okHttpClientBuilder.connectTimeout(connectTimeout > 0 ? connectTimeout : 10, TimeUnit.SECONDS);
        return okHttpClientBuilder;
    }
}
