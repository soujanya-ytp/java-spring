package com.example.tasks;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TaskTrackerApplicationTests {
    @Autowired MockMvc mvc;

    @Test void healthEndpointWorks() throws Exception {
        mvc.perform(get("/health")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("ok"));
    }

    @Test void createAndReadTask() throws Exception {
        String response = mvc.perform(post("/api/tasks").contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Ship release\",\"description\":\"v1\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.title").value("Ship release"))
                .andReturn().getResponse().getContentAsString();
        String id = response.split("\\\"id\\\":\\\"")[1].split("\\\"")[0];
        mvc.perform(get("/api/tasks/" + id)).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(id));
    }

    @Test void blankTitleIsRejected() throws Exception {
        mvc.perform(post("/api/tasks").contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"   \"}"))
                .andExpect(status().isBadRequest());
    }
}
