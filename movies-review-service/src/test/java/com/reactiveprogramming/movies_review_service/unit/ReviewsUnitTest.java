package com.reactiveprogramming.movies_review_service.unit;

import com.reactiveprogramming.movies_review_service.domain.Review;
import com.reactiveprogramming.movies_review_service.exceptionhandler.GlobalErrorHandler;
import com.reactiveprogramming.movies_review_service.handler.ReviewHandler;
import com.reactiveprogramming.movies_review_service.repository.ReviewReactiveRepository;
import com.reactiveprogramming.movies_review_service.router.ReviewRouter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;


@WebFluxTest
@ContextConfiguration(classes = {ReviewRouter.class, ReviewHandler.class, GlobalErrorHandler.class})
@AutoConfigureWebTestClient
public class ReviewsUnitTest {

    @MockitoBean
    private ReviewReactiveRepository reviewReactiveRepository;

    @Autowired
    private WebTestClient webTestClient;

    private static final String REVIEWS_URL = "/api/v1/review";

    @Test
    void addReviewTest() {

        Review review = new Review(null, 1L, "Awesome story", 4.5);

        Review savedReview = new Review("Review1", 1L, "Awesome story", 4.5);

        when(reviewReactiveRepository.save(isA(Review.class))).thenReturn(Mono.just(savedReview));

        webTestClient.post()
                .uri(REVIEWS_URL + "/addReview")
                .bodyValue(review)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Review.class)
                .consumeWith(reviewEntityExchangeResult -> {
                    Review reviewSaved = reviewEntityExchangeResult.getResponseBody();
                    assert reviewSaved != null;
                    assert reviewSaved.getReviewId() != null;
                });

        verify(reviewReactiveRepository, times(1)).save(review);
    }

    @Test
    void getAllReviewsTest() {

        List<Review> reviewList = List.of(
                new Review(null, 1L, "Superb Movie", 4.2),
                new Review(null, 2L, "Excellent direction", 4.5),
                new Review("R3", 2L, "Awesome songs", 4.0)
        );

        when(reviewReactiveRepository.findAll()).thenReturn(Flux.fromIterable(reviewList));

        webTestClient.get()
                .uri(REVIEWS_URL + "/getAllReviews")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Review.class)
                //.hasSize(3);
                .value(reviews -> {
                    assertEquals(3, reviews.size());
                    assertEquals("R3", reviews.get(2).getReviewId());
                });

        verify(reviewReactiveRepository, times(1)).findAll();
    }

    @Test
    void updateReviewTest() {

        String reviewId = "R1";

        Review requestReview = new Review(null, 2L, "Awesome songs", 4.0);
        Review existingReview = new Review("R1", 2L, "Cool Movie", 4.2);
        Review savedReview = new Review("R1", 2L, "Awesome songs", 4.0);

        when(reviewReactiveRepository.findById(reviewId)).thenReturn(Mono.just(existingReview));
        when(reviewReactiveRepository.save(isA(Review.class))).thenReturn(Mono.just(savedReview));

        webTestClient.put()
                .uri(REVIEWS_URL + "/updateReview/{id}", reviewId)
                .bodyValue(requestReview)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Review.class)
                .consumeWith(reviewEntityExchangeResult -> {
                    Review reviewSaved = reviewEntityExchangeResult.getResponseBody();
                    assert reviewSaved != null;
                    assertEquals("Awesome songs", reviewSaved.getComment());
                    assertEquals(4.0, reviewSaved.getRating());
                });

        verify(reviewReactiveRepository, times(1)).findById(reviewId);
        verify(reviewReactiveRepository, times(1)).save(savedReview);
    }

    @Test
    void deleteReviewTest() {

        String reviewId = "R3";

        when(reviewReactiveRepository.deleteById(reviewId)).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri(REVIEWS_URL + "/deleteReview/{id}", reviewId)
                .exchange()
                .expectStatus().isNoContent()
                .expectBody(Void.class);

        verify(reviewReactiveRepository, times(1)).deleteById(reviewId);
    }

    @Test
    void getReviewsByMovieInfoIdTest() {

        Long movieInfoId = 2L;
        List<Review> reviewList = List.of(
                new Review("R2", 2L, "Excellent direction", 4.5),
                new Review("R3", 2L, "Awesome songs", 4.0)
        );

        when(reviewReactiveRepository.findByMovieInfoId(movieInfoId)).thenReturn(Flux.fromIterable(reviewList));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path(REVIEWS_URL + "/getAllReviews")
                        .queryParam("movieInfoId", movieInfoId)
                        .build()
                )
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Review.class)
                .hasSize(2);

        verify(reviewReactiveRepository, times(1)).findByMovieInfoId(movieInfoId);
    }

    @Test
    void addReviewTest_whenValidation() {

        Review review = new Review(null, null, "Awesome story", -4.5);

        Review savedReview = new Review("R1", 1L, "Awesome story", 4.5);

        when(reviewReactiveRepository.save(isA(Review.class))).thenReturn(Mono.just(savedReview));

        webTestClient.post()
                .uri(REVIEWS_URL + "/addReview")
                .bodyValue(review)
                .exchange()
                .expectStatus().isBadRequest();
    }
}
