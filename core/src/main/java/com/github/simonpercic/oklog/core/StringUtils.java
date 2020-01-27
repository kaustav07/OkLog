package com.github.simonpercic.oklog.core;


import java.io.IOException;
import java.util.Random;

import okhttp3.Response;

/**
 * String utilities.
 *
 * @author Simon Percic <a href="https://github.com/simonpercic">https://github.com/simonpercic</a>
 */
final class StringUtils {

    private StringUtils() {
        // no instance
    }

    /**
     * Returns <tt>true</tt> if the string is null or of zero length.
     *
     * @param string string
     * @return <tt>true</tt> if string is null or of zero length
     */
    static boolean isEmpty(String string) {
        return string == null || string.length() == 0;
    }

    static String getResponseJSON(Response response) throws IOException,NullPointerException {
        String json = null;
        if(response.body() != null){
            json = response.body().string();
        }
        return json;
    }

}
