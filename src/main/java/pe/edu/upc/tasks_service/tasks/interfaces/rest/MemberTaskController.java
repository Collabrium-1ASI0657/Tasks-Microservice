package pe.edu.upc.tasks_service.tasks.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.tasks_service.tasks.application.clients.iam.IamServiceClient;
import pe.edu.upc.tasks_service.tasks.domain.model.queries.GetAllTasksByMemberId;
import pe.edu.upc.tasks_service.tasks.domain.model.valueobjects.TaskStatus;
import pe.edu.upc.tasks_service.tasks.domain.services.TaskCommandService;
import pe.edu.upc.tasks_service.tasks.domain.services.TaskQueryService;
import pe.edu.upc.tasks_service.tasks.interfaces.rest.resources.CreateTaskResource;
import pe.edu.upc.tasks_service.tasks.interfaces.rest.resources.TaskResource;
import pe.edu.upc.tasks_service.tasks.interfaces.rest.transform.CreateTaskCommandFromResourceAssembler;
import pe.edu.upc.tasks_service.tasks.interfaces.rest.transform.TaskResourceFromEntityAssembler;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/members")
@Tag(name = "Tasks Member ", description = "Tasks Member endpoints")
@CrossOrigin(origins = "*", methods = { RequestMethod.POST, RequestMethod.GET, RequestMethod.PUT, RequestMethod.DELETE })
public class MemberTaskController {
  private final TaskCommandService taskCommandService;
  private final TaskQueryService taskQueryService;
  private final IamServiceClient iamServiceClient;

  public MemberTaskController(TaskCommandService taskCommandService,
                              TaskQueryService taskQueryService,
                              IamServiceClient iamServiceClient) {
    this.taskCommandService = taskCommandService;
    this.taskQueryService = taskQueryService;
    this.iamServiceClient = iamServiceClient;
  }

  @PostMapping("/{memberId}/tasks")
  @Operation(summary = "Create a new task", description = "Creates a new task")
  public ResponseEntity<TaskResource> createTask(@PathVariable Long memberId,
                                                 @RequestBody CreateTaskResource resource,
                                                 @RequestHeader("Authorization") String authorizationHeader) {
    var createTaskCommand = CreateTaskCommandFromResourceAssembler.toCommandFromResource(resource, memberId);
    var task = taskCommandService.handle(createTaskCommand);
    if(task.isEmpty()) return ResponseEntity.badRequest().build();

    var userResource = iamServiceClient.fetchUserByMemberId(memberId, authorizationHeader);
    if (userResource.isEmpty()) return ResponseEntity.notFound().build();

    var taskResource = TaskResourceFromEntityAssembler.toResourceFromEntity(task.get(), userResource.get());

    return new ResponseEntity<>(taskResource, HttpStatus.CREATED);
  }

  @GetMapping("/{memberId}/tasks")
  @Operation(summary = "Get all tasks by member id", description = "Get all tasks by member id")
  public ResponseEntity<List<TaskResource>> getAllTasksByMemberId(@PathVariable Long memberId,
                                                                  @RequestHeader("Authorization") String authorizationHeader) {
    // Obtener tareas del servicio de consulta
    var getAllTasksByMemberId = new GetAllTasksByMemberId(memberId);
    var tasks = taskQueryService.handle(getAllTasksByMemberId);
    if (tasks.isEmpty()) return ResponseEntity.ok(List.of());

    // Obtener usuario desde IAM (necesario para construir TaskResource)
    var userResource = iamServiceClient.fetchUserByMemberId(memberId, authorizationHeader);
    if (userResource.isEmpty()) return ResponseEntity.notFound().build();

    // Convertir cada entidad Task a TaskResource
    var taskResources = tasks.stream()
        .map(task -> TaskResourceFromEntityAssembler.toResourceFromEntity(task, userResource.get()))
        .toList();
    return ResponseEntity.ok(taskResources);
  }

  @GetMapping("/{memberId}/tasks/next")
  @Operation(summary = "Get the next task by member id", description = "Get the next task by member id")
  public ResponseEntity<TaskResource> getLastNextByMemberId(@PathVariable Long memberId,
                                                            @RequestHeader("Authorization") String authorizationHeader) {
    var getAllTasksByMemberId = new GetAllTasksByMemberId(memberId);
    var tasks = taskQueryService.handle(getAllTasksByMemberId);
    if (tasks.isEmpty()) return ResponseEntity.notFound().build();

    var inProgressTasks = tasks.stream()
        .filter(task -> task.getStatus().equals(TaskStatus.IN_PROGRESS))
        .collect(Collectors.toList());
    var now = LocalDateTime.now(ZoneId.of("UTC"));

    var nextTask = inProgressTasks.stream()
        .filter(task -> {
          if (task.getDueDate() == null) return false;
          LocalDateTime dueDate = task.getDueDate().toInstant()
              .atZone(ZoneId.systemDefault())
              .toLocalDateTime();
          return !dueDate.isBefore(now);
        })
        .min((t1, t2) -> {
          LocalDateTime d1 = t1.getDueDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
          LocalDateTime d2 = t2.getDueDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
          return d1.compareTo(d2);
        });

    var userResource = iamServiceClient.fetchUserByMemberId(memberId, authorizationHeader);
    if (userResource.isEmpty()) return ResponseEntity.notFound().build();

    var taskResource = TaskResourceFromEntityAssembler.toResourceFromEntity(nextTask.get(), userResource.get());

    return ResponseEntity.ok(taskResource);
  }

}
