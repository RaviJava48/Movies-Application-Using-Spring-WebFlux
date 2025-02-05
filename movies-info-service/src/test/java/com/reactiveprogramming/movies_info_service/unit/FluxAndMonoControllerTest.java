package com.reactiveprogramming.movies_info_service.unit;

import com.reactiveprogramming.movies_info_service.controller.FluxAndMonoController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.Objects;

@WebFluxTest(controllers = FluxAndMonoController.class)
class FluxAndMonoControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void integerFlux() {

        webTestClient.get()
                .uri("/api/v1/flux")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(Integer.class)
                .hasSize(4);
    }

    @Test
    void integerFlux_approach_2() {

        Flux<Integer> integerFlux = webTestClient.get()
                .uri("/api/v1/flux")
                .exchange()
                .expectStatus()
                .isOk()
                .returnResult(Integer.class)
                .getResponseBody();

        StepVerifier.create(integerFlux)
                .expectNext(1,2,3,4)
                .verifyComplete();
    }

    @Test
    void integerFlux_approach_3() {

        webTestClient.get()
                .uri("/api/v1/flux")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(Integer.class)
                .consumeWith(listEntityExchangeResult -> {
                    var responseBody = listEntityExchangeResult.getResponseBody();
                    assert (Objects.requireNonNull(responseBody).size() == 4 && responseBody.get(0) == 1);
                });
    }

    @Test
    void helloWorldMono() {

        webTestClient.get()
                .uri("/api/v1/mono")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .consumeWith(stringEntityExchangeResult -> {
                    var responseBody = stringEntityExchangeResult.getResponseBody();
                    assert Objects.equals(responseBody, "Hello World");
                });
    }

    @Test
    void streamFlux() {

        Flux<Long> streamFlux = webTestClient.get()
                .uri("/api/v1/stream")
                .exchange()
                .expectStatus()
                .isOk()
                .returnResult(Long.class)
                .getResponseBody();

        StepVerifier.create(streamFlux)
                .expectNext(0L,1L,2L,3L,4L)
                .thenCancel()
                .verify();
    }
}