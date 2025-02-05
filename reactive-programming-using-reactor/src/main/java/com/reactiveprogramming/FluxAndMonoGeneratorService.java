package com.reactiveprogramming;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.function.Function;

public class FluxAndMonoGeneratorService {

    public Flux<String> namesFlux() {

        return Flux.fromIterable(List.of("Ravi", "Sai", "Kiran")).log();
    }

    public Mono<String> nameMono() {

        return Mono.just("Ganesh").log();
    }

    public Flux<String> namesFluxMap(int stringLength) {

        return Flux.fromIterable(List.of("Ravi", "Sai", "Kiran"))
                .map(String::toUpperCase)
                .filter(s -> s.length() > stringLength)
                .map(s -> s.length() + "-" + s)   //4-RAVI, 5-KIRAN
                .log();
    }

    public Mono<String> namesMonoMap() {

        return Mono.just("Ravi")
                .map(String::toUpperCase)
                .log();
    }

    public Flux<String> namesFluxFlatMap(int stringLength) {

        return Flux.fromIterable(List.of("Ravi", "Sai", "Kiran"))
                .map(String::toUpperCase)
                .filter(s -> s.length() > stringLength)  //Flux("RAVI","KIRAN")
                .flatMap(this::splitString)  //Flux("R","A","V","I","K","I","R","A","N")
                .log();
    }

    public Mono<List<String>> namesMonoFlatMap(int stringLength) {

        return Mono.just("Ravi")
                .map(String::toUpperCase)
                .filter(s -> s.length() > stringLength)
                .flatMap(this::splitStringMono)
                .log();
    }

    public Flux<String> namesMonoFlatMapMany(int stringLength) {

        return Mono.just("Ravi")
                .map(String::toUpperCase)
                .filter(s -> s.length() > stringLength)
                .flatMapMany(this::splitString)
                .log();
    }

    private Mono<List<String>> splitStringMono(String s) {

        String[] stringArray = s.split("");
        return Mono.just(List.of(stringArray));
    }

    public Flux<String> namesFluxFlatMapAsync(int stringLength) {

        return Flux.fromIterable(List.of("Ravi", "Sai", "Kiran"))
                .map(String::toUpperCase)
                .filter(s -> s.length() > stringLength)  //Flux("RAVI","KIRAN")
                .flatMap(this::splitStringAsync)  //Flux("R","A","V","I","K","I","R","A","N")
                .log();
    }

    public Flux<String> namesFluxConcatMap(int stringLength) {

        return Flux.fromIterable(List.of("Ravi", "Sai", "Kiran"))
                .map(String::toUpperCase)
                .filter(s -> s.length() > stringLength)  //Flux("RAVI","KIRAN")
                .concatMap(this::splitStringAsync)  //Flux("R","A","V","I","K","I","R","A","N")
                .log();
    }

    public Flux<String> namesFluxDefaultIfEmpty(int stringLength) {

        Function<Flux<String>, Flux<String>> fluxFunction = name -> name.map(String::toUpperCase)
                .filter(s -> s.length() > stringLength)
                .flatMap(this::splitString);

        return Flux.fromIterable(List.of("Ravi", "Sai", "Kiran"))
                .transform(fluxFunction)  //Empty Flux Here
                .defaultIfEmpty("Default")
                .log();
    }

    public Flux<String> namesFluxSwitchIfEmpty(int stringLength) {

        Function<Flux<String>, Flux<String>> fluxFunction = name -> name.map(String::toUpperCase)
                .filter(s -> s.length() > stringLength)
                .flatMap(this::splitString);

        Flux<String> defaultFlux = Flux.just("Default").transform(fluxFunction);

        return Flux.fromIterable(List.of("Ravi", "Sai", "Kiran"))
                .transform(fluxFunction)  //Empty Flux Here
                .switchIfEmpty(defaultFlux)
                .log();
    }

    public Flux<String> exploreConcat() {

        Flux<String> abcFlux = Flux.just("A","B","C");
        Flux<String> defFlux = Flux.just("D","E","F");

        return Flux.concat(abcFlux, defFlux).log();  //Flux("A","B","C","D","E","F")
    }

    public Flux<String> exploreConcatWith() {

        Flux<String> abcFlux = Flux.just("A","B","C");
        Flux<String> defFlux = Flux.just("D","E","F");

        return abcFlux.concatWith(defFlux);  //Flux("A","B","C","D","E","F")
    }

