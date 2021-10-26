package com.getlabeltext

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory


class Settings {

    private var config: Config

    init {
        val profile = System.getenv("profile")
        config = if (profile != null) {
            ConfigFactory.load("application-${profile}")
                .withFallback(
                    ConfigFactory.load()
                )
        } else {
            ConfigFactory.load()
        }
    }

    fun port(): Int = config.getInt("app.port")
    fun debug(): Boolean = config.getBoolean("app.debug")
    fun dbUrl(): String = config.getString("app.db.url")
    fun dbUser(): String = config.getString("app.db.user")
    fun dbPass(): String = config.getString("app.db.pass")


}
