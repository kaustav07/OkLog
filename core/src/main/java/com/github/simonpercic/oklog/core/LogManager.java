package com.github.simonpercic.oklog.core;

import com.github.simonpercic.oklog.shared.LogDataSerializer;
import com.github.simonpercic.oklog.shared.SharedConstants;
import com.github.simonpercic.oklog.shared.data.LogData;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Log manager.
 * Logs the received response body.
 *
 * @author Simon Percic <a href="https://github.com/simonpercic">https://github.com/simonpercic</a>
 */
public class LogManager {

    private static final String LOG_FORMAT = "LogManager: %s";

    private final String logUrlBase;
    private final LogInterceptor logInterceptor;
    private final Logger logger;
    private final boolean withRequestBody;
    private final boolean shortenInfoUrl;
    private GooleAuthTokenProvider authprovider = null;
    private URLShortenAPIKeyProvider apiprovider = null;
    @NotNull
    private final LogDataConfig logDataConfig;
    @NotNull
    private final CompressionUtil compressionUtil;

    /**
     * Constructor.
     *
     * @param urlBase         url base to use
     * @param logInterceptor  optional log interceptor
     * @param logger          optional logger to use
     * @param ignoreTimber    true to ignore Timber for logging, even if it is present
     * @param withRequestBody true to include request body
     * @param shortenInfoUrl  true to shorten info url on the server-side
     * @param logDataConfig   log data config
     * @param compressionUtil compression util
     */
    public LogManager(String urlBase, LogInterceptor logInterceptor, Logger logger, boolean ignoreTimber,
                      boolean withRequestBody, boolean shortenInfoUrl, @NotNull LogDataConfig logDataConfig,
                      @NotNull CompressionUtil compressionUtil,GooleAuthTokenProvider authprovider,URLShortenAPIKeyProvider apiprovider) {
        this.logUrlBase = urlBase;
        this.logInterceptor = logInterceptor;
        this.logger = resolveLogger(logger, ignoreTimber);
        this.withRequestBody = withRequestBody;
        this.shortenInfoUrl = shortenInfoUrl;
        this.logDataConfig = logDataConfig;
        this.compressionUtil = compressionUtil;
        this.authprovider = authprovider;
        this.apiprovider = apiprovider;
    }

    public LogManager(String urlBase, LogInterceptor logInterceptor, Logger logger, boolean ignoreTimber,
                      boolean withRequestBody, boolean shortenInfoUrl, @NotNull LogDataConfig logDataConfig,
                      @NotNull CompressionUtil compressionUtil) {
        this.logUrlBase = urlBase;
        this.logInterceptor = logInterceptor;
        this.logger = resolveLogger(logger, ignoreTimber);
        this.withRequestBody = withRequestBody;
        this.shortenInfoUrl = shortenInfoUrl;
        this.logDataConfig = logDataConfig;
        this.compressionUtil = compressionUtil;
    }


    /**
     * Logs response data.
     *
     * @param data response data
     */
    public void log(LogDataBuilder data) {
        LogData logData = LogDataConverter.convert(data, logDataConfig);
        String logUrl = getLogUrl(data.getResponseBody(), data.getRequestBody(), logData);

        if (logInterceptor == null || !logInterceptor.onLog(logUrl)) {
            logDebug(logUrl, data.getRequestMethod(), data.getRequestUrlPath());
        }
    }

