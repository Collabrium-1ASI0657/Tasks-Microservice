package pe.edu.upc.tasks_service.tasks.domain.services;

import pe.edu.upc.tasks_service.tasks.domain.model.aggregates.Task;
import pe.edu.upc.tasks_service.tasks.domain.model.commands.*;

import java.util.Optional;

public interface TaskCommandService {
  Optional<Task> handle(CreateTaskCommand command);
  Optional<Task> handle(UpdateTaskCommand command);
  void handle(DeleteTaskCommand command);
  Optional<Task> handle(UpdateTaskStatusCommand command);
  void handle(DeleteTasksByMemberId command);
}