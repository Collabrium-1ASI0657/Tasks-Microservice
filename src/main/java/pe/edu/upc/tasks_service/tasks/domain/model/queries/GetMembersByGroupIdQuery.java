package pe.edu.upc.tasks_service.tasks.domain.model.queries;

public record GetMembersByGroupIdQuery(Long groupId, String authorizationHeader) {
}
