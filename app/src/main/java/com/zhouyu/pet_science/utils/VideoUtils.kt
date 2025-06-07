package com.zhouyu.pet_science.utils

import android.content.Context
import com.google.android.exoplayer2.database.StandaloneDatabaseProvider
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import java.io.File

object VideoUtils {
     private var simpleCache: SimpleCache? = null
     fun getVideoCache(context: Context): SimpleCache {
         if(simpleCache == null){
             // 创建视频缓存
             val cacheDir = File(context.cacheDir, "video_cache")
             val cacheSize = 500 * 1024 * 1024L // 500MB 缓存大小
             val databaseProvider = StandaloneDatabaseProvider(context)
             simpleCache =
                 SimpleCache(cacheDir, LeastRecentlyUsedCacheEvictor(cacheSize),databaseProvider)
         }
         return simpleCache!!
    }

}
