package com.github.simonpercic.oklog.core

interface GooleAuthTokenProvider {
    fun getAccessToken(): Pair<String?, Long>?
}
interface URLShortenAPIKeyProvider {
    fun getAPIKey(): String?
    fun getShortUrlBaseUrl() : String?
}