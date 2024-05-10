package com.example.florascope.backend

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ModelApi {
    @POST("analyze")
    fun analyzeImage(@Body request: AnalyzeRequest): Call<AnalysisResponse>
}