package extensions

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success

/**
 * Converts a [Result] into an [Either].
 */
inline fun <reified T, reified E> Result<T, E>.toEither(): Either<E, T> = when (this) {
    is Success -> value.right()
    is Failure -> reason.left()
}
