package com.example.plantscanner

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface GoogleFormApi {
    @FormUrlEncoded
    @POST("/forms/d/e/1FAIpQLScqZ8Vv6NuWxZeKZWXY7IiANywumvzSmqTIlmci88v7kLJ_Xg/formResponse")
    fun sendFormData(
        @Field("entry.1063136466") email: String,
        @Field("entry.1592679026") feedback: String
    ): Call<Void>
}