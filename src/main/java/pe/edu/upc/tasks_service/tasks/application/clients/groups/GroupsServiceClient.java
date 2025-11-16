package pe.edu.upc.tasks_service.tasks.application.clients.groups;

import pe.edu.upc.tasks_service.tasks.application.clients.groups.resources.GroupDetailsResource;

import java.util.Optional;

public interface GroupsServiceClient {
  Optional<GroupDetailsResource> fetchGroupByGroupId(Long groupId);
}
