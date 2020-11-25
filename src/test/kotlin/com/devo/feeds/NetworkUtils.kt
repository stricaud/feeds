package com.devo.feeds

import java.net.ServerSocket

object NetworkUtils {
    fun findAvailablePort(): Int {
        ServerSocket(0).use {
            it.reuseAddress = true
            return it.localPort
        }
    }
}