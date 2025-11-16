package pe.edu.upc.tasks_service.tasks.application.internal.queryservices;

import org.springframework.stereotype.Service;
import pe.edu.upc.tasks_service.tasks.domain.model.aggregates.Task;
import pe.edu.upc.tasks_service.tasks.domain.model.queries.*;
import pe.edu.upc.tasks_service.tasks.domain.model.valueobjects.GroupId;
import pe.edu.upc.tasks_service.tasks.domain.model.valueobjects.TaskStatus;
import pe.edu.upc.tasks_service.tasks.domain.services.TaskQueryService;
import pe.edu.upc.tasks_service.tasks.infrastructure.persistence.jpa.repositories.TaskRepository;

import java.util.List;
import java.util.Optional;

@Service
public class TaskQueryServiceImpl implements TaskQueryService {
  private final TaskRepository taskRepository;

  public TaskQueryServiceImpl(TaskRepository taskRepository) {
    this.taskRepository = taskRepository;
  }


  @Override
  public List<Task> handle(GetAllTasksQuery query) {
    return this.taskRepository.findAll();
  }

  @Override
  public Optional<Task> handle(GetTaskByIdQuery query) {
    return this.taskRepository.findById(query.taskId());
  }

  @Override
  public List<Task> handle(GetAllTasksByMemberId query) {
    return this.taskRepository.findByMember_Id(query.memberId());
  }

  @Override
  public List<Task> handle(GetAllTaskByStatusQuery query) {
    TaskStatus taskStatus = TaskStatus.valueOf(query.taskStatus());
    return taskRepository.findByStatus(taskStatus);
  }

  @Override
  public List<Task> handle(GetAllTasksByGroupIdQuery query) {
    return taskRepository.findByGroupId(new GroupId(query.groupId()));
  }
}
