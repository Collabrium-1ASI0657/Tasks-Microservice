package pe.edu.upc.tasks_service.tasks.application.clients.groups.resources;

public record GroupDetailsResource(Long id,
                                   String name,
                                   String imgUrl,
                                   String description,
                                   String code,
                                   Integer memberCount) {
}
