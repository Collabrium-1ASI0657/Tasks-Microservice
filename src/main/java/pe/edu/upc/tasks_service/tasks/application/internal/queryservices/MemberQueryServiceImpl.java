package pe.edu.upc.tasks_service.tasks.application.internal.queryservices;

import org.springframework.stereotype.Service;
import pe.edu.upc.tasks_service.tasks.application.clients.iam.IamServiceClient;
import pe.edu.upc.tasks_service.tasks.application.clients.iam.resources.UserResource;
import pe.edu.upc.tasks_service.tasks.domain.model.aggregates.Member;
import pe.edu.upc.tasks_service.tasks.domain.model.queries.*;
import pe.edu.upc.tasks_service.tasks.domain.model.valueobjects.GroupId;
import pe.edu.upc.tasks_service.tasks.domain.services.MemberQueryService;
import pe.edu.upc.tasks_service.tasks.infrastructure.persistence.jpa.repositories.MemberRepository;

import java.util.List;
import java.util.Optional;

@Service
public class MemberQueryServiceImpl implements MemberQueryService {
  private final MemberRepository memberRepository;
  private final IamServiceClient  iamServiceClient;

  public MemberQueryServiceImpl(MemberRepository memberRepository,
                                IamServiceClient iamServiceClient) {
    this.memberRepository = memberRepository;
    this.iamServiceClient = iamServiceClient;
  }

  @Override
  public Optional<Member> handle(GetMemberByIdQuery query) {
    return memberRepository.findById(query.memberId());
  }

  @Override
  public Optional<UserResource> handle(GetMemberByUsernameQuery query) {
    var userOptional = iamServiceClient.fetchUserByUsername(query.username(), query.authorizationHeader());
    if (userOptional.isEmpty()) {
      return Optional.empty();
    }
    var user = userOptional.get();

    var primaryRoleOptional = user.roles().stream().findFirst();
    if (primaryRoleOptional.isEmpty()) {
      return Optional.empty();
    }

    var role = primaryRoleOptional.get();
    if (!role.equals("ROLE_MEMBER")) {
      return Optional.empty();
    }

    return userOptional;
  }

  @Override
  public List<Member> handle(GetAllMembersQuery query) {
    return this.memberRepository.findAll();
  }

  @Override
  public List<UserResource> handle(GetMembersByGroupIdQuery query) {
    var groupId = new GroupId(query.groupId());
    var members = memberRepository.findMembersByGroupId(groupId);

    // Por cada member, consultamos su información en el IAM service
    return members.stream()
        .map(member -> iamServiceClient.fetchUserByMemberId(
            member.getId(), query.authorizationHeader())
        )
        .filter(Optional::isPresent) // solo los que existan
        .map(Optional::get)
        .filter(user -> user.roles().stream()
            .anyMatch(role -> role.equals("ROLE_MEMBER"))
        )
        .toList();
  }

  @Override
  public Optional<UserResource> handle(GetMemberInfoByIdQuery query) {
    var memberOptional = iamServiceClient.fetchUserByMemberId(query.memberId(), query.authorizationHeader());
    if (memberOptional.isEmpty()) {
      return Optional.empty();
    }
    var user = memberOptional.get();

    var primaryRoleOptional = user.roles().stream().findFirst();
    if (primaryRoleOptional.isEmpty()) {
      return Optional.empty();
    }

    var role = primaryRoleOptional.get();
    if (!role.equals("ROLE_MEMBER")) {
      return Optional.empty();
    }

    return memberOptional;
  }
}
