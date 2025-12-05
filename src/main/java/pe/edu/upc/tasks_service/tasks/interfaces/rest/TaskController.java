package pe.edu.upc.tasks_service.tasks.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.tasks_service.tasks.application.clients.iam.IamServiceClient;
import pe.edu.upc.tasks_service.tasks.domain.model.commands.DeleteTaskCommand;
import pe.edu.upc.tasks_service.tasks.domain.model.commands.UpdateTaskStatusCommand;
import pe.edu.upc.tasks_service.tasks.domain.model.queries.GetAllTaskByStatusQuery;
import pe.edu.upc.tasks_service.tasks.domain.model.queries.GetAllTasksByGroupIdQuery;
import pe.edu.upc.tasks_service.tasks.domain.model.queries.GetTaskByIdQuery;
import pe.edu.upc.tasks_service.tasks.domain.services.TaskCommandService;
import pe.edu.upc.tasks_service.tasks.domain.services.TaskQueryService;
import pe.edu.upc.tasks_service.tasks.interfaces.rest.resources.TaskDetailsResource;
import pe.edu.upc.tasks_service.tasks.interfaces.rest.resources.TaskResource;
import pe.edu.upc.tasks_service.tasks.interfaces.rest.resources.UpdateTaskResource;
import pe.edu.upc.tasks_service.tasks.interfaces.rest.transform.TaskDetailsResourceFromEntityAssembler;
import pe.edu.upc.tasks_service.tasks.interfaces.rest.transform.TaskResourceFromEntityAssembler;
import pe.edu.upc.tasks_service.tasks.interfaces.rest.transform.UpdateTaskCommandFromResourceAssembler;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/tasks")
@Tag(name = "Task", description = "Task management API")
@ApiResponse(responseCode = "201", description = "Task Created")
public class TaskController {
  private final TaskQueryService taskQueryService;
  private final TaskCommandService taskCommandService;
  private final IamServiceClient iamServiceClient;

  public TaskController(TaskQueryService taskQueryService,
                        TaskCommandService taskCommandService,
                        IamServiceClient iamServiceClient) {
    this.taskQueryService = taskQueryService;
    this.taskCommandService = taskCommandService;
    this.iamServiceClient = iamServiceClient;
  }

  @GetMapping("/{taskId}")
  @Operation(summary = "Get a task by id", description = "Get a task by id")
  public ResponseEntity<TaskResource> getTaskById(@PathVariable Long taskId,
                                                  @RequestHeader("Authorization") String authorizationHeader) {
    var getTaskByIdQuery = new GetTaskByIdQuery(taskId);
    var task = this.taskQueryService.handle(getTaskByIdQuery);
    if (task.isEmpty()) return ResponseEntity.notFound().build();

    var memberId = task.get().getMember().getId();
    var userResource = iamServiceClient.fetchUserByMemberId(memberId, authorizationHeader);
    if (userResource.isEmpty()) return ResponseEntity.notFound().build();

    var taskResource = TaskResourceFromEntityAssembler.toResourceFromEntity(task.get(), userResource.get());
    return ResponseEntity.ok(taskResource);
  }

  @GetMapping("/details/{taskId}")
  @Operation(summary = "Get task details by id", description = "Get task details by id")
  public ResponseEntity<TaskDetailsResource> getTaskDetailsById(@PathVariable Long taskId) {
    var getTaskByIdQuery = new GetTaskByIdQuery(taskId);
    var task = this.taskQueryService.handle(getTaskByIdQuery);
    if (task.isEmpty()) return ResponseEntity.notFound().build();

    var taskResource = TaskDetailsResourceFromEntityAssembler.toResourceFromEntity(task.get());
    return ResponseEntity.ok(taskResource);
  }

