package com.reactiveprogramming;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;


import java.util.List;

class FluxAndMonoGeneratorServiceTest {

    FluxAndMonoGeneratorService fluxAndMonoGeneratorService = new FluxAndMonoGeneratorService();

    @Test
    void namesFluxMethodTest() {

        //when
        Flux<String> namesFlux = fluxAndMonoGeneratorService.namesFlux();

        //Verify
        StepVerifier.create(namesFlux)
                //.expectNext("Ravi", "Sai", "Kiran")
                //.expectNextCount(3)
                .expectNext("Ravi")
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void namesFluxMapMethodTest() {

        //given
        int stringLength = 3;

        //when
        Flux<String> namesFlux = fluxAndMonoGeneratorService.namesFluxMap(stringLength);

        //then
        StepVerifier.create(namesFlux)
                .expectNext("4-RAVI", "5-KIRAN")
                .verifyComplete();
    }

    @Test
    void namesFluxImmutabilityMethodTest() {

        //when
        Flux<String> namesFlux = fluxAndMonoGeneratorService.namesFluxImmutability();

        //then
        StepVerifier.create(namesFlux)
                .expectNext("Ravi", "Sai", "Kiran")
                .verifyComplete();
    }

    @Test
    void namesFluxFlatMapMethodTest() {

        //given
        int stringLength = 3;

        //when
        Flux<String> namesFlux = fluxAndMonoGeneratorService.namesFluxFlatMap(stringLength);

        //then
        StepVerifier.create(namesFlux)
                .expectNext("R","A","V","I","K","I","R","A","N")
                .verifyComplete();
    }

    @Test
    void namesFluxFlatMapAsync() {

        //given
        int stringLength = 3;

        //when
        Flux<String> namesFlux = fluxAndMonoGeneratorService.namesFluxFlatMapAsync(stringLength);

        //then
        StepVerifier.create(namesFlux)
                //.expectNext("R","A","V","I","K","I","R","A","N")
                .expectNextCount(9)
                .verifyComplete();
    }

    @Test
    void namesFluxConcatMap() {

        //given
        int stringLength = 3;

        //when
        Flux<String> namesFlux = fluxAndMonoGeneratorService.namesFluxConcatMap(stringLength);

        //then
        StepVerifier.create(namesFlux)
                .expectNext("R","A","V","I","K","I","R","A","N")
                //.expectNextCount(9)
                .verifyComplete();
    }

    @Test
    void namesMonoFlatMap() {

        //given
        int stringLength = 3;

        //when
        Mono<List<String>> namesMono = fluxAndMonoGeneratorService.namesMonoFlatMap(stringLength);

        //then
        StepVerifier.create(namesMono)
                .expectNext(List.of("R","A","V","I"))
                .verifyComplete();
    }

    @Test
    void namesMonoFlatMapMany() {

        //given
        int stringLength = 3;

        //when
        Flux<String> namesFlux = fluxAndMonoGeneratorService.namesMonoFlatMapMany(stringLength);

        //then
        StepVerifier.create(namesFlux)
                .expectNext("R","A","V","I")
                .verifyComplete();
    }

    @Test
    void namesFluxSwitchIfEmpty() {

        //given
        int stringLength = 6;

        //when
        Flux<String> namesFlux = fluxAndMonoGeneratorService.namesFluxSwitchIfEmpty(stringLength);

        //then
        StepVerifier.create(namesFlux)
                .expectNext("D","E","F","A","U","L","T")
                .verifyComplete();
    }

    @Test
    void exploreConcat() {

        //given

        //when
        Flux<String> concatFlux = fluxAndMonoGeneratorService.exploreConcat();

        //then
        StepVerifier.create(concatFlux)
                .expectNext("A","B","C","D","E","F")
                .verifyComplete();
    }

    @Test
    void exploreMerge() {

        //given

        //when
        Flux<String> mergeFlux = fluxAndMonoGeneratorService.exploreMerge();

        //then
        StepVerifier.create(mergeFlux)
                .expectNext("A","D","B","E","C","F")
                .verifyComplete();
    }

    @Test
    void exploreMergeSequential() {

        //given

        //when
        Flux<String> mergeFlux = fluxAndMonoGeneratorService.exploreMergeSequential();

        //then
        StepVerifier.create(mergeFlux)
                .expectNext("A","B","C","D","E","F")
                .verifyComplete();
    }

    @Test
    void exploreZip() {

        //given

        //when
        Flux<String> mergeFlux = fluxAndMonoGeneratorService.exploreZip();

        //then
        StepVerifier.create(mergeFlux)
                .expectNext("AD","BE","CF")
                .verifyComplete();
    }

    @Test
    void exploreZipWithMap() {

        //given

        //when
        Flux<String> mergeFlux = fluxAndMonoGeneratorService.exploreZipMap();

        //then
        StepVerifier.create(mergeFlux)
                .expectNext("AD14","BE25","CF36")
                .verifyComplete();
    }

    @Test
    void exploreZipWithMono() {

        //given

        //when
        Mono<String> mergeMono = fluxAndMonoGeneratorService.exploreZipWithMono();

        //then
        StepVerifier.create(mergeMono)
                .expectNext("AB")
                .verifyComplete();
    }
}