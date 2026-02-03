package com.khuslee.student_planner_api.task;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TaskService {

    private final TaskRepository tasks;

    public TaskService(TaskRepository tasks) {
        this.tasks = tasks;
    }


    // Create task

    public TaskEntity createTask(String username, String title) {

        int position = tasks.countByUsernameAndCompletedFalse(username);

        TaskEntity task = new TaskEntity(username, title, position);
        return tasks.save(task);
    }


    // Get active tasks (ordered)

    public List<TaskEntity> getTasks(String username) {
        return tasks.findByUsernameAndCompletedFalseOrderByPositionAsc(username);
    }


    // Rename task

    public void renameTask(String username, Long taskId, String newTitle) {

        TaskEntity task = tasks.findByIdAndUsername(taskId, username)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        task.setTitle(newTitle);
    }


    // Update progress (0–100)

    public void updateProgress(String username, Long taskId, int progress) {

        TaskEntity task = tasks.findByIdAndUsername(taskId, username)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        task.setProgress(progress);
    }


    // Mark task as completed (soft delete)

    public void completeTask(String username, Long taskId) {

        TaskEntity task = tasks.findByIdAndUsername(taskId, username)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        task.setCompleted(true);
    }


    // Reorder tasks (drag & drop)

    public void reorderTasks(String username, List<Long> orderedTaskIds) {

        List<TaskEntity> userTasks =
                tasks.findByUsernameAndCompletedFalseOrderByPositionAsc(username);

        // Validate: same size
        if (orderedTaskIds.size() != userTasks.size()) {
            throw new IllegalArgumentException("Invalid reorder list");
        }

        // Update positions
        for (int i = 0; i < orderedTaskIds.size(); i++) {

            Long taskId = orderedTaskIds.get(i);

            TaskEntity task = userTasks.stream()
                    .filter(t -> t.getId().equals(taskId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Invalid task id"));

            task.setPosition(i);
        }
    }
}
