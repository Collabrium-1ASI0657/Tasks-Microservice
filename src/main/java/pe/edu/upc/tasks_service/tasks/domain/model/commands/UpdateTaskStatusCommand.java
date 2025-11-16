package pe.edu.upc.tasks_service.tasks.domain.model.commands;

public record UpdateTaskStatusCommand(Long taskId,
                                      String status) {
}
