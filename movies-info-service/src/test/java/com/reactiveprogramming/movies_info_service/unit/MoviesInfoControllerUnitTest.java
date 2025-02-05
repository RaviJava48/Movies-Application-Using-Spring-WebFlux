package com.reactiveprogramming.movies_info_service.unit;

import com.reactiveprogramming.movies_info_service.controller.MoviesInfoController;
import com.reactiveprogramming.movies_info_service.domain.MoviesInfo;
import com.reactiveprogramming.movies_info_service.service.MoviesInfoService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@WebFluxTest(controllers = MoviesInfoController.class)
public class MoviesInfoControllerUnitTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private MoviesInfoService moviesInfoServiceMock;

    @Test
    void getAllMoviesInfo() {

        List<MoviesInfo> moviesInfoList = List.of(new MoviesInfo("MVE1", "Legend", 2014, List.of("Bala Krishna", "Jagapathi Babu"), LocalDate.parse("2014-06-20")),
                new MoviesInfo("MVE2", "Magadheera", 2016, List.of("Ram Charan", "Kajal"), LocalDate.parse("2016-08-15")));

        when(moviesInfoServiceMock.getAllMoviesInfo()).thenReturn(Flux.fromIterable(moviesInfoList));

        webTestClient.get()
                .uri("/api/v1/getAllMoviesInfo")
                .exchange()
                .expectBodyList(MoviesInfo.class)
                .hasSize(2);
    }

    @Test
    void getMoviesInfoById() {

        String id = "MVE1";

        MoviesInfo moviesInfo = new MoviesInfo("MVE1", "Legend", 2014, List.of("Bala Krishna", "Jagapathi Babu"), LocalDate.parse("2014-06-20"));

        when(moviesInfoServiceMock.getMoviesInfoById(id)).thenReturn(Mono.just(moviesInfo));

        webTestClient.get()
                .uri("/api/v1/getMoviesInfoById/" + id)
                .exchange()
                .expectStatus().isOk()
                .expectBody(MoviesInfo.class)
                .consumeWith(moviesInfoEntityExchangeResult -> {
                    MoviesInfo moviesInfo1 = moviesInfoEntityExchangeResult.getResponseBody();
                    assert moviesInfo1 != null;
                    assertEquals("Legend", moviesInfo1.getName());
                });
    }

    @Test
    void addMoviesInfo() {

        //given
        MoviesInfo moviesInfo = new MoviesInfo(null, "Manam", 2018, List.of("Nagarjuna", "Naga Chaitanya"), LocalDate.parse("2018-06-20"));

        when(moviesInfoServiceMock.addMoviesInfo(moviesInfo)).thenReturn(
                Mono.just(new MoviesInfo("mockId", "Manam", 2018, List.of("Nagarjuna", "Naga Chaitanya"), LocalDate.parse("2018-06-20")))
        );

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
                    assertEquals("mockId", moviesInfo1.getMoviesInfoId());
                });
    }

    @Test
    void updateMoviesInfo() {

        //given
        String id = "MVE2";
        MoviesInfo moviesInfo = new MoviesInfo(null, "Manam", 2018, List.of("Nagarjuna", "Naga Chaitanya"), LocalDate.parse("2018-06-20"));

        when(moviesInfoServiceMock.updateMoviesInfo(moviesInfo, id)).thenReturn(
                Mono.just(new MoviesInfo("MVE2", "Manam", 2018, List.of("Nagarjuna", "Naga Chaitanya"), LocalDate.parse("2018-06-20")))
        );

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

        when(moviesInfoServiceMock.deleteMoviesInfo(id)).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/api/v1/deleteMoviesInfo/" + id)
                .exchange()
                .expectStatus().isNoContent()
                .expectBody(Void.class);

        verify(moviesInfoServiceMock, times(1)).deleteMoviesInfo(id);
    }

    @Test
    void addMoviesInfoWithValidation() {

        //given
        MoviesInfo moviesInfo = new MoviesInfo(null, "", -2018, List.of(""), LocalDate.parse("2018-06-20"));

        //when
        webTestClient.post()
                .uri("/api/v1/addMoviesInfo")
                .bodyValue(moviesInfo)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class)
                .consumeWith(stringEntityExchangeResult -> {
                    String responseBody = stringEntityExchangeResult.getResponseBody();
                    System.out.println("Response body: " + responseBody);
                    assert responseBody != null;
                    assertEquals("MoviesInfo cast must be present, MoviesInfo name must be present, MoviesInfo year must be positive", responseBody);
                });
    }
}
