package com.sloy.sevibus.domain.model

import com.sloy.sevibus.data.api.model.LoggedUserDto

data class LoggedUser(
    val id: String,
    val displayName: String,
    val email: String,
    val photoUrl: String?,
)

fun LoggedUser.toDto() = LoggedUserDto(displayName, email, photoUrl)
