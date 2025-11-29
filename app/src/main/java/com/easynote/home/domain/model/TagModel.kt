package com.easynote.home.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TagModel(
    val tagId: Long= -1L,
    val tagName: String= "",
    val color: String= "#FFFFFF"
) : Parcelable