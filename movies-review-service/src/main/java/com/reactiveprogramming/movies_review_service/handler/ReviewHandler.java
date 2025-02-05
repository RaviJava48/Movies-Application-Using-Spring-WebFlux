package com.reactiveprogramming.movies_review_service.handler;

import com.reactiveprogramming.movies_review_service.domain.Review;
import com.reactiveprogramming.movies_review_service.exception.ReviewDataException;
import com.reactiveprogramming.movies_review_service.exception.ReviewNotFoundException;
import com.reactiveprogramming.movies_review_service.repository.ReviewReactiveRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ReviewHandler {

    @Autowired
    private Validator validator;

    private final ReviewReactiveRepository reviewReactiveRepository;

    Sinks.Many<Review> reviewsSink = Sinks.many().replay().all();

    public ReviewHandler(ReviewReactiveRepository reviewReactiveRepository) {
        this.reviewReactiveRepository = reviewReactiveRepository;
    }

    public Mono<ServerResponse> addReview(ServerRequest request) {

        return request.bodyToMono(Review.class)
                .doOnNext(this::validate)
                .flatMap(reviewReactiveRepository::save)
                .doOnNext(savedReview -> reviewsSink.tryEmitNext(savedReview))
                .flatMap(savedReview -> ServerResponse.status(HttpStatus.CREATED).bodyValue(savedReview));
    }

    private void validate(Review review) {

        Set<ConstraintViolation<Review>> constraintViolationSet =  validator.validate(review);

        if(!constraintViolationSet.isEmpty()) {
            log.error("Constraint Violations: {}", constraintViolationSet);

            String errorMessage = constraintViolationSet.stream()
                    .map(ConstraintViolation::getMessage)
                    .sorted()
                    .collect(Collectors.joining(","));

            throw new ReviewDataException(errorMessage);
        }
    }

    public Mono<ServerResponse> getAllReviews(ServerRequest request) {

        Optional<String> optionalMovieInfoId = request.queryParam("movieInfoId");

        Flux<Review> reviewFlux;
        if(optionalMovieInfoId.isPresent()) {
            reviewFlux = reviewReactiveRepository.findByMovieInfoId(Long.valueOf(optionalMovieInfoId.get()));
        } else {
            reviewFlux = reviewReactiveRepository.findAll();
        }
        return ServerResponse.ok().body(reviewFlux, Review.class);
    }

    public Mono<ServerResponse> updateReview(ServerRequest request) {

        String reviewId = request.pathVariable("id");

        Mono<Review> existingReview = reviewReactiveRepository.findById(reviewId)
                .switchIfEmpty(Mono.error(new ReviewNotFoundException("Review not found for Review ID " + reviewId)));

        return existingReview
                .flatMap(review -> request.bodyToMono(Review.class)
                        .map(requestReview -> {
                            review.setComment(requestReview.getComment());
                            review.setRating(requestReview.getRating());
                            return review;
                        })
                        .flatMap(reviewReactiveRepository::save)
                        .flatMap(savedReview -> ServerResponse.ok().bodyValue(savedReview))
                );
    }

    public Mono<ServerResponse> deleteReview(ServerRequest serverRequest) {

        String reviewId = serverRequest.pathVariable("id");

        return reviewReactiveRepository.deleteById(reviewId)
                .then(ServerResponse.noContent().build());
    }

    public Mono<ServerResponse> getReviewsStream(ServerRequest serverRequest) {

        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_NDJSON)
                .body(reviewsSink.asFlux(), Review.class)
                .log();
    }
}
