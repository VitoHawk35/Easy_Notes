package com.easynote.data.annotation

import androidx.annotation.StringDef

const val ORDER_UPDATE_TIME_DESC = "ORDER_UPDATE_TIME_DESC"
const val ORDER_UPDATE_TIME_ASC = "ORDER_UPDATE_TIME_ASC"
const val TITLE_ASC = "TITLE_ASC"
const val TITLE_DESC = "TITLE_DESC"

@StringDef(
    ORDER_UPDATE_TIME_DESC,
    ORDER_UPDATE_TIME_ASC,
    TITLE_ASC,
    TITLE_DESC
)
@Retention(AnnotationRetention.SOURCE)
annotation class NoteOrderWay