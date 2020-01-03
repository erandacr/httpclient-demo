package org.demo.http.client.config;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.binder.okhttp3.OkHttpMetricsEventListener;
import io.micrometer.jmx.JmxConfig;
import io.micrometer.jmx.JmxMeterRegistry;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    private static final int HTTP_MAX_IDLE = 50;
    private static final int HTTP_KEEP_ALIVE = 30;
    private static final int HTTP_CONNECTION_TIMEOUT = 20;

    private static final int RETRY_INIT_INTERVAL = 1000;
    private static final int RETRY_MULTIPLIER = 2;
    private static final int RETRY_MAX_ATTEMPTS = 3;

    private static final Map<Class<? extends Throwable>, Boolean> RETRY_EXCEPTIONS = new HashMap<>();

    // include the exceptions that needs to be retried
    static {
        RETRY_EXCEPTIONS.put(HttpClientErrorException.class, true);
        RETRY_EXCEPTIONS.put(HttpServerErrorException.class, true);
        RETRY_EXCEPTIONS.put(SocketTimeoutException.class, true);
    }

    @Bean
    @Qualifier("okhttp3Template")
    public RestTemplate okhttp3Template() {
        RestTemplate restTemplate = new RestTemplate();

        // 1. create the okhttp client builder
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        ConnectionPool okHttpConnectionPool = new ConnectionPool(HTTP_MAX_IDLE, HTTP_KEEP_ALIVE,
            TimeUnit.SECONDS);
        builder.connectionPool(okHttpConnectionPool);
        builder.connectTimeout(HTTP_CONNECTION_TIMEOUT, TimeUnit.SECONDS);
        builder.retryOnConnectionFailure(false);

        // 2. enable metrics in okhttp client
        builder.eventListener(OkHttpMetricsEventListener
            .builder(metricRegistry(), "okhttp3.requests")
            .build());

        // 3. embed the created okhttp client to a spring rest template
        restTemplate.setRequestFactory(new OkHttp3ClientHttpRequestFactory(builder.build()));

        return restTemplate;
    }

    @Bean
    public RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(RETRY_INIT_INTERVAL);
        backOffPolicy.setMultiplier(RETRY_MULTIPLIER);
        retryTemplate.setBackOffPolicy(backOffPolicy);

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(RETRY_MAX_ATTEMPTS,
            RETRY_EXCEPTIONS, true, false);
        retryTemplate.setRetryPolicy(retryPolicy);

        return retryTemplate;
    }

    @Bean
    public JmxMeterRegistry metricRegistry() {
        JmxMeterRegistry jmxMeterRegistry = new JmxMeterRegistry(JmxConfig.DEFAULT, Clock.SYSTEM);
        jmxMeterRegistry.start();
        return jmxMeterRegistry;
    }

}
