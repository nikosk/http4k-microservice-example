package com.getlabeltext.domain

import arrow.core.Either
import com.getlabeltext.domain.user.Email
import com.getlabeltext.domain.user.Password
import com.getlabeltext.domain.user.User
import com.getlabeltext.domain.user.UserApi
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Status.Companion.UNPROCESSABLE_ENTITY
import org.http4k.testing.Approver
import org.http4k.testing.JsonApprovalTest
import org.http4k.testing.assertApproved
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDateTime
import java.util.*

@ExtendWith(JsonApprovalTest::class)
class UserRoutesKtTest {

    private val user = User(
        UUID.fromString("cf19a71d-eb44-4f4b-81e8-e685a0fb6f7c"),
        Email("email"),
        Password("pass"),
        LocalDateTime.of(
            2021, 10, 22, 12, 30, 10
        ),
        null
    )

    @Test
    fun `user is found`(approver: Approver) {
        val app = UserApi.getUserContract { Optional.of(user) }
        approver.assertApproved(app(Request(Method.GET, "/user/${user.id}")), OK)
    }

    @Test
    fun `user is not found`() {
        val app = UserApi.getUserContract { Optional.empty() }
        assert(app(Request(Method.GET, "/user/${user.id}")).status == NOT_FOUND)
    }

    @Test
    fun `user is created`(approver: Approver) {
        val app = UserApi.createUserContract { Either.Right(user) }
        approver.assertApproved(
            app(
                Request(Method.POST, "/user/")
                    .body(
                        """ 
                        {
                          "email": "email@example.com",
                          "password": "password",
                          "passwordConfirm": "password"
                        }
                    """.trimIndent()
                    )
            ), OK
        )
    }

    @Test
    fun `userForm is invalid`(approver: Approver) {
        val app = UserApi.createUserContract { Either.Right(user) }
        approver.assertApproved(
            app(
                Request(Method.POST, "/user/")
                    .body(
                        """ 
                        {
                          "email": "not an email",                          
                          "passwordConfirm": "password"
                        }
                    """.trimIndent()
                    )
            ), UNPROCESSABLE_ENTITY
        )
    }
}
