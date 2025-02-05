package com.reactiveprogramming.movies_info_service.integration;

import com.reactiveprogramming.movies_info_service.domain.MoviesInfo;
import com.reactiveprogramming.movies_info_service.repository.MoviesInfoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;

@DataMongoTest
@ActiveProfiles("test")
class MoviesInfoRepositoryIntegrationTest {

    @Autowired
    private MoviesInfoRepository moviesInfoRepository;

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
    void testFindAll() {

        //given

        //when
        Flux<MoviesInfo> moviesInfoFlux = moviesInfoRepository.findAll().log();

        //then
        StepVerifier.create(moviesInfoFlux)
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void testFindById() {

        //given
        MoviesInfo moviesInfo = new MoviesInfo("MVE1", "Legend", 2014, List.of("Bala Krishna", "Jagapathi Babu"), LocalDate.parse("2014-06-20"));

        //when
        Mono<MoviesInfo> moviesInfoMono = moviesInfoRepository.findById("MVE1").log();

        //then
        StepVerifier.create(moviesInfoMono)
                .expectNext(moviesInfo)
                .verifyComplete();
    }

    @Test
    void testSave() {

        //given
        MoviesInfo moviesInfo = new MoviesInfo(null, "Manam", 2018, List.of("Nagarjuna", "Naga Chaitanya"), LocalDate.parse("2018-06-20"));

        //when
        Mono<MoviesInfo> moviesInfoMono = moviesInfoRepository.save(moviesInfo).log();

        //then
        StepVerifier.create(moviesInfoMono)
                .assertNext(moviesInfo1 -> {
                    Assertions.assertNotNull(moviesInfo1.getMoviesInfoId());
                    Assertions.assertEquals("Manam", moviesInfo1.getName());
                })
                .verifyComplete();
    }

    @Test
    void testUpdate() {

        //given
        MoviesInfo existingMovieInfo = moviesInfoRepository.findById("MVE2").block();
        assert existingMovieInfo != null;
        existingMovieInfo.setYear(2020);

        //when
        Mono<MoviesInfo> moviesInfoMono = moviesInfoRepository.save(existingMovieInfo).log();

        //then
        StepVerifier.create(moviesInfoMono)
                .assertNext(moviesInfo -> {
                    Assertions.assertEquals(2020, moviesInfo.getYear());
                })
                .verifyComplete();
    }

    @Test
    void testDeleteById() {

        //when
        moviesInfoRepository.deleteById("MVE2").block();
        Flux<MoviesInfo> moviesInfoFlux = moviesInfoRepository.findAll().log();

        //then
        StepVerifier.create(moviesInfoFlux)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void testFindByYear() {

        //given
        Integer year = 2016;
        MoviesInfo moviesInfo = new MoviesInfo("MVE2", "Magadheera", 2016, List.of("Ram Charan", "Kajal"), LocalDate.parse("2016-08-15"));

        //when
        Flux<MoviesInfo> moviesInfoFlux = moviesInfoRepository.findByYear(year).log();

        //then
        StepVerifier.create(moviesInfoFlux)
                .expectNext(moviesInfo)
                .verifyComplete();
    }

    @Test
    void testFindByName() {

        //given
        String name = "Legend";
        MoviesInfo moviesInfo = new MoviesInfo("MVE1", "Legend", 2014, List.of("Bala Krishna", "Jagapathi Babu"), LocalDate.parse("2014-06-20"));

        //when
        Mono<MoviesInfo> moviesInfoMono = moviesInfoRepository.findByName(name);

        //then
        StepVerifier.create(moviesInfoMono)
                .expectNext(moviesInfo)
                .verifyComplete();
    }
}