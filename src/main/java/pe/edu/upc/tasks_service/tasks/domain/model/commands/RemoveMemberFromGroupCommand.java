package pe.edu.upc.tasks_service.tasks.domain.model.commands;

public record RemoveMemberFromGroupCommand(Long groupId, Long memberId) {
}
