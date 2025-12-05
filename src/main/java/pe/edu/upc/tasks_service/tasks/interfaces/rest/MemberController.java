package pe.edu.upc.tasks_service.tasks.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.tasks_service.tasks.application.clients.groups.GroupsServiceClient;
import pe.edu.upc.tasks_service.tasks.application.clients.iam.IamServiceClient;
import pe.edu.upc.tasks_service.tasks.domain.model.commands.DeleteTasksByMemberId;
import pe.edu.upc.tasks_service.tasks.domain.model.queries.*;
import pe.edu.upc.tasks_service.tasks.domain.model.valueobjects.TaskStatus;
import pe.edu.upc.tasks_service.tasks.domain.services.MemberQueryService;
import pe.edu.upc.tasks_service.tasks.domain.services.TaskCommandService;
import pe.edu.upc.tasks_service.tasks.domain.services.TaskQueryService;
import pe.edu.upc.tasks_service.tasks.interfaces.rest.resources.ExtendedGroupResource;
import pe.edu.upc.tasks_service.tasks.interfaces.rest.resources.MemberOnlyResource;
import pe.edu.upc.tasks_service.tasks.interfaces.rest.resources.MemberResource;
import pe.edu.upc.tasks_service.tasks.interfaces.rest.resources.TaskResource;
import pe.edu.upc.tasks_service.tasks.interfaces.rest.transform.ExtendedGroupResourceFromEntityAssembler;
import pe.edu.upc.tasks_service.tasks.interfaces.rest.transform.MemberResourceFromEntityAssembler;
import pe.edu.upc.tasks_service.tasks.interfaces.rest.transform.TaskResourceFromEntityAssembler;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/member")
@Tag(name = "Member", description = "Member API")
@ApiResponse(responseCode = "201", description = "Member created")
public class MemberController {
  private final MemberQueryService memberQueryService;
  private final GroupsServiceClient groupsServiceClient;
  private final TaskQueryService taskQueryService;
  private final TaskCommandService taskCommandService;
  private final IamServiceClient iamServiceClient;

  public MemberController(MemberQueryService memberQueryService,
                          GroupsServiceClient groupsServiceClient,
                          TaskQueryService taskQueryService,
                          IamServiceClient iamServiceClient,
                          TaskCommandService taskCommandService) {
    this.memberQueryService = memberQueryService;
    this.groupsServiceClient = groupsServiceClient;
    this.taskQueryService = taskQueryService;
    this.iamServiceClient = iamServiceClient;
    this.taskCommandService = taskCommandService;
  }

  @GetMapping()
  @Operation(summary = "Get members by groupId",  description = "Fetches all the members of a group.")
  public ResponseEntity<List<MemberResource>> getMembersByGroupId(@RequestParam Long groupId,
                                                                  @RequestHeader("Authorization") String authorizationHeader) {
    var getMembersByGroupIdQuery = new GetMembersByGroupIdQuery(groupId, authorizationHeader);
    var members = this.memberQueryService.handle(getMembersByGroupIdQuery);
    if (members == null || members.isEmpty()) {
      return ResponseEntity.noContent().build();
    }

    // Mapear cada UserResource a MemberResource
    var memberResources = members.stream()
        .map(user -> MemberResourceFromEntityAssembler.toResourceFromUserResource(user, user.member().id()))
        .toList();

    return ResponseEntity.ok(memberResources);
  }

  @GetMapping("/details")
  @Operation(summary = "Get member details by authentication", description = "Fetches the details of the authenticated member.")
  public ResponseEntity<MemberResource> getMemberByAuthentication(@RequestHeader("Authorization") String authorizationHeader,
                                                                  @RequestHeader("X-Username") String username) {
    var getMemberByUsernameQuery = new GetMemberByUsernameQuery(username, authorizationHeader);
    var memberWithUserInfo = this.memberQueryService.handle(getMemberByUsernameQuery);
    if (memberWithUserInfo.isEmpty()) return ResponseEntity.notFound().build();

    var memberResource = MemberResourceFromEntityAssembler
        .toResourceFromUserResource(memberWithUserInfo.get(), memberWithUserInfo.get().member().id());

    return ResponseEntity.ok(memberResource);
  }


  @GetMapping("/details/{memberId}")
  @Operation(summary = "Get member details by member ID", description = "Fetches the details of a member by their ID.")
  public ResponseEntity<MemberResource> getMemberById(@PathVariable Long memberId,
                                                      @RequestHeader("Authorization") String authorizationHeader) {
    var getMemberInfoByIdQuery = new GetMemberInfoByIdQuery(memberId, authorizationHeader);
    var memberWithUserInfo = this.memberQueryService.handle(getMemberInfoByIdQuery);
    if (memberWithUserInfo.isEmpty()) return ResponseEntity.notFound().build();

    var memberResource = MemberResourceFromEntityAssembler.toResourceFromUserResource(memberWithUserInfo.get(), memberId);
    return ResponseEntity.ok(memberResource);
  }

  @GetMapping("{memberId}")
  @Operation(summary = "Get member only by member ID", description = "Fetches the member by their ID.")
  public ResponseEntity<MemberOnlyResource> getMemberOnlyById(@PathVariable Long memberId) {
    var getMemberByIdQuery = new GetMemberByIdQuery(memberId);
    var member = this.memberQueryService.handle(getMemberByIdQuery);
    if (member.isEmpty()) return ResponseEntity.notFound().build();
    var memberResource = MemberResourceFromEntityAssembler.toResourceFromEntity(member.get());

    return ResponseEntity.ok(memberResource);
  }

