package com.reactiveprogramming.movies_service.controller;

import com.reactiveprogramming.movies_service.domain.Movie;
import com.reactiveprogramming.movies_service.domain.MovieInfo;
import com.reactiveprogramming.movies_service.service.MoviesService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/movies")
public class MoviesServiceController {

    private final MoviesService moviesService;

    public MoviesServiceController(MoviesService moviesService) {
        this.moviesService = moviesService;
    }

    @GetMapping("/getMovie/{id}")
    public Mono<Movie> getMovieById(@PathVariable("id") String movieId) {

        return moviesService.getMovieById(movieId);
    }

    @GetMapping(value = "/getMovieInfo/stream", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<MovieInfo> getMovieInfoStream() {

        return moviesService.getMovieInfoStream();
    }
}
