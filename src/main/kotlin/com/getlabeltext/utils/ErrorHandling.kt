package com.getlabeltext.utils

import arrow.core.Nel
import arrow.core.Validated
import arrow.core.valueOr
import org.http4k.core.Body
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import org.http4k.format.Jackson.auto

data class UnrecoverableError(val message: String, val exception: Throwable)

val unrecoverableErrorLens = Body.auto<UnrecoverableError>().toLens()

fun Throwable.toUnrecoverableError() = UnrecoverableError(message.orEmpty(), this)

data class ValidationError(val message: String)

typealias ValidationResult<E> = Validated<Nel<ValidationError>, E>

object ValidationOps {

    fun <E> validatedResponse(validated: ValidationResult<E>, successHandler: (E) -> Response): Response =
        validated.map { successHandler(it) }
            .valueOr { errors -> Response(Status.UNPROCESSABLE_ENTITY).with(Body.auto<List<ValidationError>>().toLens() of errors.all) }

}

