package com.example.storypop
import com.google.firebase.Timestamp

data class Story(
    var id: String = "",
    val caption: String = "",
    val imageUrl: String = "",
    var likeCount: Int = 0,
    var isLiked: Boolean = false,
    var pinStatus: Boolean = true,
    val timestamp: Timestamp = Timestamp.now()
)