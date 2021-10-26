package com.getlabeltext.domain.user

import arrow.core.*
import arrow.typeclasses.Semigroup
import com.fasterxml.jackson.databind.JsonNode
import com.getlabeltext.utils.*
import org.http4k.contract.ContractRoute
import org.http4k.contract.div
import org.http4k.contract.meta
import org.http4k.core.*
import org.http4k.core.ContentType.Companion.APPLICATION_JSON
import org.http4k.core.Status.Companion.INTERNAL_SERVER_ERROR
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.format.Jackson.json
import org.http4k.lens.BodyLens
import org.http4k.lens.Header.CONTENT_TYPE
import org.http4k.lens.Path
import org.http4k.lens.uuid
import java.util.*

object UserApi {

    fun getUserContract(userFinder: (id: UUID) -> Optional<User>): ContractRoute {

        val spec = "/user/" / Path.uuid().of("id") meta {
            produces += APPLICATION_JSON
        } bindContract Method.GET

        fun findUserById(id: UUID): HttpHandler =
            { _ ->
                userFinder(id).map {
                    Response(OK).with(userLens of it)
                }.orElse(Response(NOT_FOUND).with(CONTENT_TYPE of APPLICATION_JSON))
            }

        return spec to ::findUserById
    }

    fun createUserContract(
        userCreator: (userForm: UserForm) -> Either<Throwable, User>
    ): ContractRoute {
        val spec = "/user/" meta {
            summary = "Creates"
            consumes += APPLICATION_JSON
        } bindContract Method.POST

        fun createUser(): HttpHandler = { req ->
            val validated: ValidatedNel<ValidationError, UserForm> = UserForm.lens(req)
            ValidationOps.validatedResponse(validated) {
                when (val result = userCreator(it)) {
                    is Either.Left -> Response(INTERNAL_SERVER_ERROR).with(unrecoverableErrorLens of  result.value.toUnrecoverableError())
                    is Either.Right -> Response(OK).with(userLens of result.value)
                }
            }
        }
        return spec to ::createUser
    }
}

data class UserForm(val email: Email, val password: Password) {

    companion object {

        val lens: BodyLens<ValidatedNel<ValidationError, UserForm>> = Body.json().map { jsonToUserForm(it) }.toLens()

        private fun jsonToUserForm(node: JsonNode): ValidatedNel<ValidationError, UserForm> {
            val email: String? = if (node.has("email")) node.get("email").asText() else null
            val password: String? = if (node.has("password")) node.get("password").asText() else null
            val passwordConfirm: String? = if (node.has("passwordConfirm")) node.get("passwordConfirm").asText() else null
            return asValidated(email, password, passwordConfirm)
        }

        private fun asValidated(email: String?, password: String?, passwordConfirm: String?): ValidatedNel<ValidationError, UserForm> {
            return Email.create(email).zip(
                Semigroup.nonEmptyList(),
                when {
                    password == null -> ValidationError("Password confirmation cannot be null").invalidNel()
                    password.length < 6 -> ValidationError("Password is too short").invalidNel()
                    password != passwordConfirm -> ValidationError("Passwords dont' match").invalidNel()
                    else -> Valid(Password(password))
                }
            ) { e, p -> UserForm(e, p) }
        }
    }
}



