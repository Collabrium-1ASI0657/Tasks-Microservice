package pe.edu.upc.tasks_service.tasks.domain.model.events;

public record MemberLeftEvent(Long memberId,
                              Long groupId) {
}
