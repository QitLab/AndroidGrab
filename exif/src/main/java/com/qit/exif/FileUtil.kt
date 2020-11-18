package com.qit.exif

import android.content.Context
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * author: Qit .
 * date:   On 2020/11/18
 */
object FileUtil {
    fun getStorageFolder(context: Context, dirName: String): String {
        val path = context.cacheDir.toString() + "/" + dirName + "/"
        val file = File(path)
        file.mkdirs()
        return path
    }

    /**
     * 删除目录下的文件
     *
     * @param folderPath
     */
    fun deleteFiles(folderPath: String?) {
        val dir = File(folderPath)
        if (!dir.exists() || !dir.isDirectory || dir.listFiles() == null) {
            return
        }
        for (file in dir.listFiles()) {
            if (file.isFile) {
                file.delete()
            }
        }
    }

    /**
     * 将文件压缩成zip
     *
     * @param src
     * @param dest
     */
    fun zip(src: String?, dest: String?): Boolean {
        var out: ZipOutputStream? = null
        var inputStream: FileInputStream? = null
        val outFile = File(dest)
        val fileOrDirectory = File(src)
        try {
            out = ZipOutputStream(FileOutputStream(outFile))
            // 压缩文件
            val buffer = ByteArray(4096)
            var bytes_read: Int
            inputStream = FileInputStream(fileOrDirectory)
            val entry = ZipEntry(fileOrDirectory.name)
            out.putNextEntry(entry)
            while (inputStream.read(buffer).also { bytes_read = it } != -1) {
                out.write(buffer, 0, bytes_read)
            }
            out.closeEntry()
        } catch (ex: IOException) {
            ex.printStackTrace()
            return false
        } finally {
            fileOrDirectory.delete()
            if (out != null) {
                try {
                    out.close()
                } catch (ex: IOException) {
                    ex.printStackTrace()
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close()
                } catch (ex: IOException) {
                    ex.printStackTrace()
                }
            }
        }
        return true
    }
}
