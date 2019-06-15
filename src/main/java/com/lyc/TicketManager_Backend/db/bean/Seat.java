package com.lyc.TicketManager_Backend.db.bean;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = "movieShow")
@ToString(exclude = "movieShow")
@Table(
        uniqueConstraints = @UniqueConstraint(columnNames = {"x", "y", "movie_show_id"})
)
public class Seat implements Serializable {
    @Id
    @GeneratedValue
    long id;
    //        --------------------
    //        |                  |
    //        |      screen      |
    //        |                  |
    //        --------------------
    //        y x ->
    //        |
    //        v
    private int x;
    private int y;
    @ManyToOne(targetEntity = MovieShow.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_show_id", referencedColumnName = "id")
    private MovieShow movieShow;
    @Column(name = "user_id")
    private Long userId;
}
