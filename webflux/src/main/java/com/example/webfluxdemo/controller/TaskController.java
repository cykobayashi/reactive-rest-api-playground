package com.example.webfluxdemo.controller;

import com.example.webfluxdemo.exception.TaskNotFoundException;
import com.example.webfluxdemo.model.Task;
import com.example.webfluxdemo.payload.ErrorResponse;
import com.example.webfluxdemo.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.time.Instant;

@RestController
public class TaskController {

    @Autowired
    private TaskRepository taskRepository;

    @GetMapping("/tasks")
    public Flux<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    @PostMapping("/tasks")
    public Mono<Task> createTasks(@RequestBody Task task) {
        task.setCreatedAt(Instant.now());
        task.setCompleted(false);

        return taskRepository.save(task);
    }

    @GetMapping("/tasks/{id}")
    public Mono<ResponseEntity<Task>> getTaskById(@PathVariable(value = "id") String taskId) {
        return taskRepository.findById(taskId)
                .map(savedTask -> ResponseEntity.ok(savedTask))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PutMapping("/tasks/{id}")
    public Mono<ResponseEntity<Task>> updateTask(@PathVariable(value = "id") String taskId,
                                                 @RequestBody Task task) {
        return taskRepository.findById(taskId)
                .flatMap(existingTask -> {
                    existingTask.setDescription(task.getDescription());
                    existingTask.setCompleted(task.getCompleted());
                    existingTask.setDueTo(task.getDueTo());
                    return taskRepository.save(existingTask);
                })
                .map(updateTask -> new ResponseEntity<>(updateTask, HttpStatus.OK))
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/tasks/{id}")
    public Mono<ResponseEntity<Void>> deleteTask(@PathVariable(value = "id") String taskId) {

        return taskRepository.findById(taskId)
                .flatMap(existingTask ->
                        taskRepository.delete(existingTask)
                            .then(Mono.just(new ResponseEntity<Void>(HttpStatus.OK)))
                )
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // Tasks are Sent to the client as Server Sent Events
    @GetMapping(value = "/stream/tasks", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Task> streamAllTasks() {
        return taskRepository.findAll();
    }




    /*
        Exception Handling Examples (These can be put into a @ControllerAdvice to handle exceptions globally)
    */

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity handleDuplicateKeyException(DuplicateKeyException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse("A Task with the same text already exists"));
    }

    @ExceptionHandler(TaskNotFoundException.class)
    public ResponseEntity handleTaskNotFoundException(TaskNotFoundException ex) {
        return ResponseEntity.notFound().build();
    }

}
