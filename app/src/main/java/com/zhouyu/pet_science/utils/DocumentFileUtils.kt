package com.zhouyu.pet_science.utils

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore

object DocumentFileUtils {
    fun getPath(context: Context, uri: Uri?): String? {
        if (uri == null) {
            return null
        }
        if (DocumentsContract.isDocumentUri(context, uri)) {
            val authority = uri.authority
            when (authority) {
                "com.android.externalstorage.documents" -> {
                    // 外部储存空间
                    val docId = DocumentsContract.getDocumentId(uri)
                    val divide =
                        docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    val type = divide[0]
                    val path: String = if ("primary" == type) {
                        if (divide.size == 1) {
                            Environment.getExternalStorageDirectory().absolutePath + "/"
                        } else {
                            Environment.getExternalStorageDirectory().absolutePath + "/" + divide[1]
                        }
                    } else {
                        "/storage/" + type + "/" + divide[1]
                    }
                    return path
                }
                "com.android.providers.downloads.documents" -> {
                    // 下载目录
                    val docId = DocumentsContract.getDocumentId(uri)
                    if (docId.startsWith("raw:")) {
                        return docId.replaceFirst("raw:".toRegex(), "")
                    }
                    val downloadUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        docId.toLong()
                    )
                    return queryAbsolutePath(context, downloadUri)
                }
                "com.android.providers.media.documents" -> {
                    // 图片影音
                    val docId = DocumentsContract.getDocumentId(uri)
                    val divide =
                        docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    val type = divide[0]
                    var mediaUri: Uri?
                    mediaUri = when (type) {
                        "image" -> {
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        }
                        "video" -> {
                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                        }
                        "audio" -> {
                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                        }
                        else -> {
                            return null
                        }
                    }
                    mediaUri = ContentUris.withAppendedId(mediaUri, divide[1].toLong())
                    return queryAbsolutePath(context, mediaUri)
                }
            }
        } else {
            // 如果是一般的URI
            val scheme = uri.scheme
            var path: String? = null
            if ("content" == scheme) {
                // 內容URI
                path = queryAbsolutePath(context, uri)
            } else if ("file" == scheme) {
                // 档案URI
                path = uri.path
            }
            return path
        }
        return null
    }

    fun queryAbsolutePath(context: Context, uri: Uri?): String? {
        val projection = arrayOf(MediaStore.MediaColumns.DATA)
        var cursor: Cursor? = null
        try {
            cursor = context.contentResolver.query(uri!!, projection, null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
                return cursor.getString(index)
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            cursor?.close()
        }
        return null
    } //    /**
    //     * Get a file path from a Uri. This will get the the path for Storage Access
    //     * Framework Documents, as well as the _data field for the MediaStore and
    //     * other file-based ContentProviders.
    //     *
    //     * @param context The context.
    //     * @param uri The Uri to query.
    //     * @author paulburke
    //     */
    //    public static String getPath(final Context context, final Uri uri) {
    //
    //        // DocumentProvider
    //        if (DocumentsContract.isDocumentUri(context, uri)) {
    //            // ExternalStorageProvider
    //            if (isExternalStorageDocument(uri)) {
    //                final String docId = DocumentsContract.getDocumentId(uri);
    //                final String[] split = docId.split(":");
    //                final String type = split[0];
    //
    //                if ("primary".equalsIgnoreCase(type)) {
    //                    return Environment.getExternalStorageDirectory() + "/" + split[1];
    //                }
    //
    //                // TODO handle non-primary volumes
    //            }
    //            // DownloadsProvider
    //            else if (isDownloadsDocument(uri)) {
    //
    //                final String id = DocumentsContract.getDocumentId(uri);
    //                final Uri contentUri = ContentUris.withAppendedId(
    //                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
    //
    //                return getDataColumn(context, contentUri, null, null);
    //            }
    //            // MediaProvider
    //            else if (isMediaDocument(uri)) {
    //                final String docId = DocumentsContract.getDocumentId(uri);
    //                final String[] split = docId.split(":");
    //                final String type = split[0];
    //
    //                Uri contentUri = null;
    //                if ("image".equals(type)) {
    //                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    //                } else if ("video".equals(type)) {
    //                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
    //                } else if ("audio".equals(type)) {
    //                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    //                }
    //
    //                final String selection = "_id=?";
    //                final String[] selectionArgs = new String[] {
    //                        split[1]
    //                };
    //
    //                return getDataColumn(context, contentUri, selection, selectionArgs);
    //            }
    //        }
    //        // MediaStore (and general)
    //        else if ("content".equalsIgnoreCase(uri.getScheme())) {
    //            return getDataColumn(context, uri, null, null);
    //        }
    //        // File
    //        else if ("file".equalsIgnoreCase(uri.getScheme())) {
    //            return uri.getPath();
    //        }
    //
    //        return null;
    //    }
    //    /**
    //     * Get the value of the data column for this Uri. This is useful for
    //     * MediaStore Uris, and other file-based ContentProviders.
    //     *
    //     * @param context The context.
    //     * @param uri The Uri to query.
    //     * @param selection (Optional) Filter used in the query.
    //     * @param selectionArgs (Optional) Selection arguments used in the query.
    //     * @return The value of the _data column, which is typically a file path.
    //     */
    //    public static String getDataColumn(Context context, Uri uri, String selection,
    //                                       String[] selectionArgs) {
    //
    //        Cursor cursor = null;
    //        final String column = "_data";
    //        final String[] projection = {
    //                column
    //        };
    //
    //        try {
    //            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
    //                    null);
    //            if (cursor != null && cursor.moveToFirst()) {
    //                final int column_index = cursor.getColumnIndexOrThrow(column);
    //                return cursor.getString(column_index);
    //            }
    //        } finally {
    //            if (cursor != null)
    //                cursor.close();
    //        }
    //        return null;
    //    }
    //
    //
    //    /**
    //     * @param uri The Uri to check.
    //     * @return Whether the Uri authority is ExternalStorageProvider.
    //     */
    //    public static boolean isExternalStorageDocument(Uri uri) {
    //        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    //    }
    //
    //    /**
    //     * @param uri The Uri to check.
    //     * @return Whether the Uri authority is DownloadsProvider.
    //     */
    //    public static boolean isDownloadsDocument(Uri uri) {
    //        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    //    }
    //
    //    /**
    //     * @param uri The Uri to check.
    //     * @return Whether the Uri authority is MediaProvider.
    //     */
    //    public static boolean isMediaDocument(Uri uri) {
    //        return "com.android.providers.media.documents".equals(uri.getAuthority());
    //    }
}
