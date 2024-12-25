package com.api.pmtool.dtos;

public interface Views {
    public interface Basic {}
    public interface Detailed extends Basic {}
}

/*  
user guide
 *  @GetMapping
    @JsonView(Views.Basic.class) // Use Basic view for this endpoint
    public ResponseEntity<List<TasksEntity>> getBasicTasks() {
        List<TasksEntity> tasks = taskService.findAll();
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/{id}")
    @JsonView(Views.Detailed.class) // Use Detailed view for this endpoint
    public ResponseEntity<TasksEntity> getTaskDetails(@PathVariable UUID id) {
        TasksEntity task = taskService.findById(id);
        return ResponseEntity.ok(task);
    }

@Entity
     @JsonView(Views.Basic.class)
    private LocalDate dueDate;

    @JsonView(Views.Detailed.class) // Only include in Detailed view
    private List<Comments> comments = new ArrayList<>();
*/