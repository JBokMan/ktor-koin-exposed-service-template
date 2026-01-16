package com.example.config

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.defaultheaders.DefaultHeaders

fun Application.configureSecurityHeaders() {
    install(DefaultHeaders) {
        // Prevent clickjacking attacks
        header("X-Frame-Options", "DENY")

        // Prevent MIME type sniffing
        header("X-Content-Type-Options", "nosniff")

        // Enable XSS filter in browsers
        header("X-XSS-Protection", "1; mode=block")

        // Control referrer information
        header("Referrer-Policy", "strict-origin-when-cross-origin")

        // Permissions policy (formerly Feature-Policy)
        header(
            "Permissions-Policy",
            "accelerometer=(), camera=(), geolocation=(), gyroscope=(), magnetometer=(), microphone=(), payment=(), usb=()",
        )

        // Content Security Policy - adjust based on your needs
        // This is a restrictive default; customize for your application
        header(
            "Content-Security-Policy",
            "default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'; img-src 'self' data:; font-src 'self'; frame-ancestors 'none'",
        )

        // HTTP Strict Transport Security (HSTS)
        // Only enable in production with HTTPS
        val enableHsts = System.getenv("ENABLE_HSTS")?.toBoolean() ?: false
        if (enableHsts) {
            // max-age=31536000 (1 year), includeSubDomains, preload
            header("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload")
        }

        // Cache control for API responses
        header("Cache-Control", "no-store, no-cache, must-revalidate, proxy-revalidate")
        header("Pragma", "no-cache")
        header("Expires", "0")
    }
}
