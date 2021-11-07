package com.nunchuk.android.core.network.di

import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.nunchuk.android.core.network.ApiConstant.BASE_URL
import com.nunchuk.android.core.network.ApiConstant.BASE_URL_MATRIX
import com.nunchuk.android.core.network.ApiConstant.HTTP_CONNECT_TIMEOUT
import com.nunchuk.android.core.network.ApiConstant.HTTP_READ_TIMEOUT
import com.nunchuk.android.core.network.BuildConfig
import com.nunchuk.android.core.network.HeaderInterceptor
import com.nunchuk.android.core.network.UnauthorizedInterceptor
import com.nunchuk.android.network.util.MATRIX_HTTP_CLIENT
import com.nunchuk.android.network.util.MATRIX_RETROFIT
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Named

@Module
class NetworkModule @Inject constructor() {

    @Provides
    fun provideGson(): Gson = GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .create()

    @Provides
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        headerInterceptor: HeaderInterceptor,
        unauthorizedInterceptor: UnauthorizedInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(HTTP_CONNECT_TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(HTTP_READ_TIMEOUT, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .addInterceptor(headerInterceptor)
        .addInterceptor(unauthorizedInterceptor)
        .build()

    @Provides
    fun provideRetrofit(gson: Gson, client: OkHttpClient): Retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .baseUrl(BASE_URL)
        .client(client)
        .build()

    @Provides
    @Named(MATRIX_RETROFIT)
    fun provideRetrofitMatrix(gson: Gson, @Named(MATRIX_HTTP_CLIENT) client: OkHttpClient): Retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .baseUrl(BASE_URL_MATRIX)
        .client(client)
        .build()

    @Provides
    @Named(MATRIX_HTTP_CLIENT)
    fun provideOkHttpClientMatrix(
        loggingInterceptor: HttpLoggingInterceptor,
    ): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(HTTP_CONNECT_TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(HTTP_READ_TIMEOUT, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .build()

    @Provides
    fun provideLoggingInterceptor() = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) Level.BODY else Level.NONE
    }

}