package sg.edu.ntu.taskflow_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import sg.edu.ntu.taskflow_api.model.Task;
import sg.edu.ntu.taskflow_api.service.TaskService;

@SpringBootApplication
public class TaskflowApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(TaskflowApiApplication.class, args);
	}

	// Seed some example tasks every time the app starts (data is in memory only)
	@Bean
	CommandLineRunner seedDefaultTasks(TaskService taskService) {
		return args -> {
			seedTask(taskService, "Sample task",
					"Confirm the Taskflow API is running.", false);
			seedTask(taskService, "Write Dockerfile",
					"Create a multi-stage Docker build for the TaskFlow API.", true);
			seedTask(taskService, "Deploy to Railway",
					"Deploy the API to Railway and generate a public domain.", true);
			seedTask(taskService, "Add CI pipeline",
					"Build, test and publish the image to Docker Hub with GitHub Actions.", true);
			seedTask(taskService, "Improve the frontend",
					"Add buttons to create, complete and delete tasks from the React page.", false);
		};
	}

	private static void seedTask(TaskService taskService, String title,
			String description, boolean completed) {
		Task task = new Task();
		task.setTitle(title);
		task.setDescription(description);
		task.setCompleted(completed);
		taskService.createTask(task);
	}

}