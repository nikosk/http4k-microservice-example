package com.getlabeltext

import com.getlabeltext.domain.user.UserApi
import com.getlabeltext.domain.user.UserService
import com.getlabeltext.infrastructure.Datasource
import org.http4k.contract.ContractRoutingHttpHandler
import org.http4k.contract.bind
import org.http4k.contract.contract
import org.http4k.core.HttpHandler
import org.http4k.core.then
import org.http4k.filter.DebuggingFilters.PrintRequest
import org.http4k.routing.routes
import org.http4k.server.Undertow
import org.http4k.server.asServer
import org.jdbi.v3.core.Jdbi
import org.slf4j.Logger
import org.slf4j.LoggerFactory

val settings = Settings()

val userService = object : UserService {

    override val logger: Logger = LoggerFactory.getLogger(UserService::class.java)

    override val jdbi: Jdbi by lazy {
        Datasource(settings).jdbi
    }
}

val contract: ContractRoutingHttpHandler = contract {
    routes += UserApi.getUserContract(userService.getById())
    routes += UserApi.createUserContract(userService.create())
}

val app: HttpHandler = routes("/api/" bind contract)

fun main() {

    val appVersion: HttpHandler = if (settings.debug()) {
        PrintRequest().then(app)
    } else {
        app
    }

    val server = appVersion.asServer(Undertow(settings.port())).start()

    println("Server started on " + server.port())
}
