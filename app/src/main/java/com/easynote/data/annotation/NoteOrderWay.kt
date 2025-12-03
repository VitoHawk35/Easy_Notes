package com.easynote.data.annotation

import androidx.annotation.StringDef

const val UPDATE_TIME_DESC = "UPDATE_TIME_DESC"
const val UPDATE_TIME_ASC = "UPDATE_TIME_ASC"
const val TITLE_ASC = "TITLE_ASC"
const val TITLE_DESC = "TITLE_DESC"
const val CREATE_TIME_DESC = "CREATE_TIME_DESC"
const val CREATE_TIME_ASC = "CREATE_TIME_ASC"

@StringDef(
    UPDATE_TIME_DESC,
    UPDATE_TIME_ASC,
    TITLE_ASC,
    TITLE_DESC,
    CREATE_TIME_ASC,
    CREATE_TIME_DESC
)
@Retention(AnnotationRetention.SOURCE)
annotation class NoteOrderWay