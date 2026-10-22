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

	@Bean
	CommandLineRunner seedDefaultTask(TaskService taskService) {
		return args -> {
			Task task = new Task();
			task.setTitle("Sample task");
			task.setDescription("Confirm the Taskflow API is running.");
			task.setCompleted(false);
			taskService.createTask(task);
		};
	}

}