    public Flux<String> exploreConcatWithMono() {

        Mono<String> aMono = Mono.just("A");
        Mono<String> bMono = Mono.just("B");

        return aMono.concatWith(bMono);  //Flux("A","B")
    }

    public Flux<String> exploreMerge() {

        Flux<String> abcFlux = Flux.just("A","B","C")
                .delayElements(Duration.ofMillis(100));  //"A","B","C"
        Flux<String> defFlux = Flux.just("D","E","F")
                .delayElements(Duration.ofMillis(125));  //"D","E","F"

        return Flux.merge(abcFlux, defFlux).log();  //Flux("A","D","B","E","C","F")
    }

    public Flux<String> exploreMergeSequential() {

        Flux<String> abcFlux = Flux.just("A","B","C")
                .delayElements(Duration.ofMillis(100));  //"A","B","C"
        Flux<String> defFlux = Flux.just("D","E","F")
                .delayElements(Duration.ofMillis(125));  //"D","E","F"

        return Flux.mergeSequential(abcFlux, defFlux).log();  //Flux("A","B","C","D","E","F")
    }

    public Flux<String> exploreZip() {

        Flux<String> abcFlux = Flux.just("A","B","C");  //"A","B","C"
        Flux<String> defFlux = Flux.just("D","E","F");  //"D","E","F"

        //This way of merging is suitable for only 2 publishers
        return Flux.zip(abcFlux, defFlux, (f1, f2) -> f1 + f2).log();  //Flux("AD","BE","CF")
    }

    public Flux<String> exploreZipMap() {

        Flux<String> abcFlux = Flux.just("A","B","C");  //"A","B","C"
        Flux<String> defFlux = Flux.just("D","E","F");  //"D","E","F"
        Flux<String> flux123 = Flux.just("1","2","3");
        Flux<String> flux456 = Flux.just("4","5","6");

        //This way of merging with map suitable for more than 2 publishers(up-to 8)
        return Flux.zip(abcFlux, defFlux, flux123, flux456)
                .map(t4 -> t4.getT1() + t4.getT2() + t4.getT3() + t4.getT4())
                .log();   //Flux("AD14","BE25","CF36")
    }

    public Flux<String> exploreZipWith() {

        Flux<String> abcFlux = Flux.just("A","B","C");  //"A","B","C"
        Flux<String> defFlux = Flux.just("D","E","F");  //"D","E","F"

        return abcFlux.zipWith(defFlux, (f1, f2) -> f1 + f2).log();  //Flux("AD","BE","CF")
    }

    public Mono<String> exploreZipWithMono() {

        Mono<String> aMono = Mono.just("A");
        Mono<String> bMono = Mono.just("B");

        return aMono.zipWith(bMono)
                .map(t2 -> t2.getT1() + t2.getT2())
                .log();  //Mono("AB")
    }

    //"RAVI" -> Flux("R","A","V","I")
    private Flux<String> splitString(String s) {

        String[] stringArray = s.split("");
        return Flux.fromArray(stringArray);
    }

    private Flux<String> splitStringAsync(String s) {

        String[] stringArray = s.split("");
        return Flux.fromArray(stringArray).delayElements(Duration.ofMillis(1000));
    }

    public Flux<String> namesFluxImmutability() {

        Flux<String> namesFlux = Flux.fromIterable(List.of("Ravi", "Sai", "Kiran"));
        namesFlux.map(String::toUpperCase);
        return namesFlux;
    }

    public static void main(String[] args) {

        FluxAndMonoGeneratorService fluxAndMonoGeneratorService = new FluxAndMonoGeneratorService();
        fluxAndMonoGeneratorService.namesFlux()
                .subscribe(name -> System.out.println("Flux name is : " + name));

        fluxAndMonoGeneratorService.nameMono()
                .subscribe(name -> System.out.println("Mono name is : " + name));

        System.out.println("Transformed Data using map() for Flux:");
        fluxAndMonoGeneratorService.namesFluxMap(3)
                .subscribe(System.out::println);

        System.out.println("Transformed Data using map() for Mono:");
        fluxAndMonoGeneratorService.namesMonoMap()
                .subscribe(System.out::println);

        fluxAndMonoGeneratorService.namesFluxFlatMap(3)
                .subscribe(System.out::println);

        fluxAndMonoGeneratorService.namesMonoFlatMap(3)
                .subscribe(System.out::println);

        fluxAndMonoGeneratorService.exploreMerge()
                .subscribe(System.out::println);
    }
}
