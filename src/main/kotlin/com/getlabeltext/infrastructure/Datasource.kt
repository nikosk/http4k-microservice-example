package com.getlabeltext.infrastructure

import com.getlabeltext.Settings
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.h2.H2DatabasePlugin
import org.jdbi.v3.core.kotlin.KotlinPlugin
import org.jdbi.v3.core.statement.Slf4JSqlLogger


class Datasource(settings: Settings) {

    private val dataSource: HikariDataSource by lazy {
        val config = HikariConfig()
        config.jdbcUrl = settings.dbUrl()
        config.username = settings.dbUser()
        config.password = settings.dbPass()
        HikariDataSource(config)
    }

    val jdbi: Jdbi by lazy {
        val result = Jdbi.create(dataSource)
        result.installPlugin(H2DatabasePlugin())
        result.installPlugin(KotlinPlugin())
        if (settings.debug()) {
            result.setSqlLogger(Slf4JSqlLogger())
        }
        result;
    }
}
