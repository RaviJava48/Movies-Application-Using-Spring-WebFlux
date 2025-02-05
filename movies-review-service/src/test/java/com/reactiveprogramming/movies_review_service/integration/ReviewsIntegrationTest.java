package com.reactiveprogramming.movies_review_service.integration;

import com.reactiveprogramming.movies_review_service.domain.Review;
import com.reactiveprogramming.movies_review_service.repository.ReviewReactiveRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
public class ReviewsIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ReviewReactiveRepository reviewReactiveRepository;

    private static final String REVIEWS_URL = "/api/v1/review";

    @BeforeEach
    void setUp() {

        List<Review> reviewList = List.of(
                new Review(null, 1L, "Superb Movie", 4.2),
                new Review(null, 2L, "Excellent direction", 4.5),
                new Review("R3", 2L, "Awesome songs", 4.0)
        );
        reviewReactiveRepository.saveAll(reviewList).blockLast();
    }

    @AfterEach
    void tearDown() {
        reviewReactiveRepository.deleteAll().block();
    }

    @Test
    void addReviewTest() {

        Review review = new Review(null, 3L, "Awesome story", 4.5);

        webTestClient.post()
                .uri(REVIEWS_URL + "/addReview")
                .bodyValue(review)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Review.class)
                .consumeWith(reviewEntityExchangeResult -> {
                    Review savedReview = reviewEntityExchangeResult.getResponseBody();
                    assert savedReview != null;
                    assert savedReview.getReviewId() != null;
                });
    }

    @Test
    void getReviewsStream() {

        Review review = new Review(null, 3L, "Awesome story", 4.5);

        webTestClient.post()
                .uri(REVIEWS_URL + "/addReview")
                .bodyValue(review)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Review.class)
                .consumeWith(reviewEntityExchangeResult -> {
                    Review savedReview = reviewEntityExchangeResult.getResponseBody();
                    assert savedReview != null;
                    assert savedReview.getReviewId() != null;
                });

        Flux<Review> reviewFlux = webTestClient.get()
                .uri(REVIEWS_URL + "/getReviews/stream")
                .exchange()
                .expectStatus().isOk()
                .returnResult(Review.class)
                .getResponseBody();

        StepVerifier.create(reviewFlux)
                .assertNext(review1 -> {
                    assert review1.getReviewId() != null;
                    assertEquals("Awesome story", review1.getComment());
                })
                .thenCancel()
                .verify();
    }


    @Test
    void getAllReviewsTest() {

        webTestClient.get()
                .uri(REVIEWS_URL + "/getAllReviews")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Review.class)
                .hasSize(3);
    }

    @Test
    void updateReviewTest() {

        String reviewId = "R3";
        Review review = new Review(null, 2L, "Cool movie", 4.0);

        webTestClient.put()
                .uri(REVIEWS_URL + "/updateReview/{id}", reviewId)
                .bodyValue(review)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Review.class)
                .consumeWith(reviewEntityExchangeResult -> {
                    Review updatedReview = reviewEntityExchangeResult.getResponseBody();
                    assert updatedReview != null;
                    assertEquals("Cool movie", updatedReview.getComment());
                    assertEquals(4.0, updatedReview.getRating());
                });
    }

    @Test
    void deleteReviewTest() {

        String reviewId = "R3";

        webTestClient.delete()
                .uri(REVIEWS_URL + "/deleteReview/{id}", reviewId)
                .exchange()
                .expectStatus().isNoContent()
                .expectBody(Void.class);
    }

    @Test
    void getReviewByMoviesInfoIdTest() {

        Long movieInfoId = 2L;

        URI uri = UriComponentsBuilder.fromUriString(REVIEWS_URL + "/getAllReviews")
                .queryParam("movieInfoId", movieInfoId)
                .buildAndExpand().toUri();


        webTestClient.get()
                .uri(uri)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Review.class)
                .hasSize(2);
    }

    @Test
    void updateReviewTest_whenNotFound() {

        String reviewId = "R10";
        Review review = new Review(null, 2L, "Cool movie", 4.0);

        webTestClient.put()
                .uri(REVIEWS_URL + "/updateReview/{id}", reviewId)
                .bodyValue(review)
                .exchange()
                .expectStatus().isNotFound();
    }
}
