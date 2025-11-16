package pe.edu.upc.tasks_service.tasks.interfaces.rest.transform;

import pe.edu.upc.tasks_service.tasks.application.clients.iam.resources.UserResource;
import pe.edu.upc.tasks_service.tasks.domain.model.aggregates.Member;
import pe.edu.upc.tasks_service.tasks.interfaces.rest.resources.MemberOnlyResource;
import pe.edu.upc.tasks_service.tasks.interfaces.rest.resources.MemberResource;

public class MemberResourceFromEntityAssembler {
  public static MemberOnlyResource toResourceFromEntity(Member entity) {

    Long groupIdValue = (entity.getGroupId() != null && entity.getGroupId().value() != null)
        ? entity.getGroupId().value()
        : 0L;

    return new MemberOnlyResource(
        entity.getId(),
        groupIdValue);
  }

  public static MemberResource toResourceFromUserResource(UserResource resource, Long memberId) {
    return new MemberResource(
        memberId,
        resource.username(),
        resource.name(),
        resource.surname(),
        resource.imgUrl(),
        resource.email(),
        resource.member().groupId());
  }
}
