package com.github.simonpercic.oklog.core

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

const val URL_SHORTNER_URL = "https://firebasedynamiclinks.googleapis.com/v1/shortLinks?key="

object URLShortenUtils {

    val networkClient by lazy { OkHttpClient() }

    fun getShortUrl(longUrl : String?,apiKeyProvider: URLShortenAPIKeyProvider?) : String?{
        var shorturl : String? = null
        val apikey = apiKeyProvider?.getAPIKey()
        apikey?.let { api ->
            longUrl?.let {
                val requestJSON = JSONObject().apply {
                    put("dynamicLinkInfo",JSONObject().apply {
                        put("dynamicLinkDomain",apiKeyProvider.getShortUrlBaseUrl() ?: "nearbuy.page.link")
                        put("link",it)
                    })
                    //put("longDynamicLink","https://nearbuy.page.link/?link=$it")
                }
                val requestBody = requestJSON.toString().toRequestBody()
                val postRequest = Request.Builder().apply {
                    url("$URL_SHORTNER_URL$api")
                    post(requestBody)
                }.build()

                try {
                    val response = networkClient.newCall(postRequest).execute()
                    val responseJSON = JSONObject(StringUtils.getResponseJSON(response))
                    shorturl = responseJSON.getString("shortLink")
                }catch (e : Exception){
                    e.printStackTrace()
                    return shorturl
                }
            }
        }

        return shorturl
    }
}