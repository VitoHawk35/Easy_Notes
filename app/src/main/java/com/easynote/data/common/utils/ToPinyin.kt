package com.easynote.data.common.utils

import android.content.Context
import com.github.promeg.pinyinhelper.Pinyin
import com.github.promeg.tinypinyin.lexicons.android.cncity.CnCityDict

class ToPinyin(appContext: Context) {
    init {
        Pinyin.init(Pinyin.newConfig().with(CnCityDict.getInstance(appContext)))
    }

    fun convertToPinyin(input: String?): String? {
        if (input == null) return null
        return Pinyin.toPinyin(input, " ")
    }
}