package sg.edu.ntu.taskflow_api.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;
import lombok.NoArgsConstructor;

/*
• It is a plain POJO. Do not add any Spring annotations to it.
• Decide your own fields. At minimum it should have an id, a title, and a completed flag.
• Use Lombok for getters, setters and constructors.
*/

@Data 
@NoArgsConstructor 
@JsonPropertyOrder({ "id", "title", "description", "completed" })
public class Task {
    private Long id;
    private String title;
    private String description;
    private boolean completed;
}
