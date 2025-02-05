package com.reactiveprogramming.movies_info_service.integration;

import com.reactiveprogramming.movies_info_service.domain.MoviesInfo;
import com.reactiveprogramming.movies_info_service.repository.MoviesInfoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
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
import java.time.LocalDate;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class MoviesInfoControllerIntegrationTest {

    @Autowired
    private MoviesInfoRepository moviesInfoRepository;

    @Autowired
    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {

        List<MoviesInfo> moviesInfoList = List.of(new MoviesInfo("MVE1", "Legend", 2014, List.of("Bala Krishna", "Jagapathi Babu"), LocalDate.parse("2014-06-20")),
                new MoviesInfo("MVE2", "Magadheera", 2016, List.of("Ram Charan", "Kajal"), LocalDate.parse("2016-08-15")));

        moviesInfoRepository.saveAll(moviesInfoList).blockLast();
        //blockLast() - General definition - Blocks the flux indefinitely until all values are emitted(including last one)
    }

    @AfterEach
    void tearDown() {
        moviesInfoRepository.deleteAll().block();
    }

    @Test
    void addMoviesInfo() {

        //given
        MoviesInfo moviesInfo = new MoviesInfo(null, "Manam", 2018, List.of("Nagarjuna", "Naga Chaitanya"), LocalDate.parse("2018-06-20"));

        //when
        webTestClient.post()
                .uri("/api/v1/addMoviesInfo")
                .bodyValue(moviesInfo)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(MoviesInfo.class)
                .consumeWith(moviesInfoEntityExchangeResult -> {
                    MoviesInfo moviesInfo1 = moviesInfoEntityExchangeResult.getResponseBody();
                    assert moviesInfo1 != null;
                    assert moviesInfo1.getMoviesInfoId() != null;
                });
    }

    @Test
    void getMoviesInfoStream() {

        MoviesInfo moviesInfo = new MoviesInfo(null, "Manam", 2018, List.of("Nagarjuna", "Naga Chaitanya"), LocalDate.parse("2018-06-20"));

        webTestClient.post()
                .uri("/api/v1/addMoviesInfo")
                .bodyValue(moviesInfo)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(MoviesInfo.class)
                .consumeWith(moviesInfoEntityExchangeResult -> {
                    MoviesInfo moviesInfo1 = moviesInfoEntityExchangeResult.getResponseBody();
                    assert moviesInfo1 != null;
                    assert moviesInfo1.getMoviesInfoId() != null;
                });

        //Created MoviesInfo using above code before calling the streaming endpoint
        Flux<MoviesInfo> moviesInfoFlux = webTestClient.get()
                .uri("/api/v1/getMoviesInfo/stream")
                .exchange()
                .expectStatus().isOk()
                .returnResult(MoviesInfo.class)
                .getResponseBody();

        StepVerifier.create(moviesInfoFlux)
                .assertNext(moviesInfo1 -> {
                    assert moviesInfo1.getMoviesInfoId() != null;
                    Assertions.assertEquals("Manam", moviesInfo1.getName());
                })
                .thenCancel()
                .verify();
    }

    @Test
    void getAllMoviesInfo() {

        webTestClient.get()
                .uri("/api/v1/getAllMoviesInfo")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(MoviesInfo.class)
                .hasSize(2);
    }

    @Test
    void getMoviesInfoById() {

        String id = "MVE1";

        webTestClient.get()
                .uri("/api/v1/getMoviesInfoById/" + id)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Legend");
    }

    @Test
    void getMoviesInfoByYear() {

        URI uri = UriComponentsBuilder.fromUriString("/api/v1/getAllMoviesInfo")
                        .queryParam("year", 2016)
                        .buildAndExpand().toUri();

        webTestClient.get()
                .uri(uri)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(MoviesInfo.class)
                .hasSize(1);
    }

    @Test
    void updateMoviesInfo() {

        //given
        String id = "MVE2";
        MoviesInfo moviesInfo = new MoviesInfo(null, "Manam", 2018, List.of("Nagarjuna", "Naga Chaitanya"), LocalDate.parse("2018-06-20"));

        //when
        webTestClient.put()
                .uri("/api/v1/updateMoviesInfo/" + id)
                .bodyValue(moviesInfo)
                .exchange()
                .expectStatus().isOk()
                .expectBody(MoviesInfo.class)
                .consumeWith(moviesInfoEntityExchangeResult -> {
                    MoviesInfo updatedMoviesInfo = moviesInfoEntityExchangeResult.getResponseBody();
                    assert updatedMoviesInfo != null;
                    assert updatedMoviesInfo.getMoviesInfoId() != null;
                    Assertions.assertEquals("Manam", updatedMoviesInfo.getName());
                });
    }

    @Test
    void deleteMoviesInfoById() {

        String id = "MVE2";

        webTestClient.delete()
                .uri("/api/v1/deleteMoviesInfo/" + id)
                .exchange()
                .expectStatus().isNoContent()
        //        .expectBody(Void.class)
                .expectBody()
                .isEmpty();
    }

    @Test
    void updateMoviesInfo_whenNotFound() {

        //given
        String id = "MVE4";
        MoviesInfo moviesInfo = new MoviesInfo(null, "Manam", 2018, List.of("Nagarjuna", "Naga Chaitanya"), LocalDate.parse("2018-06-20"));

        //when
        webTestClient.put()
                .uri("/api/v1/updateMoviesInfo/" + id)
                .bodyValue(moviesInfo)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getMoviesInfoById_whenNotFound() {

        String id = "MVE4";

        webTestClient.get()
                .uri("/api/v1/getMoviesInfoById/" + id)
                .exchange()
                .expectStatus().isNotFound();
    }
}