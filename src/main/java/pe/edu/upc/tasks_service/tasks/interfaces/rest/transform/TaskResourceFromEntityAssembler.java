package pe.edu.upc.tasks_service.tasks.interfaces.rest.transform;

import pe.edu.upc.tasks_service.tasks.application.clients.iam.resources.UserResource;
import pe.edu.upc.tasks_service.tasks.domain.model.aggregates.Task;
import pe.edu.upc.tasks_service.tasks.interfaces.rest.resources.TaskResource;

public class TaskResourceFromEntityAssembler {
  public static TaskResource toResourceFromEntity(Task entity, UserResource user) {
    return new TaskResource(
        entity.getId(),
        entity.getTitle(),
        entity.getDescription(),
        entity.getDueDate().toString(),
        entity.getCreatedAt().toString(),
        entity.getUpdatedAt().toString(),
        entity.getStatus().toString(),
        entity.getTimesRearranged(),
        entity.getTimePassed(),
        TaskMemberResourceFromEntityAssembler.toResourceFromEntity(user),
        entity.getGroupId().value()
    );
  }
}
