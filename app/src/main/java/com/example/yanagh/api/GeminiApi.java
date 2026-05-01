package com.example.yanagh.api;

import com.example.yanagh.BuildConfig;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import java.util.concurrent.TimeUnit;

/**
 * Google Gemini AI API interface.
 */
public interface GeminiApi {
    
    // Using a dynamic path to ensure the model name is correctly passed
    @POST("v1beta/models/{model}:generateContent")
    Call<GeminiDto.GeminiResponse> generateContent(
        @Path("model") String model,
        @Query("key") String apiKey,
        @Body GeminiDto.GeminiRequest request
    );

    class Factory {
        private static final String BASE_URL = "https://generativelanguage.googleapis.com/";
        private static final String DEFAULT_API_KEY = BuildConfig.GEMINI_API_KEY;

        public static GeminiApi create() {
            HttpLoggingInterceptor logger = new HttpLoggingInterceptor();
            logger.setLevel(HttpLoggingInterceptor.Level.BODY);
            
            OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logger)
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
            
            return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(GeminiApi.class);
        }
        
        public static String getApiKey() {
            return DEFAULT_API_KEY;
        }
    }
}
