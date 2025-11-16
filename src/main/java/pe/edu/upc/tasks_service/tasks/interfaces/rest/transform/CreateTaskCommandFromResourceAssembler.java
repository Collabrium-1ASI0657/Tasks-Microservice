package pe.edu.upc.tasks_service.tasks.interfaces.rest.transform;

import pe.edu.upc.tasks_service.tasks.domain.model.commands.CreateTaskCommand;
import pe.edu.upc.tasks_service.tasks.interfaces.rest.resources.CreateTaskResource;

public class CreateTaskCommandFromResourceAssembler {
  public static CreateTaskCommand toCommandFromResource(CreateTaskResource resource, Long memberId) {
    return new CreateTaskCommand(
        resource.title(),
        resource.description(),
        resource.dueDate(),
        memberId
    );
  }
}