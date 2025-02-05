package com.reactiveprogramming.movies_info_service.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document   //Similar to @Entity in RDBMS
public class MoviesInfo {

    @Id
    private String moviesInfoId;

    @NotNull(message = "MoviesInfo name must not be null")
    @NotBlank(message = "MoviesInfo name must be present")
    private String name;

    @NotNull(message = "MoviesInfo year must not be null")
    @Positive(message = "MoviesInfo year must be positive")
    private Integer year;

    private List<@NotBlank(message = "MoviesInfo cast must be present") String> cast;
    private LocalDate releaseDate;
}
