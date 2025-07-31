package com.example.config

import com.example.api.StarWarsApi
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import org.koin.core.annotation.Single
import org.koin.dsl.module
import retrofit2.Retrofit

val retrofitModule = module {
    single {
        Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
            isLenient = true
        }
    }

    single {
        val contentType = "application/json".toMediaType()
        get<Json>().asConverterFactory(contentType)
    }

    single {
        // Create a trust manager that does not validate certificate chains
        val trustAllCerts =
            arrayOf<TrustManager>(
                object : X509TrustManager {
                    override fun checkClientTrusted(
                        chain: Array<out X509Certificate>?,
                        authType: String?,
                    ) {}

                    override fun checkServerTrusted(
                        chain: Array<out X509Certificate>?,
                        authType: String?,
                    ) {}

                    override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                }
            )

        // Install the all-trusting trust manager
        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, SecureRandom())

        // Create an OkHttpClient that trusts all certificates
        OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier { _, _ -> true }
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build()
    }

    single {
        Retrofit.Builder()
            .baseUrl("https://swapi.dev/api/")
            .client(get())
            .addConverterFactory(get<retrofit2.Converter.Factory>())
            .build()
    }

    single { get<Retrofit>().create(StarWarsApi::class.java) }
}

@Single
class StarWarsClient(private val api: StarWarsApi) {
    fun getPeople(page: Int? = null) = api.getPeople(page)

    fun getPerson(id: Int) = api.getPerson(id)

    fun getPlanets(page: Int? = null) = api.getPlanets(page)

    fun getPlanet(id: Int) = api.getPlanet(id)
}
