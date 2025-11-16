package pe.edu.upc.tasks_service.tasks.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.tasks_service.tasks.application.clients.groups.GroupsServiceClient;
import pe.edu.upc.tasks_service.tasks.domain.model.queries.GetMemberByIdQuery;
import pe.edu.upc.tasks_service.tasks.domain.model.queries.GetMemberByUsernameQuery;
import pe.edu.upc.tasks_service.tasks.domain.model.queries.GetMemberInfoByIdQuery;
import pe.edu.upc.tasks_service.tasks.domain.model.queries.GetMembersByGroupIdQuery;
import pe.edu.upc.tasks_service.tasks.domain.services.MemberQueryService;
import pe.edu.upc.tasks_service.tasks.interfaces.rest.resources.ExtendedGroupResource;
import pe.edu.upc.tasks_service.tasks.interfaces.rest.resources.MemberOnlyResource;
import pe.edu.upc.tasks_service.tasks.interfaces.rest.resources.MemberResource;
import pe.edu.upc.tasks_service.tasks.interfaces.rest.transform.ExtendedGroupResourceFromEntityAssembler;
import pe.edu.upc.tasks_service.tasks.interfaces.rest.transform.MemberResourceFromEntityAssembler;

import java.util.List;

@RestController
@RequestMapping("/api/v1/member")
@Tag(name = "Member", description = "Member API")
@ApiResponse(responseCode = "201", description = "Member created")
public class MemberController {
  private final MemberQueryService memberQueryService;
  private final GroupsServiceClient groupsServiceClient;

  public MemberController(MemberQueryService memberQueryService,
                          GroupsServiceClient groupsServiceClient) {
    this.memberQueryService = memberQueryService;
    this.groupsServiceClient = groupsServiceClient;
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
}
