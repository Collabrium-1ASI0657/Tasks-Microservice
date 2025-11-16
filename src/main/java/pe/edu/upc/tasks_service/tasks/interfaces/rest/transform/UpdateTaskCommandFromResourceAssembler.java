package pe.edu.upc.tasks_service.tasks.interfaces.rest.transform;

import pe.edu.upc.tasks_service.tasks.domain.model.commands.UpdateTaskCommand;
import pe.edu.upc.tasks_service.tasks.interfaces.rest.resources.UpdateTaskResource;

public class UpdateTaskCommandFromResourceAssembler {
  public static UpdateTaskCommand toCommandFromResource(UpdateTaskResource command, Long taskId) {
    return new UpdateTaskCommand(
        taskId,
        command.title(),
        command.description(),
        command.dueDate(),
        command.memberId()
    );

  }
}