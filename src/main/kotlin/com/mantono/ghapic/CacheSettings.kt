package com.mantono.ghapic

data class CacheSettings(val resourcePolicy: CachePolicy = CachePolicy.THRESHOLD,
                         val searchPolicy: CachePolicy = CachePolicy.THRESHOLD,
                         val resourceThreshold: Int = HOURLY_RATE / 2,
                         val searchThreshold: Int = MINUTE_SEARCH_RATE / 2)