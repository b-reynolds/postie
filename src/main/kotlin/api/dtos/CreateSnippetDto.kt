package api.dtos

import se.ansman.kotshi.JsonSerializable
import java.time.Instant

@JsonSerializable
data class CreateSnippetDto(
    val title: String,
    val contents: String,
    val createdAt: Instant,
    val format: String? = null,
    val expiresAt: Instant? = null,
)
