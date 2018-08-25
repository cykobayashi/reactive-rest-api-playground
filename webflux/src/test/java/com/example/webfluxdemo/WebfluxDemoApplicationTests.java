package com.example.webfluxdemo;

import com.example.webfluxdemo.model.Task;
import com.example.webfluxdemo.repository.TaskRepository;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Collections;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WebfluxDemoApplicationTests {

	@Autowired
	private WebTestClient webTestClient;

	@Autowired
    TaskRepository taskRepository;

	@Test
	public void testCreateTask() {
		Task task = Task.builder()
                .description("This is a Test Task")
                .dueTo(Instant.now())
                .build();

		webTestClient.post().uri("/tasks")
				.contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .body(Mono.just(task), Task.class)
				.exchange()
				.expectStatus().isOk()
				.expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
				.expectBody()
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.description").isEqualTo("This is a Test Task");
	}

	@Test
    public void testGetAllTasks() {
	    webTestClient.get().uri("/tasks")
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBodyList(Task.class);
    }

    @Test
    public void testGetSingleTask() {
        Task task = taskRepository.save(Task.builder().description("Hello, World!").build()).block();

        webTestClient.get()
                .uri("/tasks/{id}", Collections.singletonMap("id", task.getId()))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(response ->
                        Assertions.assertThat(response.getResponseBody()).isNotNull());
    }

    @Test
    public void testUpdateTask() {
        Task task = taskRepository.save(Task.builder().description("Initial Task").build()).block();

        Task newTaskData = Task.builder().description("Updated Task").build();

        webTestClient.put()
                .uri("/tasks/{id}", Collections.singletonMap("id", task.getId()))
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .body(Mono.just(newTaskData), Task.class)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBody()
                .jsonPath("$.description").isEqualTo("Updated Task");
    }

    @Test
    public void testDeleteTask() {
	    Task task = taskRepository.save(Task.builder().description("To be deleted").build()).block();

	    webTestClient.delete()
                .uri("/tasks/{id}", Collections.singletonMap("id",  task.getId()))
                .exchange()
                .expectStatus().isOk();
    }
}
