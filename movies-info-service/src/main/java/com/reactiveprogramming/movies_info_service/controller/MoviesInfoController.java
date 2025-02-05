package com.reactiveprogramming.movies_info_service.controller;

import com.reactiveprogramming.movies_info_service.domain.MoviesInfo;
import com.reactiveprogramming.movies_info_service.service.MoviesInfoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@RestController
@RequestMapping("/api/v1")
public class MoviesInfoController {

    private final MoviesInfoService moviesInfoService;

    public MoviesInfoController(MoviesInfoService moviesInfoService) {
        this.moviesInfoService = moviesInfoService;
    }

    Sinks.Many<MoviesInfo> moviesInfoSink = Sinks.many().replay().all();

    @PostMapping("/addMoviesInfo")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<MoviesInfo> addMoviesInfo(@RequestBody @Valid MoviesInfo moviesInfo) {

        return moviesInfoService.addMoviesInfo(moviesInfo)
                //publish the MoviesInfo event using Sinks
                .doOnNext(saveMoviesInfo -> moviesInfoSink.tryEmitNext(saveMoviesInfo))
                .log();
    }

    //Streaming endpoint that streams MoviesInfo event to client after a new MoviesInfo is created
    //Server Sent Events (SSE)
    @GetMapping(value = "/getMoviesInfo/stream", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<MoviesInfo> getMoviesInfoStream() {

        //Subscribing to the event that is published using Sinks
        return moviesInfoSink.asFlux().log();
    }

    @GetMapping("/getAllMoviesInfo")
    public Flux<MoviesInfo> getAllMoviesInfo(@RequestParam(value = "year", required = false) Integer year) {

        if(year != null) {
            return moviesInfoService.getMoviesInfoByYear(year).log();
        }

        return moviesInfoService.getAllMoviesInfo().log();
    }

    @GetMapping("/getMoviesInfoById/{id}")
    public Mono<ResponseEntity<MoviesInfo>> getMoviesInfoById(@PathVariable("id") String id) {

        return moviesInfoService.getMoviesInfoById(id)
                .map(moviesInfo -> ResponseEntity.status(HttpStatus.OK).body(moviesInfo))
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()))
                .log();
    }

    @PutMapping("/updateMoviesInfo/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<ResponseEntity<MoviesInfo>> updateMoviesInfo(@RequestBody MoviesInfo updatedMoviesInfo, @PathVariable String id) {

        return moviesInfoService.updateMoviesInfo(updatedMoviesInfo, id)
                .map(moviesInfo -> ResponseEntity.status(HttpStatus.OK).body(moviesInfo))
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()))
                .log();
    }

    @DeleteMapping("/deleteMoviesInfo/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteMoviesInfo(@PathVariable String id) {

        return moviesInfoService.deleteMoviesInfo(id).log();
    }
}
