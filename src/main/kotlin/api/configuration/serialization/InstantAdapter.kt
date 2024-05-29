package api.configuration.serialization

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.time.Instant

class InstantAdapter {
    @FromJson
    fun fromJson(timestamp: Long): Instant {
        return Instant.ofEpochSecond(timestamp)
    }

    @ToJson
    fun toJson(instant: Instant): Long {
        return instant.epochSecond
    }
}