    String getLogUrl(@Nullable String responseBody, @Nullable String requestBody, @Nullable LogData logData) {
        String responseBodyString = compressBody(responseBody);

        if (StringUtils.isEmpty(responseBodyString)) {
            String message = "LogManager: responseBodyString string is empty";
            logger.w(Constants.LOG_TAG, message);
            responseBodyString = SharedConstants.EMPTY_RESPONSE_BODY;
        }

        StringBuilder queryParams = new StringBuilder();

        if (withRequestBody) {
            queryParams = getRequestBodyQuery(queryParams, requestBody);
        }

        queryParams = getLogDataQuery(queryParams, logData);

        boolean infoUrl = withRequestBody || shortenInfoUrl || logDataConfig.any();

        if (shortenInfoUrl) {
            queryParams = appendQuerySymbol(queryParams, SharedConstants.QUERY_SHORTEN_URL, "1");
        }

        String urlPath = infoUrl ? Constants.LOG_URL_INFO_PATH : Constants.LOG_URL_ECHO_PATH;

        String dataPartString = responseBodyString.concat(queryParams.toString());

        String url = String.format("%s%s%s%s", logUrlBase, Constants.LOG_URL_BASE_PATH, urlPath, dataPartString);

        if (dataPartString.length() > Constants.URL_SHORTEN_THRESHOLD) {
            if (dataPartString.length() > Constants.FIREBASE_URL_THRESHOLD) {
                FirebaseUtils.INSTANCE.setTokenProvider(authprovider);
                String uniquePostKey = FirebaseUtils.INSTANCE.postData(dataPartString);
                if (uniquePostKey != null) {
                    url = String.format("%s%s%s%s", logUrlBase, Constants.LOG_URL_BASE_PATH, urlPath, uniquePostKey + "?" + SharedConstants.QUERY_FIREBASE_URL + "=1");
                }
            } else {
                if (shortenInfoUrl) {
                    String shortUrl = URLShortenUtils.INSTANCE.getShortUrl(url,apiprovider);
                    if (shortUrl != null)
                        url = shortUrl;
                }
            }
        }

        return url;
    }

    @Nullable
    private String compressBody(@Nullable String body) {
        String bodyString;

        try {
            bodyString = compressionUtil.gzipBase64UrlSafe(body);
        } catch (IOException e) {
            logger.e(Constants.LOG_TAG, String.format(LOG_FORMAT, e.getMessage()), e);
            return null;
        }

        return bodyString;
    }

    @NotNull
    private StringBuilder getRequestBodyQuery(@NotNull StringBuilder queryParams, @Nullable String requestBody) {
        String requestBodyString = compressBody(requestBody);

        return appendQuerySymbol(queryParams, SharedConstants.QUERY_PARAM_REQUEST_BODY, requestBodyString);
    }

    @NotNull
    private StringBuilder getLogDataQuery(@NotNull StringBuilder queryParams, @Nullable LogData logData) {
        byte[] logDataBytes = LogDataSerializer.serialize(logData);

        String logDataString = null;
        try {
            logDataString = compressionUtil.gzipBase64UrlSafe(logDataBytes);
        } catch (IOException e) {
            logger.e(Constants.LOG_TAG, String.format(LOG_FORMAT, e.getMessage()), e);
        }

        return appendQuerySymbol(queryParams, SharedConstants.QUERY_PARAM_DATA, logDataString);
    }

    void logDebug(String logUrl, String requestMethod, String requestUrlPath) {
        String format = "%s - %s %s - %s";

        logger.d(Constants.LOG_TAG, String.format(format, Constants.LOG_TAG, requestMethod, requestUrlPath, logUrl));
    }

    @NotNull
    private static StringBuilder appendQuerySymbol(@NotNull StringBuilder queryParams, String querySymbol,
                                                   String string) {

        if (!StringUtils.isEmpty(string)) {
            boolean first = queryParams.length() == 0;
            queryParams.append(first ? "?" : "&");
            queryParams.append(querySymbol);
            queryParams.append('=');
            queryParams.append(string);
        }

        return queryParams;
    }

    @NotNull
    private static Logger resolveLogger(@Nullable Logger logger, boolean ignoreTimber) {
        if (logger != null) {
            return logger;
        }

        if (!ignoreTimber) {
            Logger timberLogger = resolveTimberLogger();

            if (timberLogger != null) {
                return timberLogger;
            } else if (ReflectionUtils.hasClass(Constants.TIMBER_CLASS)) {
                ReflectionTimberLogger reflectionTimberLogger = new ReflectionTimberLogger();

                if (reflectionTimberLogger.isValid()) {
                    return reflectionTimberLogger;
                }
            }
        }

        if (ReflectionUtils.hasClass("android.util.Log")) {
            return new AndroidLogger();
        } else {
            return new JavaLogger();
        }
    }

    @Nullable
    @SuppressWarnings("TryWithIdenticalCatches")
    private static Logger resolveTimberLogger() {

        Method provideLoggerMethod = ReflectionUtils.getMethod(
                "com.github.simonpercic.oklog.core.android.TimberLoggerProvider",
                "provideLogger");

        if (provideLoggerMethod != null) {
            try {
                return (Logger) provideLoggerMethod.invoke(null);
            } catch (IllegalAccessException e) {
                // ignore
            } catch (IllegalArgumentException e) {
                // ignore
            } catch (InvocationTargetException e) {
                // ignore
            }
        }

        return null;
    }
}