  @GetMapping("/group")
  @Operation(summary = "Get group by member authenticated", description = "Retrieve the group associated with the authenticated member")
  public ResponseEntity<ExtendedGroupResource> getGroupByMemberId(@RequestHeader("Authorization") String authorizationHeader,
                                                                  @RequestHeader("X-Username") String username){
    var getMemberByUsernameQuery = new GetMemberByUsernameQuery(username, authorizationHeader);
    var memberWithUserInfo = this.memberQueryService.handle(getMemberByUsernameQuery);
    if (memberWithUserInfo.isEmpty()) return ResponseEntity.notFound().build();

    var groupId = memberWithUserInfo.get().member().groupId();
    var group = groupsServiceClient.fetchGroupByGroupId(groupId);
    if (group.isEmpty()) return ResponseEntity.notFound().build();

    var getMembersByGroupIdQuery = new GetMembersByGroupIdQuery(groupId, authorizationHeader);
    var members = this.memberQueryService.handle(getMembersByGroupIdQuery);
    if (members == null || members.isEmpty()) {
      return ResponseEntity.noContent().build();
    }

    var extendedGroupResource = ExtendedGroupResourceFromEntityAssembler.toResourceFromEntity(group.get(), members);

    return ResponseEntity.ok(extendedGroupResource);
  }

  @GetMapping("/tasks")
  @Operation(summary = "Get all tasks by authenticated member", description = "Fetches all tasks for the authenticated member.")
  public ResponseEntity<List<TaskResource>> getAllTasksByMemberAuthenticated(
      @RequestHeader("X-Username") String username,
      @RequestHeader("Authorization") String authorizationHeader) {
    var getMemberByUsernameQuery = new GetMemberByUsernameQuery(username, authorizationHeader);
    var member = this.memberQueryService.handle(getMemberByUsernameQuery);
    if(member.isEmpty()) return ResponseEntity.notFound().build();

    var memberId = member.get().member().id();
    var getAllTasksByMemberId = new GetAllTasksByMemberId(memberId);

    var tasks = taskQueryService.handle(getAllTasksByMemberId);
    if(tasks.isEmpty()) return ResponseEntity.noContent().build();

    var userResource = iamServiceClient.fetchUserByMemberId(memberId, authorizationHeader);
    if (userResource.isEmpty()) return ResponseEntity.notFound().build();

    var taskResources = tasks.stream()
        .map(task -> TaskResourceFromEntityAssembler.toResourceFromEntity(task, userResource.get()))
        .toList();

    return ResponseEntity.ok(taskResources);
  }

  @DeleteMapping("/group/leave")
  @Operation(summary = "Leave group by member authenticated", description = "Allows the authenticated member to leave their group.")
  public ResponseEntity<Void> leaveGroupByMemberAuthenticated(
      @RequestHeader("X-Username") String username,
      @RequestHeader("Authorization") String authorizationHeader) {
    var getMemberByUsernameQuery = new GetMemberByUsernameQuery(username, authorizationHeader);
    var member = this.memberQueryService.handle(getMemberByUsernameQuery);
    if(member.isEmpty()) return ResponseEntity.notFound().build();

    var groupId = member.get().member().groupId();
    var memberId = member.get().member().id();

    var deleteTasksByMemberId = new DeleteTasksByMemberId(memberId, groupId);
    taskCommandService.handle(deleteTasksByMemberId);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/tasks/next")
  @Operation(summary = "Get the next task by authenticated member", description = "Fetches the next task for the authenticated member.")
  public ResponseEntity<TaskResource> getNextTaskByMemberAuthenticated(
      @RequestHeader("X-Username") String username,
      @RequestHeader("Authorization") String authorizationHeader) {
    var getMemberByUsernameQuery = new GetMemberByUsernameQuery(username, authorizationHeader);
    var member = this.memberQueryService.handle(getMemberByUsernameQuery);
    if(member.isEmpty()) return ResponseEntity.notFound().build();

    var memberId = member.get().member().id();
    var getAllTasksByMemberId = new GetAllTasksByMemberId(memberId);
    var tasks = taskQueryService.handle(getAllTasksByMemberId);
    if (tasks.isEmpty()) return ResponseEntity.notFound().build();

    var inProgressTasks = tasks.stream()
        .filter(task -> task.getStatus().equals(TaskStatus.IN_PROGRESS))
        .collect(Collectors.toList());

    var now = LocalDateTime.now(ZoneId.of("UTC"));
    var nextTask = inProgressTasks.stream()
        .filter(task -> {
          if (task.getDueDate() == null) {
            return false;
          }
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
    if (nextTask.isEmpty()) return ResponseEntity.notFound().build();

    var userResource = iamServiceClient.fetchUserByMemberId(memberId, authorizationHeader);
    if (userResource.isEmpty()) return ResponseEntity.notFound().build();

    var taskResource = TaskResourceFromEntityAssembler.toResourceFromEntity(nextTask.get(), userResource.get());
    return ResponseEntity.ok(taskResource);
  }
}
