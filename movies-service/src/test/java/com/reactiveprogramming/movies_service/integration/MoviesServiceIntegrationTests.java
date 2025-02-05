package com.reactiveprogramming.movies_service.integration;

import com.reactiveprogramming.movies_service.domain.Movie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@AutoConfigureWireMock(port = 8085) //Spins up an embedded WireMock server on port 8085
@TestPropertySource(
        properties = {
                "restClient.moviesInfoUrl=http://localhost:8085/api/v1",
                "restClient.reviewsUrl=http://localhost:8085/api/v1/review"
        }
)
public class MoviesServiceIntegrationTests {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void testRetrieveMovieById() {

        String movieId = "1";

        stubFor(get(urlEqualTo("/api/v1/getMoviesInfoById/" + movieId))
                        .willReturn(aResponse()
                                .withHeader("Content-Type", "application/json")
                                .withBody("{\n" +
                                        "    \"moviesInfoId\": \"1\",\n" +
                                        "    \"name\": \"Magadheera\",\n" +
                                        "    \"year\": 2016,\n" +
                                        "    \"cast\": [\n" +
                                        "        \"Ramcharan\",\n" +
                                        "        \"Kajal\"\n" +
                                        "    ],\n" +
                                        "    \"releaseDate\": \"2016-10-14\"\n" +
                                        "}")));

        stubFor(get(urlPathEqualTo("/api/v1/review/getAllReviews"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("[\n" +
                                "    {\n" +
                                "        \"reviewId\": \"R2\",\n" +
                                "        \"movieInfoId\": \"1\",\n" +
                                "        \"comment\": \"Super action movie\",\n" +
                                "        \"rating\": 5.0\n" +
                                "    }\n" +
                                "]")));

        webTestClient.get()
                .uri("/api/v1/movies/getMovie/{id}", movieId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Movie.class)
                .consumeWith(movieEntityExchangeResult -> {
                    Movie movie = movieEntityExchangeResult.getResponseBody();
                    assert movie != null;
                    assertEquals(1, movie.getReviewList().size());
                    assertEquals("Magadheera", movie.getMovieInfo().getName());
                });
    }

    @Test
    void testRetrieveMovieById_when404Error() {

        String movieId = "1";

        stubFor(get(urlEqualTo("/api/v1/getMoviesInfoById/" + movieId))
                .willReturn(aResponse()
                        .withStatus(404)
                )
        );

        //This stub is ignored as error is thrown before this
        stubFor(get(urlPathEqualTo("/api/v1/review/getAllReviews"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("[\n" +
                                "    {\n" +
                                "        \"reviewId\": \"R2\",\n" +
                                "        \"movieInfoId\": \"1\",\n" +
                                "        \"comment\": \"Super action movie\",\n" +
                                "        \"rating\": 5.0\n" +
                                "    }\n" +
                                "]")));

        webTestClient.get()
                .uri("/api/v1/movies/getMovie/{id}", movieId)
                .exchange()
                .expectStatus()
                .is4xxClientError()
                .expectBody(String.class)
                .isEqualTo("There is no MovieInfo available for given movieId " + movieId);


        verify(1, getRequestedFor(urlEqualTo("/api/v1/getMoviesInfoById/" + movieId)));
    }

    @Test
    void testRetrieveMovieById_when404Error_forReviewService() {

        String movieId = "1";

        stubFor(get(urlEqualTo("/api/v1/getMoviesInfoById/" + movieId))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\n" +
                                "    \"moviesInfoId\": \"1\",\n" +
                                "    \"name\": \"Magadheera\",\n" +
                                "    \"year\": 2016,\n" +
                                "    \"cast\": [\n" +
                                "        \"Ramcharan\",\n" +
                                "        \"Kajal\"\n" +
                                "    ],\n" +
                                "    \"releaseDate\": \"2016-10-14\"\n" +
                                "}")));

        stubFor(get(urlPathEqualTo("/api/v1/review/getAllReviews"))
                .willReturn(aResponse()
                        .withStatus(404)));

        webTestClient.get()
                .uri("/api/v1/movies/getMovie/{id}", movieId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Movie.class)
                .consumeWith(movieEntityExchangeResult -> {
                    Movie movie = movieEntityExchangeResult.getResponseBody();
                    assert movie != null;
                    assertEquals(0, movie.getReviewList().size());
                    assertEquals("Magadheera", movie.getMovieInfo().getName());
                });
    }

    @Test
    void testRetrieveMovieById_when5xxError() {

        String movieId = "1";

        stubFor(get(urlEqualTo("/api/v1/getMoviesInfoById/" + movieId))
                .willReturn(aResponse()
                        .withStatus(503)
                        .withBody("MovieInfo service is unavailable")
                )
        );

        //This stub is ignored as error is thrown before this
        stubFor(get(urlPathEqualTo("/api/v1/review/getAllReviews"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("[\n" +
                                "    {\n" +
                                "        \"reviewId\": \"R2\",\n" +
                                "        \"movieInfoId\": \"1\",\n" +
                                "        \"comment\": \"Super action movie\",\n" +
                                "        \"rating\": 5.0\n" +
                                "    }\n" +
                                "]")));

        webTestClient.get()
                .uri("/api/v1/movies/getMovie/{id}", movieId)
                .exchange()
                .expectStatus()
                .is5xxServerError()
                .expectBody(String.class)
                .isEqualTo("Server error in MoviesInfo service, MovieInfo service is unavailable");

        verify(4, getRequestedFor(urlEqualTo("/api/v1/getMoviesInfoById/" + movieId)));

    }

    @Test
    void testRetrieveMovieById_when5xxError_forReviewService() {

        String movieId = "1";

        stubFor(get(urlEqualTo("/api/v1/getMoviesInfoById/" + movieId))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\n" +
                                "    \"moviesInfoId\": \"1\",\n" +
                                "    \"name\": \"Magadheera\",\n" +
                                "    \"year\": 2016,\n" +
                                "    \"cast\": [\n" +
                                "        \"Ramcharan\",\n" +
                                "        \"Kajal\"\n" +
                                "    ],\n" +
                                "    \"releaseDate\": \"2016-10-14\"\n" +
                                "}")));

        stubFor(get(urlPathEqualTo("/api/v1/review/getAllReviews"))
                .willReturn(aResponse()
                        .withStatus(503)
                        .withBody("Reviews service unavailable")));

        webTestClient.get()
                .uri("/api/v1/movies/getMovie/{id}", movieId)
                .exchange()
                .expectStatus()
                .is5xxServerError()
                .expectBody(String.class)
                .isEqualTo("Server error in Reviews service, Reviews service unavailable");

        verify(4, getRequestedFor(urlPathMatching("/api/v1/review/getAllReviews*")));
    }
}
