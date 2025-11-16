package pe.edu.upc.tasks_service.tasks.domain.model.queries;

public record GetMemberInfoByIdQuery(Long memberId, String authorizationHeader) {
}
