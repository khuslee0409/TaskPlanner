package com.khuslee.student_planner_api.task;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService service;

    public TaskController(TaskService service) {
        this.service = service;
    }

    // Create task
    @PostMapping
    public TaskEntity createTask(@Valid @RequestBody CreateTaskRequest request,
                                 Principal principal) throws ParseException {
        String username = principal.getName();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy hh:mm a");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC")); // Force UTC parsing
        Date deadlineDate = sdf.parse(request.getDeadline());
        return service.createTask(username, request.getTitle(), deadlineDate);
    }

    // Get tasks (ordered)
    @GetMapping
    public List<TaskEntity> getTasks(Principal principal) {
        return service.getTasks(principal.getName());
    }

    // Rename task
    @PutMapping("/{id}/rename")
    public void renameTask(@PathVariable Long id,
                           @RequestBody RenameTaskRequest request,
                           Principal principal) {
        service.renameTask(principal.getName(), id, request.getTitle());
    }

    // Update progress
    @PutMapping("/{id}/progress")
    public void updateProgress(@PathVariable Long id,
                               @RequestBody UpdateProgressRequest request,
                               Principal principal) {
        service.updateProgress(principal.getName(), id, request.getProgress());
    }

    // Complete
    @PutMapping("/{id}/complete")
    public void completeTask(@PathVariable Long id,
                             Principal principal) {
        service.completeTask(principal.getName(), id);
    }

    // Reorder
    @PutMapping("/reorder")
    public void reorderTasks(@RequestBody List<Long> orderedTaskIds,
                             Principal principal) {
        service.reorderTasks(principal.getName(), orderedTaskIds);
    }
}
