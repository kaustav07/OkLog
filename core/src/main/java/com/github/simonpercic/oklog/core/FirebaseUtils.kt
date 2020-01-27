package com.github.simonpercic.oklog.core

import android.net.Uri
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.lang.Exception
import java.net.URLDecoder

const val LOG_URL = "prettylog-546b4.firebaseio.com"
const val TOKEN_EXPIRY_MINUTES = 55
const val TOKEN_EXPIRY_MILL_DIFF = (TOKEN_EXPIRY_MINUTES * 60 * 1000).toLong()
const val LOG_KEY = "RESPONSE_LOG"
const val LOG_TIMESTAMP_KEY = "RESPONSE_LOG_TIME"

object FirebaseUtils {

    val MEDIA_TYPE by lazy { "application/json; charset=utf-8".toMediaType() }

    var token: String? = null
    var authTokenProvider : GooleAuthTokenProvider? = null
    var tokenPair: Pair<String?, Long> = Pair(null,System.currentTimeMillis())
        set(value) {
            field = value
            token = value.first
        }
    val networkClient by lazy { OkHttpClient() }

    fun setTokenProvider(tokenProvider: GooleAuthTokenProvider?){
        authTokenProvider = tokenProvider
        retriveToken()
    }

    fun postData(logData: String?): String? {
        var postUniqueKey: String? = null
        logData?.let {
            validateAccessToke()
            token?.let {token ->
                val jsonObject = JSONObject().apply {
                    put(LOG_KEY, it)
                    put(LOG_TIMESTAMP_KEY, System.currentTimeMillis())
                }

                val uri = Uri.Builder().apply {
                    scheme("https")
                    authority(LOG_URL)
                    appendPath("logData")
                    appendPath("logs.json")
                    appendQueryParameter("access_token",token)
                }.build()

                val requestBody = jsonObject.toString().toRequestBody(MEDIA_TYPE)

                val request = Request.Builder().apply {
                    url(URLDecoder.decode(uri.toString(),"UTF-8"))
                    post(requestBody)
                }.build()

                try {
                    val response = networkClient.newCall(request).execute()
                    if(response.isSuccessful){
                        val returnedJson = JSONObject(StringUtils.getResponseJSON(response))
                        postUniqueKey = returnedJson.getString("name")
                    }
                }catch (e : Exception){
                    e.printStackTrace()
                    return postUniqueKey
                }
            }
        }

        return postUniqueKey
    }

    fun validateAccessToke() {
        if (((System.currentTimeMillis() - tokenPair.second) < TOKEN_EXPIRY_MILL_DIFF) && token != null)
            return
        else {
            retriveToken()
            return
        }
    }

    private val retriveToken = {
        tokenPair = authTokenProvider?.getAccessToken() ?: Pair(null,System.currentTimeMillis())
    }
}