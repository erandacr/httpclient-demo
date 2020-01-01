package org.demo.http.client.http;

import java.net.URI;
import java.net.URISyntaxException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class SampleHttpGateway {

    @Autowired
    @Qualifier("okhttp3Template")
    private RestTemplate okHttpRestTemplate;

    @Autowired
    private RetryTemplate retryTemplate;

    public String getContent() throws URISyntaxException {

        // example endpoint
        String endpoint = "http://www.googleeeeexxxddd.com";

        HttpEntity<?> entity = new HttpEntity<>(new HttpHeaders());
        return execute(new URI(endpoint), HttpMethod.GET, entity, String.class).getBody();
    }

    private  <T> ResponseEntity<T> execute(URI uri, HttpMethod httpMethod, HttpEntity httpEntity,
        Class<T> typeClass) {

        ResponseEntity<T> response = retryTemplate.execute(arg -> okHttpRestTemplate.exchange(
            uri,
            httpMethod,
            httpEntity,
            typeClass));

        return response;
    }

}
