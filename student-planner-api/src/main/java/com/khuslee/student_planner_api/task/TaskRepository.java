package com.khuslee.student_planner_api.task;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<TaskEntity, Long> {


    List<TaskEntity> findByUsernameAndCompletedFalseOrderByPositionAsc(String username);

    Optional<TaskEntity> findByIdAndUsername(Long id, String username);

    int countByUsernameAndCompletedFalse(String username);
}
