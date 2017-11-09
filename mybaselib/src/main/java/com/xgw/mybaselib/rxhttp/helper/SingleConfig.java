package com.xgw.mybaselib.rxhttp.helper;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import com.xgw.mybaselib.rxhttp.cache.MyCacheInterceptor;
import com.xgw.mybaselib.utils.AppUtils;
import com.xgw.mybaselib.utils.Utils;

import java.io.File;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by XieGuangwei on 2017/11/6.
 * 单个请求配置帮助类（区别与全局GlobalConfig，不同于其他请求的单独请求配置）
 * 所有变量全部新建
 */

public class SingleConfig {
    private String baseUrl;
    private boolean isLogShow = true;
    private long readTimeout;
    private long writeTimeout;
    private long connectTimeout;
    private OkHttpClient okHttpClient;
    private boolean isNeedCache;
    private String cachePath;
    private long maxCacheSize;

    public SingleConfig setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    public SingleConfig isLogShow(boolean isLogShow) {
        this.isLogShow = isLogShow;
        return this;
    }

    public SingleConfig setReadTimeout(long readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    public SingleConfig setWriteTimeout(long writeTimeout) {
        this.writeTimeout = writeTimeout;
        return this;
    }

    public SingleConfig setConnectTimeout(long connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    public SingleConfig setOkHttpClient(OkHttpClient okHttpClient) {
        this.okHttpClient = okHttpClient;
        return this;
    }

    public SingleConfig setNeedCache(boolean needCache) {
        isNeedCache = needCache;
        return this;
    }

    public SingleConfig setCachePath(String cachePath) {
        this.cachePath = cachePath;
        return this;
    }

    public SingleConfig setMaxCacheSize(long maxCacheSize) {
        this.maxCacheSize = maxCacheSize;
        return this;
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
        if (isNeedCache) {
            MyCacheInterceptor cacheInterceptor = new MyCacheInterceptor();
            Cache cache;
            //设置缓存路径不为空，切设置的缓存大小大于0，则新建缓存
            if (!TextUtils.isEmpty(cachePath) && maxCacheSize > 0) {
                cache = new Cache(new File(cachePath), maxCacheSize);
            } else {
                //否则默认缓存路径和大小
                cache = new Cache(new File(Utils.getApp().getCacheDir() + "/cache")
                        , 1024 * 1024 * 10);
            }
            okHttpClientBuilder.addInterceptor(cacheInterceptor)
                    .addNetworkInterceptor(cacheInterceptor)
                    .cache(cache);
        }
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
