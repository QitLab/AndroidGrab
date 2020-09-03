package com.qit.device

import android.content.Context
import android.provider.MediaStore

object SDCardUtils {

    fun getVideoExternalCount(context: Context): Int {
        var count = 0
        val contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val cursor = context.contentResolver.query(contentUri, null, null, null, null)
        if (cursor != null) {
            count = cursor.count
            cursor.close()
        }
        return count
    }

    fun getVideoInternalCount(context: Context): Int {
        var count = 0
        val contentUri = MediaStore.Video.Media.INTERNAL_CONTENT_URI
        val cursor = context.contentResolver.query(contentUri, null, null, null, null)
        if (cursor != null) {
            count = cursor.count
            cursor.close()
        }
        return count
    }

    fun getAudioExternalCount(context: Context): Int {
        var count = 0
        val contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val cursor = context.contentResolver.query(contentUri, null, null, null, null)
        if (cursor != null) {
            count = cursor.count
            cursor.close()
        }
        return count
    }

    fun getAudioInternalCount(context: Context): Int {
        var count = 0
        val contentUri = MediaStore.Audio.Media.INTERNAL_CONTENT_URI
        val cursor = context.contentResolver.query(contentUri, null, null, null, null)
        if (cursor != null) {
            count = cursor.count
            cursor.close()
        }
        return count
    }

    fun getImagesExternalCount(context: Context): Int {
        var count = 0
        val contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val cursor = context.contentResolver.query(contentUri, null, null, null, null)
        if (cursor != null) {
            count = cursor.count
            cursor.close()
        }
        return count
    }

    fun getImagesInternalCount(context: Context): Int {
        var count = 0
        val contentUri = MediaStore.Images.Media.INTERNAL_CONTENT_URI
        val cursor = context.contentResolver.query(contentUri, null, null, null, null)
        if (cursor != null) {
            count = cursor.count
            cursor.close()
        }
        return count
    }

}