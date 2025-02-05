package com.reactiveprogramming.movies_service.service;

import com.reactiveprogramming.movies_service.client.MoviesInfoRestClient;
import com.reactiveprogramming.movies_service.client.ReviewsRestClient;
import com.reactiveprogramming.movies_service.domain.Movie;
import com.reactiveprogramming.movies_service.domain.MovieInfo;
import com.reactiveprogramming.movies_service.domain.Review;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class MoviesService {

    private final MoviesInfoRestClient moviesInfoRestClient;
    private final ReviewsRestClient reviewsRestClient;

    public MoviesService(MoviesInfoRestClient moviesInfoRestClient, ReviewsRestClient reviewsRestClient) {
        this.moviesInfoRestClient = moviesInfoRestClient;
        this.reviewsRestClient = reviewsRestClient;
    }

    public Mono<Movie> getMovieById(String movieId) {

        return moviesInfoRestClient.retrieveMovieInfoById(movieId)
                .flatMap(movieInfo -> {

                    Mono<List<Review>> reviewsListMono = reviewsRestClient.retrieveReviews(movieId).collectList();

                    return reviewsListMono.map(reviews -> new Movie(movieInfo, reviews));
                });
    }

    public Flux<MovieInfo> getMovieInfoStream() {

        return moviesInfoRestClient.retrieveMovieInfoStream();
    }
}
