package com.example.firstaidfront.models

data class User(
    val id: String,
    var firstName: String,
    var lastName: String,
    var username: String,
    var email: String,
    var phone: String,
    var address: String,
    var profileImage: String? = null,
    var dateJoined: Long = System.currentTimeMillis()
)