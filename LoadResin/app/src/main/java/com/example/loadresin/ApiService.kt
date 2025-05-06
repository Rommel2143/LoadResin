package com.example.loadresin
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ApiService {
    @FormUrlEncoded
    @POST("resin/save_resin.php")
    fun saveResin(
        @Field("machine") machine: String,
        @Field("resin") resin: String,
        @Field("wt") wt: Float,
        @Field("fname") fname: String
    ): Call<ApiResponse>
}