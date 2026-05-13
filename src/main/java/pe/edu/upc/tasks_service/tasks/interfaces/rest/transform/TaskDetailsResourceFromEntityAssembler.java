package pe.edu.upc.tasks_service.tasks.interfaces.rest.transform;

import pe.edu.upc.tasks_service.tasks.domain.model.aggregates.Task;
import pe.edu.upc.tasks_service.tasks.interfaces.rest.resources.TaskDetailsResource;

public class TaskDetailsResourceFromEntityAssembler {
    public static TaskDetailsResource toResourceFromEntity(Task entity) {
        return new TaskDetailsResource(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getDueDate().toString(),
                entity.getCreatedAt().toString(),
                entity.getUpdatedAt().toString(),
                entity.getStatus().toString(),
                entity.getMember().getId(),
                entity.getGroupId().value()
        );
    }
}
