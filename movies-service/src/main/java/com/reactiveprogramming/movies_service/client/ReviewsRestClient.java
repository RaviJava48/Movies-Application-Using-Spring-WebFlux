package com.reactiveprogramming.movies_service.client;

import com.reactiveprogramming.movies_service.domain.Review;
import com.reactiveprogramming.movies_service.exception.MoviesInfoClientException;
import com.reactiveprogramming.movies_service.exception.MoviesInfoServerException;
import com.reactiveprogramming.movies_service.exception.ReviewsClientException;
import com.reactiveprogramming.movies_service.exception.ReviewsServerException;
import com.reactiveprogramming.movies_service.util.RetryUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;

@Component
@Slf4j
public class ReviewsRestClient {

    private final WebClient webClient;

    @Value("${restClient.reviewsUrl}")
    private String reviewsUrl;

    public ReviewsRestClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public Flux<Review> retrieveReviews(String movieId) {

        URI uri = UriComponentsBuilder.fromUriString(reviewsUrl + "/getAllReviews")
                .queryParam("movieInfoId", movieId)
                .buildAndExpand()
                .toUri();

        return webClient.get()
                .uri(uri)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> {
                    log.info("Client error status code: {}", clientResponse.statusCode().value());

                    if(clientResponse.statusCode().equals(HttpStatusCode.valueOf(404))) {
                        return Mono.empty();
                    }

                    return clientResponse.bodyToMono(String.class)
                            .flatMap(errorMessage -> Mono.error(new ReviewsClientException(errorMessage)));
                })
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> {
                    log.info("Server error status code: {}", clientResponse.statusCode().value());

                    return clientResponse.bodyToMono(String.class)
                            .flatMap(errorMessage -> Mono.error(new ReviewsServerException(
                                    "Server error in Reviews service, " + errorMessage
                            )));
                })
                .bodyToFlux(Review.class)
                .retryWhen(RetryUtil.getRetrySpec())
                .log();
    }
}
