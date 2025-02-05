package com.reactiveprogramming.movies_service.client;

import com.reactiveprogramming.movies_service.domain.MovieInfo;
import com.reactiveprogramming.movies_service.exception.MoviesInfoClientException;
import com.reactiveprogramming.movies_service.exception.MoviesInfoServerException;
import com.reactiveprogramming.movies_service.util.RetryUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Component
@Slf4j
public class MoviesInfoRestClient {

    private final WebClient webClient;

    @Value("${restClient.moviesInfoUrl}")
    private String moviesInfoUrl;

    public MoviesInfoRestClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<MovieInfo> retrieveMovieInfoById(String movieId) {

        String uri = moviesInfoUrl + "/getMoviesInfoById/{id}";

        return webClient.get()
                .uri(uri, movieId)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> {
                    log.info("Client error status code: {}", clientResponse.statusCode().value());

                    if(clientResponse.statusCode().equals(HttpStatusCode.valueOf(404))) {
                        return Mono.error(new MoviesInfoClientException(
                                "There is no MovieInfo available for given movieId " + movieId,
                                clientResponse.statusCode().value()
                        ));
                    }

                    return clientResponse.bodyToMono(String.class)
                            .flatMap(errorMessage -> Mono.error(new MoviesInfoClientException(
                                    errorMessage,
                                    clientResponse.statusCode().value()
                            )));
                })
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> {
                    log.info("Server error status code: {}", clientResponse.statusCode().value());

                    return clientResponse.bodyToMono(String.class)
                            .flatMap(errorMessage -> Mono.error(new MoviesInfoServerException(
                                    "Server error in MoviesInfo service, " + errorMessage
                            )));
                })
                .bodyToMono(MovieInfo.class)
                //.retry(4)
                .retryWhen(RetryUtil.getRetrySpec())
                .log();
    }

    //Making a call for external streaming endpoint in MoviesInfo service using WebClient
    //No integration test written for this, have a GO if you want :)
    public Flux<MovieInfo> retrieveMovieInfoStream() {

        String uri = moviesInfoUrl + "/getMoviesInfo/stream";

        return webClient.get()
                .uri(uri)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> {
                    log.info("Client error status code in retrieveMovieInfoStream(): {}", clientResponse.statusCode().value());

                    return clientResponse.bodyToMono(String.class)
                            .flatMap(errorMessage -> Mono.error(new MoviesInfoClientException(
                                    errorMessage,
                                    clientResponse.statusCode().value()
                            )));
                })
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> {
                    log.info("Server error status code in retrieveMovieInfoStream(): {}", clientResponse.statusCode().value());

                    return clientResponse.bodyToMono(String.class)
                            .flatMap(errorMessage -> Mono.error(new MoviesInfoServerException(
                                    "Server error in MoviesInfo service, " + errorMessage
                            )));
                })
                .bodyToFlux(MovieInfo.class)
                //.retry(4)
                .retryWhen(RetryUtil.getRetrySpec())
                .log();
    }
}
