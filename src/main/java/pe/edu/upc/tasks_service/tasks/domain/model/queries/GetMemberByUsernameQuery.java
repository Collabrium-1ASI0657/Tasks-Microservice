package pe.edu.upc.tasks_service.tasks.domain.model.queries;

public record GetMemberByUsernameQuery(String username, String authorizationHeader) {
}
