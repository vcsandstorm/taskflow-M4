package sg.edu.ntu.taskflow_api.controller;

import java.util.List;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import sg.edu.ntu.taskflow_api.service.TaskService;
import sg.edu.ntu.taskflow_api.model.Task;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;




@RestController 
@RequestMapping("/api/tasks")
public class TaskController {
    
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    //Create a new task
    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody Task task){
        Task createdTask = taskService.createTask(task);
        return new ResponseEntity<>(createdTask, HttpStatus.CREATED);
    }
    
    //Get all task
    @GetMapping("")
    public ResponseEntity<List<Task>> findAllTasks() {
        List<Task> allTasks = taskService.findAllTasks();
        return new ResponseEntity<>(allTasks, HttpStatus.OK);
    }

    //Get one task by id
    @GetMapping("/{id}")
    public ResponseEntity<Task> findTaskById(@PathVariable Long id) {
        Task foundTask = taskService.findTaskById(id);
        if (foundTask == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(foundTask, HttpStatus.OK);
    }
    
    //Update an existing task
    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable Long id, @RequestBody Task task) {
        Task updatedTask = taskService.updateTask(id, task);
        if (updatedTask == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(updatedTask, HttpStatus.OK);
    }

    //Delete a task by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Task> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    //Mark a task as completed
    @PutMapping("/{id}/complete")
    public ResponseEntity<Task> markTaskAsComplete(@PathVariable Long id) {
        Task completedTask = taskService.markTaskAsComplete(id);
        return new ResponseEntity<>(completedTask, HttpStatus.OK);
    }

    //Get an AI summary of all tasks
    @GetMapping("/summary")
    public ResponseEntity<String> getTaskSummary() {
        String summary = taskService.getTaskSummary();
        return new ResponseEntity<>(summary, HttpStatus.OK);
    }
}
