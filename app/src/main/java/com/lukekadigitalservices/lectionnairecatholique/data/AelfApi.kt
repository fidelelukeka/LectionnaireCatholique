package com.lukekadigitalservices.lectionnairecatholique.data

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface AelfApi {
    @GET("v1/informations/{date}/{zone}")
    suspend fun getInformations(@Path("date") date: String, @Path("zone") zone: String): Response<InfoResponse>

    @GET("v1/messes/{date}/{zone}")
    suspend fun getMesses(@Path("date") date: String, @Path("zone") zone: String): Response<MessesResponse>

    @GET("v1/lectures/{date}/{zone}")
    suspend fun getOfficeLectures(@Path("date") date: String, @Path("zone") zone: String): Response<OfficeResponse>
}