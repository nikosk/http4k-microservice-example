package com.getlabeltext.domain.user

import arrow.core.Either
import arrow.core.Valid
import arrow.core.ValidatedNel
import arrow.core.invalidNel
import com.getlabeltext.utils.Patterns.EMAIL_ADDRESS
import com.getlabeltext.utils.ValidationError
import org.http4k.core.Body
import org.http4k.format.Jackson.auto
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.mapper.reflect.ColumnName
import org.jdbi.v3.core.statement.StatementContext
import org.slf4j.Logger
import java.sql.ResultSet
import java.time.LocalDateTime
import java.util.*

@JvmInline
value class Email(private val email: String) {

    val asString: String
        get() = email

    companion object {
        fun create(value: String?): ValidatedNel<ValidationError, Email> = when {
            value == null -> ValidationError("Email cannot be null").invalidNel()
            value.isEmpty() -> ValidationError("Email cannot be empty").invalidNel()
            value.isBlank() -> ValidationError("Email cannot be blank").invalidNel()
            !value.matches(EMAIL_ADDRESS.toRegex()) -> ValidationError("Email is invalid").invalidNel()
            else -> Valid(Email(value))
        }
    }
}

@JvmInline
value class Password(private val password: String) {

    val asString: String
        get() = password

    companion object {
        fun create(value: String?): ValidatedNel<ValidationError, Password> = when {
            value == null -> ValidationError("Password cannot be null").invalidNel()
            value.length < 6 -> ValidationError("Password is too short").invalidNel()
            else -> Valid(Password(value))
        }
    }
}

data class User(
    val id: UUID,
    val email: Email,
    val password: Password,
    @ColumnName("created_at") val createdAt: LocalDateTime,
    @ColumnName("modified_at") val modifiedAt: LocalDateTime?,
)

val userLens = Body.auto<User>().toLens()

interface UserService {

    val logger: Logger

    val jdbi: Jdbi

    fun getById(): (id: UUID) -> Optional<User> = { id ->
        jdbi.withHandle<Optional<User>, Exception> { handle ->
            handle.createQuery("SELECT * FROM users WHERE id = :id")
                .bind("id", id)
                .map(UserRowMapper)
                .findOne()
        }
    }

    fun notExistsByEmail(): (email: Email) -> Boolean = { email ->
        jdbi.withHandle<Boolean, Exception> { handle ->
            handle.createQuery("SELECT COUNT(*) = 0 FROM users WHERE email = :email")
                .bind("email", email.asString)
                .map { rs, col, _ -> rs.getBoolean(col) }
                .first()
        }
    }

    fun create(): (UserForm) -> Either<Throwable, User> = { userForm ->
        Either.catch {
            jdbi.inTransaction<User, Exception> { handle ->
                if (notExistsByEmail()(userForm.email)) {
                    val id = UUID.randomUUID()
                    handle.createUpdate("INSERT INTO users(id, email, password) VALUES (:id, :email, :password)")
                        .bind("id", id)
                        .bind("email", userForm.email.asString)
                        .bind("password", userForm.password.asString)
                        .execute()
                    getById()(id).map { it }.get()
                } else {
                    throw RuntimeException("Email already exists")
                }
            }
        }.tapLeft { logger.error(it.message, it) }
    }

    object UserRowMapper : RowMapper<User> {
        override fun map(rs: ResultSet, ctx: StatementContext?): User {
            return User(
                UUID.fromString(rs.getString("id")),
                Email(rs.getString("email")),
                Password(rs.getString("password")),
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getTimestamp("modified_at")?.toLocalDateTime()
            )
        }
    }
}
