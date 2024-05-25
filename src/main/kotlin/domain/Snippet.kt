package domain

import se.ansman.kotshi.JsonSerializable
import java.time.Instant
import java.util.UUID

@JsonSerializable
data class Snippet(
    val id: UUID,
    val title: String = "",
    val contents: String,
    val createdAt: Instant,
    val format: String? = null,
    val expiresAt: Instant? = null,
) {
    companion object {
        const val ID = "id"
    }
}
