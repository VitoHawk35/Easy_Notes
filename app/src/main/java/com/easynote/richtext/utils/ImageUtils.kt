package com.easynote.richtext.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import com.easynote.data.repository.Repository
import com.easynote.data.repository.impl.RepositoryImpl
import java.io.File
import kotlin.math.min

object ImageUtils {


    /**
     * 自适应加载图片
     * @param reqWidth 最大允许宽度
     * @param reqHeight 最大允许高度（新增参数）
     */
    fun loadScaledBitmap(context: Context, uri: Uri, reqWidth: Int, reqHeight: Int): Bitmap? {
        try {
            // 🔥 新增：处理 file:// 协议
            if (uri.scheme == "file") {
                val path = uri.path ?: return null
                return loadScaledBitmapFromFile(path, reqWidth, reqHeight)
            }
            // 1. 获取原图尺寸
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            }

            val srcWidth = options.outWidth
            val srcHeight = options.outHeight

            // 2. 计算目标缩放比例
            // 宽度的缩放比：目标宽 / 原宽
            // 高度的缩放比：目标高 / 原高
            // 取两者中较小的那个，以保证图片能完整塞进这个框里
            val widthRatio = reqWidth.toFloat() / srcWidth
            val heightRatio = reqHeight.toFloat() / srcHeight

            // 最终缩放比：不能放大(超过1.0)，且需同时满足宽高限制
            val scale = min(1.0f, min(widthRatio, heightRatio))

            // 3. 计算采样率 (inSampleSize) 用于节省内存
            // 根据最终要显示的宽高来计算
            val destWidth = (srcWidth * scale).toInt()
            val destHeight = (srcHeight * scale).toInt()

            options.inSampleSize = calculateInSampleSize(options, destWidth, destHeight)
            options.inJustDecodeBounds = false

            // 4. 加载图片
            var bitmap = context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            } ?: return null

            // 5. 使用 Matrix 进行精确缩放 (因为 inSampleSize 只能大致缩放)
            if (scale < 1.0f || bitmap.width > reqWidth) {
                // 这里需要根据加载后的实际 bitmap 尺寸重新计算 scale
                // 因为 inSampleSize 可能已经把图片缩小了一倍
                val finalScaleX = destWidth.toFloat() / bitmap.width
                val finalScaleY = destHeight.toFloat() / bitmap.height

                val matrix = Matrix()
                matrix.postScale(finalScaleX, finalScaleY)

                val scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                if (scaledBitmap != bitmap) {
                    bitmap.recycle()
                }
                return scaledBitmap
            }
            return bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun loadScaledBitmapFromFile(path: String, reqWidth: Int, reqHeight: Int): Bitmap? {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeFile(path, options)

        val srcWidth = options.outWidth
        val srcHeight = options.outHeight

        // 同样的缩放算法
        val widthRatio = reqWidth.toFloat() / srcWidth
        val heightRatio = reqHeight.toFloat() / srcHeight
        val scale = min(1.0f, min(widthRatio, heightRatio))

        val destWidth = (srcWidth * scale).toInt()
        val destHeight = (srcHeight * scale).toInt()

        options.inSampleSize = calculateInSampleSize(options, destWidth, destHeight)
        options.inJustDecodeBounds = false

        val bitmap = BitmapFactory.decodeFile(path, options) ?: return null

        if (scale < 1.0f || bitmap.width > reqWidth) {
            val finalScaleX = destWidth.toFloat() / bitmap.width
            val finalScaleY = destHeight.toFloat() / bitmap.height
            val matrix = Matrix()
            matrix.postScale(finalScaleX, finalScaleY)
            val scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            if (scaledBitmap != bitmap) bitmap.recycle()
            return scaledBitmap
        }
        return bitmap
    }
}