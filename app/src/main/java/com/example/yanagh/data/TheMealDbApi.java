package com.example.yanagh.data;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface TheMealDbApi {
    @GET("api/json/v1/1/search.php")
    Call<TheMealDbDto.SearchResponse> searchByName(@Query("s") String query);

    @GET("api/json/v1/1/lookup.php")
    Call<TheMealDbDto.SearchResponse> lookupById(@Query("i") String idMeal);

    /** One random full meal (same shape as search). */
    @GET("api/json/v1/1/random.php")
    Call<TheMealDbDto.SearchResponse> randomMeal();

    /** Category name as on TheMealDB (e.g. Breakfast, Chicken). */
    @GET("api/json/v1/1/filter.php")
    Call<TheMealDbDto.FilterResponse> filterByCategory(@Query("c") String category);
}

