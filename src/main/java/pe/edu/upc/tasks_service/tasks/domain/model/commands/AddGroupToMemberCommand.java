package pe.edu.upc.tasks_service.tasks.domain.model.commands;

public record AddGroupToMemberCommand(Long groupId,
                                      Long memberId) {
}
