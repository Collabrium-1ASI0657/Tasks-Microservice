package pe.edu.upc.tasks_service.tasks.interfaces.rest.transform;

import pe.edu.upc.tasks_service.tasks.application.clients.groups.resources.GroupDetailsResource;
import pe.edu.upc.tasks_service.tasks.application.clients.iam.resources.UserResource;
import pe.edu.upc.tasks_service.tasks.interfaces.rest.resources.ExtendedGroupResource;
import pe.edu.upc.tasks_service.tasks.interfaces.rest.resources.MemberResource;

import java.util.List;

public class ExtendedGroupResourceFromEntityAssembler {
  public static ExtendedGroupResource toResourceFromEntity(GroupDetailsResource group, List<UserResource> members) {

    // Transformar la lista de UserResource a MemberResource
    List<MemberResource> memberResources = members.stream()
        .map(user -> new MemberResource(
            user.member() != null ? user.member().id() : null, // Si el UserResource trae referencia a su Member
            user.username(),
            user.name(),
            user.surname(),
            user.imgUrl(),
            user.email(),
            user.member() != null ? user.member().groupId() : null
        ))
        .toList();

    // Construir el ExtendedGroupResource final
    return new ExtendedGroupResource(
        group.id(),
        group.name(),
        group.imgUrl(),
        group.description(),
        group.code(),
        memberResources
    );
  }
}
