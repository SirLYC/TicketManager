package com.lyc.TicketManager_Backend.db.bean;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(exclude = "userMovieRatings")
@ToString(exclude = "userMovieRatings")
public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    @OneToMany(targetEntity = UserMovieRating.class, mappedBy = "user")
    private final Set<UserMovieRating> userMovieRatings = new HashSet<>();
    @Id
    @GeneratedValue
    private long id;
    @Column(unique = true, nullable = false)
    private String username;
    @Column(nullable = false)
    private String nickname;
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;
}
