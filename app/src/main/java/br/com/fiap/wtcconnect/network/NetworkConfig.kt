package br.com.fiap.wtcconnect.network

import br.com.fiap.wtcconnect.BuildConfig

object NetworkConfig {
    val BASE_URL: String = BuildConfig.API_BASE_URL.ensureTrailingSlash()
    val HUB_URL: String = BuildConfig.SIGNALR_HUB_URL

    private fun String.ensureTrailingSlash(): String {
        return if (endsWith("/")) this else "$this/"
    }
}
