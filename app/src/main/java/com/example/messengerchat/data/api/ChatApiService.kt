package com.example.messengerchat.data.api

import com.example.messengerchat.data.models.*
import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface ChatApiService {
    @PUT("message")
    suspend fun editMessage(
        @Body request: EditMessageRequest
    ): Response<EditMessageResponse>

    @DELETE("message")
    suspend fun deleteMessage(
        @Query("message_id") messageId: Int
    ): Response<SimpleResponse>

    @GET("file")
    @Streaming
    suspend fun downloadFile(
        @Query("file") fileId: String
    ): Response<ResponseBody>

    @Multipart
    @POST("file")
    suspend fun uploadFile(
        @Query("user_id") userId: String,
        @Query("chat_id") chatId: String,
        @Part file: MultipartBody.Part
    ): Response<FileUploadResponse>
}

data class FileUploadResponse(
    @SerializedName("filePath")
    val filePath: String
)