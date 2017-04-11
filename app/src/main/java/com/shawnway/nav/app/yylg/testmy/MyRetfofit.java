package com.shawnway.nav.app.yylg.testmy;

import android.util.Log;

import com.shawnway.nav.app.yylg.net.Api;
import com.shawnway.nav.app.yylg.net.OkHttpClientManager;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import okio.Buffer;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

/**
 * Created by Administrator on 2017-03-10.
 */

public class MyRetfofit {
    private static MyRetfofit retrofitManager;
    private static Retrofit mRetrofit;
    private static final long DEFAULT_TIMEOUT = 60;

    /**
     * 私有化
     * 单例
     */
    private MyRetfofit() {
        mRetrofit = new Retrofit.Builder()
                .baseUrl(Api.BASE_URL)
                .client(OkHttpClientManager.getOkHttpClient())
                .addConverterFactory(SimpleXmlConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        Log.d("MyRetfofit","retfoti");

    }

    /**
     * 获取单例
     *
     * @return
     */
    public static MyRetfofit getInstance() {
        if (retrofitManager == null) {
            synchronized (MyRetfofit.class) {
                if (retrofitManager == null) {
                    retrofitManager = new MyRetfofit();
                }
            }
        }
        return retrofitManager;
    }

    private <T> T create(Class<T> clazz) {
        return mRetrofit.create(clazz);
    }


    /**
     * 获取访问的api接口
     *
     * @return
     */
    public Api getApi() {
        return MyRetfofit.getInstance().create(Api.class);
    }


    /**
     * 添加统一header,超时时间,http日志打印
     * body采用UTF-8编码
     *
     * @return
     */
    public static OkHttpClient genericClient() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public okhttp3.Response intercept(Chain chain) throws IOException {
                        Request request = chain.request();
                        Request.Builder requestBuilder = request.newBuilder();
                        request = requestBuilder
                                .addHeader("Content-Type", "application/json;charset=UTF-8")
                                .post(RequestBody.create(MediaType.parse("application/json;charset=UTF-8"),
                                        bodyToString(request.body())))//关键部分，设置requestBody的编码格式为json
                                .build();
                        return chain.proceed(request);
                    }
                })
                .addInterceptor(logging)
                .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .build();
        return httpClient;
    }



    private static String bodyToString(final RequestBody request) {
        try {
            final RequestBody copy = request;
            final Buffer buffer = new Buffer();
            if (copy != null)
                copy.writeTo(buffer);
            else
                return "";
            return buffer.readUtf8();
        } catch (final IOException e) {
            return "did not work";
        }
    }


}