  @GetMapping("/status/{status}")
  @Operation(summary = "Get all tasks by status", description = "Get all tasks by status")
  public ResponseEntity<List<TaskResource>> getAllTasksByStatus(@PathVariable String status,
                                                                @RequestHeader("Authorization") String authorizationHeader) {
    var getAllTasksByStatusQuery = new GetAllTaskByStatusQuery(status);
    var tasks = taskQueryService.handle(getAllTasksByStatusQuery);
    if (tasks.isEmpty()) return ResponseEntity.ok(List.of());

    // Mapear cada Task a TaskResource
    var taskResources = tasks.stream().map(task -> {
          var memberId = task.getMember().getId();

          var userResource = iamServiceClient.fetchUserByMemberId(memberId, authorizationHeader);

          // Si no se encuentra información del usuario, puedes:
          //  (a) retornar null y filtrarlo
          //  (b) lanzar error
          //  (c) continuar pero sin userResource
          // Aquí usaré (a): ignorar tareas sin usuario asociado
          if (userResource.isEmpty()) return null;

          return TaskResourceFromEntityAssembler.toResourceFromEntity(task, userResource.get());
        }).filter(resource -> resource != null)
        .toList();

    return ResponseEntity.ok(taskResources);
  }

  @PutMapping("/{taskId}/status/{status}")
  @Operation(summary = "Update task status", description = "Update task status")
  public ResponseEntity<TaskResource> updateTaskStatus(@PathVariable Long taskId,
                                                       @PathVariable String status,
                                                       @RequestHeader("Authorization") String authorizationHeader) {
    var updateTaskStatusCommand = new UpdateTaskStatusCommand(taskId, status);
    var task = this.taskCommandService.handle(updateTaskStatusCommand);
    if (task.isEmpty()) return ResponseEntity.badRequest().build();

    var memberId = task.get().getMember().getId();
    var userResource = iamServiceClient.fetchUserByMemberId(memberId, authorizationHeader);
    if (userResource.isEmpty()) return ResponseEntity.notFound().build();

    var taskResource = TaskResourceFromEntityAssembler.toResourceFromEntity(task.get(), userResource.get());
    return ResponseEntity.ok(taskResource);
  }

  @PutMapping("/{taskId}")
  @Operation(summary = "Update task", description = "Update task")
  public ResponseEntity<TaskResource> updateTask(@PathVariable Long taskId,
                                                 @RequestBody UpdateTaskResource resource,
                                                 @RequestHeader("Authorization") String authorizationHeader) {
    var updateTaskCommand = UpdateTaskCommandFromResourceAssembler.toCommandFromResource(resource, taskId);
    var task = taskCommandService.handle(updateTaskCommand);
    if (task.isEmpty()) return ResponseEntity.badRequest().build();

    var memberId = task.get().getMember().getId();
    var userResource = iamServiceClient.fetchUserByMemberId(memberId, authorizationHeader);
    if (userResource.isEmpty()) return ResponseEntity.notFound().build();

    var taskResource = TaskResourceFromEntityAssembler.toResourceFromEntity(task.get(), userResource.get());
    return ResponseEntity.ok(taskResource);
  }

  @DeleteMapping("/{taskId}")
  @Operation(summary = "Delete a task by id", description = "Delete a task by id")
  public ResponseEntity<Void> deleteTask(@PathVariable Long taskId) {
    var deleteTaskCommand = new DeleteTaskCommand(taskId);
    this.taskCommandService.handle(deleteTaskCommand);

    return ResponseEntity.noContent().build();
  }

  @GetMapping
  @Operation(summary = "Get all tasks by groupId", description = "Get all tasks by groupId")
  public ResponseEntity<List<TaskResource>> getTasksByGroupId(
      @RequestParam Long groupId,
      @RequestHeader("Authorization") String authorizationHeader) {
    var getAllTasksByGroupId = new GetAllTasksByGroupIdQuery(groupId);
    var tasks = taskQueryService.handle(getAllTasksByGroupId);
    if (tasks.isEmpty()) return ResponseEntity.ok(List.of());

    var taskResources = tasks.stream().map(task -> {
          var memberId = task.getMember().getId();
          var userResource = iamServiceClient.fetchUserByMemberId(memberId, authorizationHeader);
          if (userResource.isEmpty()) return null;

          return TaskResourceFromEntityAssembler.toResourceFromEntity(task, userResource.get());
        }).filter(resource -> resource != null)
        .toList();

    return ResponseEntity.ok(taskResources);
  }
}
