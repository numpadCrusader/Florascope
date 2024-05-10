package com.example.florascope.backend

data class AnalyzeRequest(
    val image_id: String,
    val model_name: String
)
