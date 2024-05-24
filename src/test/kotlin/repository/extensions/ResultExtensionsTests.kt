package repository.extensions

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.failureOrNull
import dev.forkhandles.result4k.valueOrNull
import extensions.toEither
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.arrow.isLeft
import strikt.arrow.isRight

class ResultExtensionTests {
    @Test
    fun `toEither() returns Resul#Success value as Either#Right`() {
        val result: Result<String, Error> = Success("Success")

        expectThat(result.toEither())
            .isRight()
            .get { value == result.valueOrNull() }
    }

    @Test
    fun `toEither() returns Resul#Failure value as Either#Left`() {
        val result: Result<String, Error> = Failure(Error("Failure"))

        expectThat(result.toEither())
            .isLeft()
            .get { value == result.failureOrNull() }
    }
}
