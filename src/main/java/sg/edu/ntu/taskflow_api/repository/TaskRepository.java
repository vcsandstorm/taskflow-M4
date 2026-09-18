package sg.edu.ntu.taskflow_api.repository;
import sg.edu.ntu.taskflow_api.model.Task;


import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Repository;


/*
• Store the tasks in memory. Choose either an ArrayList or a HashMap.
• If you use an ArrayList, you will need to loop through the list to find a task by its id. Write a small
helper method for this.
• If you use a HashMap, you can look a task up directly by its key.
• Generate your own ids. A simple counter is enough.
• Initialise your collection so it is never null.
*/


@Repository 
public class TaskRepository {
    private final Map<Long, Task> tasks = new HashMap<>();
    private long nextId = 1L;

    //Create Task
    public Task save(Task task) {
        if (task.getId() == null) {
            task.setId(nextId++);
        }

        tasks.put(task.getId(), task);
        return task;
    }

    //Find all tasks
    public List<Task> findAll() {
        return new ArrayList<>(tasks.values());
    }

    //Find task by id
    public Task findById(Long id) {
        return tasks.get(id);
    }

    //Delete task by id
    public void deleteById(Long id) {
        tasks.remove(id);
    }

}
