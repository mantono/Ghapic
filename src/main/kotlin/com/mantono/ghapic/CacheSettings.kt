package com.mantono.ghapic

data class CacheSettings(val resourcePolicy: CachePolicy = CachePolicy.THRESHOLD,
                         val searchPolicy: CachePolicy = CachePolicy.THRESHOLD,
                         val resourceThreshold: Int = WorkManager.HOURLY_RATE / 2,
                         val searchThreshold: Int = WorkManager.MINUTE_SEARCH_RATE / 2)