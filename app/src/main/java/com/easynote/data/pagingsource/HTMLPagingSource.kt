package com.easynote.data.pagingsource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import java.io.File
import java.io.RandomAccessFile

class HTMLPagingSource(
    private val file: File,
    val pageSizeBytes: Int = 4096
) : PagingSource<Int, String>() {
    private val fileLength by lazy { file.length() }

    override fun getRefreshKey(state: PagingState<Int, String>): Int? {
        return state.anchorPosition?.let { pos ->
            state.closestPageToPosition(pos)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(pos)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, String> {
        return try {
            val pageIndex = params.key ?: 0
            val startOffset = pageIndex * pageSizeBytes
            if (startOffset >= fileLength) {
                return LoadResult.Page(
                    data = emptyList(),
                    prevKey = if (pageIndex == 0) null else pageIndex - 1,
                    nextKey = null
                )
            }
            val bufferSize = minOf(pageSizeBytes.toLong(), fileLength - startOffset)
                .toInt()
            val buffer = ByteArray(bufferSize)
            RandomAccessFile(file, "r").use { raf ->
                raf.seek(startOffset.toLong())
                raf.read(buffer)
            }

            val text = String(buffer, Charsets.UTF_8)

            LoadResult.Page(
                data = listOf(text),
                prevKey = if (pageIndex == 0) null else pageIndex - 1,
                nextKey = if (startOffset + bufferSize >= fileLength) null else pageIndex + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

}