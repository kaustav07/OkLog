package com.github.simonpercic.oklog.core;

import java.nio.charset.Charset;

/**
 * Constants.
 *
 * @author Simon Percic <a href="https://github.com/simonpercic">https://github.com/simonpercic</a>
 */
final class Constants {

    static final String LOG_TAG = "OKLOG";
    static final String LOG_URL_BASE_REMOTE = "http://oklog.responseecho.com";
    static final String LOG_URL_BASE_PATH = "/v1/";
    static final String LOG_URL_ECHO_PATH = "re/";
    static final String LOG_URL_INFO_PATH = "r/";

    static final int URL_SHORTEN_THRESHOLD = 3500;
    static final int FIREBASE_URL_THRESHOLD = 7000;

    private static final String UTF8 = "UTF-8";
    static final Charset CHARSET_UTF8 = Charset.forName(UTF8);

    static final String TIMBER_CLASS = "timber.log.Timber";

    private Constants() {
        // no instance
    }
}
