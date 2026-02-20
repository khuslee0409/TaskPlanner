package com.khuslee.student_planner_api.task;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import org.springframework.boot.data.autoconfigure.metrics.DataRepositoryMetricsAutoConfiguration;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "tasks")
public class TaskEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Owner (from JWT: auth.getName())
    @Column(nullable = false)
    private String username;

    // Task text
    @Column(nullable = false)
    private String title;

    // Order in the list (0,1,2,3...)
    @Column(nullable = false)
    private int position;

    // Progress bar (0–100)
    @Column(nullable = false)
    private int progress;

    // Soft delete / done
    @Column(nullable = false)
    private boolean completed;

    // Metadata
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Column(nullable = false)
    private Date deadline;

    // ---- Constructors ----
    public TaskEntity() {}

    public TaskEntity(String username, String title, int position, Date deadline) {
        this.username = username;
        this.title = title;
        this.position = position;
        this.progress = 0;
        this.completed = false;
        this.deadline = deadline;
    }


    public Long getId() { return id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }

    public int getProgress() { return progress; }
    public void setProgress(int progress) {
        this.progress = Math.max(0, Math.min(100, progress));
    }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public Date getDeadline() { return deadline; }
}
