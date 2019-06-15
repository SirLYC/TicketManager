package com.lyc.TicketManager_Backend.db.bean;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;

import javax.persistence.*;
import java.io.IOException;
import java.sql.Date;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"actors", "directors"})
@ToString(exclude = {"actors", "directors"})
public class Movie {
    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.REFRESH}, fetch = FetchType.EAGER)
    @JoinTable(
            name = "movie_types",
            joinColumns = {@JoinColumn(name = "movie_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "type_id", referencedColumnName = "id")}
    )
    private final Set<MovieType> types = new HashSet<>();
    @ManyToMany(mappedBy = "movies", fetch = FetchType.EAGER)
    private final Set<Director> directors = new HashSet<>();
    @ManyToMany(mappedBy = "movies", fetch = FetchType.EAGER)
    private final Set<Actor> actors = new HashSet<>();
    @Id
    @GeneratedValue
    private long id;
    @Column(nullable = false)
    private String name;
    @JsonProperty(value = "cover_url")
    @Column(name = "cover_url", nullable = false)
    private String coverUrl;
    @JsonProperty(value = "release_time")
    private Date releaseTime;
    private String country;
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @Convert(converter = DurationConverter.class)
    private Duration duration;

    @JsonSerialize(using = DurationJsonSerializer.class)
    public Duration getDuration() {
        return duration;
    }

    @JsonDeserialize(using = DurationJsonDeserializer.class)
    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    private static class DurationConverter implements AttributeConverter<Duration, Long> {

        @Override
        public Long convertToDatabaseColumn(Duration attribute) {
            return attribute.get(ChronoUnit.SECONDS);
        }

        @Override
        public Duration convertToEntityAttribute(Long dbData) {
            return Duration.of(dbData, ChronoUnit.SECONDS);
        }
    }

    private static class DurationJsonDeserializer extends JsonDeserializer<Duration> {
        @Override
        public Duration deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            long time = p.getLongValue();
            return Duration.of(time, ChronoUnit.SECONDS);
        }
    }

    private static class DurationJsonSerializer extends JsonSerializer<Duration> {

        @Override
        public void serialize(Duration value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (value == null) {
                gen.writeNumber(-1);
            } else {
                gen.writeNumber(value.getSeconds());
            }
        }
    }
}
