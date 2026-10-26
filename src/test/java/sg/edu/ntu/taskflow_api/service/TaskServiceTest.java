package sg.edu.ntu.taskflow_api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sg.edu.ntu.taskflow_api.model.Task;
import sg.edu.ntu.taskflow_api.repository.TaskRepository;

/*
 * Unit tests for TaskService.
 * Uses the real in-memory TaskRepository. No Spring context and no AI calls,
 * so these tests run without an OPENAI_API_KEY (important for CI).
 */
class TaskServiceTest {

    private TaskService taskService;

    @BeforeEach
    void setUp() {
        // ChatClient is only used by getTaskSummary() for a non-empty list,
        // which these tests never call, so null is safe here.
        taskService = new TaskService(new TaskRepository(), null);
    }

    private Task newTask(String title) {
        Task task = new Task();
        task.setTitle(title);
        return task;
    }

    @Test
    void createTask_assignsIdAndStoresTask() {
        Task created = taskService.createTask(newTask("Write README"));

        assertNotNull(created.getId());
        assertEquals("Write README", taskService.findTaskById(created.getId()).getTitle());
    }

    @Test
    void findAllTasks_returnsEveryCreatedTask() {
        taskService.createTask(newTask("Task A"));
        taskService.createTask(newTask("Task B"));

        assertEquals(2, taskService.findAllTasks().size());
    }

    @Test
    void findTaskById_returnsNullWhenMissing() {
        assertNull(taskService.findTaskById(999L));
    }

    @Test
    void updateTask_replacesExistingTask() {
        Task created = taskService.createTask(newTask("Old title"));

        taskService.updateTask(created.getId(), newTask("New title"));

        assertEquals("New title", taskService.findTaskById(created.getId()).getTitle());
        assertEquals(1, taskService.findAllTasks().size());
    }

    @Test
    void deleteTask_removesTask() {
        Task created = taskService.createTask(newTask("To delete"));

        taskService.deleteTask(created.getId());

        assertNull(taskService.findTaskById(created.getId()));
    }

    @Test
    void markTaskAsComplete_setsCompletedTrue() {
        Task created = taskService.createTask(newTask("Finish M4"));
        assertFalse(created.isCompleted());

        Task completed = taskService.markTaskAsComplete(created.getId());

        assertTrue(completed.isCompleted());
    }

    @Test
    void markTaskAsComplete_returnsNullWhenMissing() {
        assertNull(taskService.markTaskAsComplete(999L));
    }

    @Test
    void getTaskSummary_whenNoTasks_returnsMessageWithoutCallingAi() {
        assertEquals("You have no tasks yet.", taskService.getTaskSummary());
    }
}