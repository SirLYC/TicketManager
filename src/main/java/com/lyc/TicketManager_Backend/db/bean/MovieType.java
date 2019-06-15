package com.lyc.TicketManager_Backend.db.bean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "movie_type")
@Data
@EqualsAndHashCode(exclude = "movies")
@ToString(exclude = "movies")
@NoArgsConstructor
@AllArgsConstructor
public class MovieType {
    @JsonIgnore
    @ManyToMany(mappedBy = "types")
    private final Set<Movie> movies = new HashSet<>();
    @Id
    @GeneratedValue
    private long id;
    @Column(nullable = false, unique = true)
    private String type;
}
