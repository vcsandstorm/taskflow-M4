package sg.edu.ntu.taskflow_api.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import java.util.List;

import sg.edu.ntu.taskflow_api.model.Task;
import sg.edu.ntu.taskflow_api.repository.TaskRepository;

/*
The service holds your business logic and calls the repository. Write these methods:
• findAllTasks()
• findTaskById(Long id)
• createTask(Task task)
• updateTask(Long id, Task task)
• deleteTask(Long id)
• markTaskAsComplete(Long id)
For markTaskAsComplete, find the task, set its completed flag to true, and save it back.
Use constructor injection to bring the repository into the service.
*/

@Service
public class TaskService {
    private TaskRepository taskRepository;
    private final ChatClient chatClient; 

    public TaskService(TaskRepository taskRepository, ChatClient chatClient) {
        this.taskRepository = taskRepository;
        this.chatClient = chatClient;
    }

    //Find all Tasks
    public List<Task> findAllTasks() {
        return taskRepository.findAll();
    }

    //Find task by ID
    public Task findTaskById(Long id) {
        return taskRepository.findById(id);
    }

    public Task createTask(Task task) {
        return taskRepository.save(task);
    }

    public Task updateTask(Long id, Task task) {
        task.setId(id);
        return taskRepository.save(task);
    }

    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }

    public Task markTaskAsComplete(Long id) {
        Task task = taskRepository.findById(id);
        if (task == null) {
            return null;
        }

        task.setCompleted(true);
        return taskRepository.save(task);
    }

    //Ask the AI to Summarize all tasks
    public String getTaskSummary() {
        List<Task> tasks = findAllTasks();

        //check if task list is empty
        if (tasks.isEmpty()) {
            return "You have no tasks yet.";
        }

        //Send the tasks to AI and return its response
        return chatClient.prompt()
                .system("You are a personal assistant. Summarize the following tasks in short, "
                        + "plain English. Clearly say which tasks are done and which are still pending.")
                .user(tasks.toString())
                .call()
                .content();

    }

}
