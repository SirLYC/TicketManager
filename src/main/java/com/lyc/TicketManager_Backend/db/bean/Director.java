package com.lyc.TicketManager_Backend.db.bean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"movies"})
@ToString(exclude = {"movies"})
public class Director {
    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.REFRESH}, fetch = FetchType.EAGER)
    @JoinTable(
            name = "director_movie",
            joinColumns = {@JoinColumn(name = "director_id")},
            inverseJoinColumns = {@JoinColumn(name = "movie_id")}
    )
    @JsonIgnore
    private final Set<Movie> movies = new HashSet<>();
    @Id
    @GeneratedValue
    private long id;
    private String name;
}
