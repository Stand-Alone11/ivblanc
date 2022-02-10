package com.strait.ivblanc.data.api

import com.strait.ivblanc.data.model.response.StyleAllResponse
import com.strait.ivblanc.data.model.response.StyleResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface StyleApi {
    @Multipart
    @POST("/api/style/add")
    suspend fun addStyle(@Part image: MultipartBody.Part, @Part clothesList: MultipartBody.Part): Response<StyleResponse>

    @GET("/api/style/finduserstyle")
    suspend fun getAllStyles(): Response<StyleAllResponse>

    @GET("/api/style/findfriendstyle")
    suspend fun getAllFriendStyles(@Query("FriendEmail")FriendEmail :String):Response<StyleAllResponse>

}