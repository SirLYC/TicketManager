package com.lyc.TicketManager_Backend.db;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lyc.TicketManager_Backend.db.bean.Movie;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.sql.Date;
import java.time.Duration;

public class JsonTest {
    @Test
    public void testMovie() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        Movie movie = new Movie(0, "1", "2", new Date(System.currentTimeMillis()), "America", Duration.ZERO);
        String s = objectMapper.writeValueAsString(movie);
        Assert.assertEquals(movie, objectMapper.readValue(s, Movie.class));
    }
}
