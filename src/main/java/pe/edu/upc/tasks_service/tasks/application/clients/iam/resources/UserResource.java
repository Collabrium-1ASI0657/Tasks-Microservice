package pe.edu.upc.tasks_service.tasks.application.clients.iam.resources;

import java.util.List;

public record UserResource(Long id,
                           String username,
                           String name,
                           String surname,
                           String imgUrl,
                           String email,
                           MemberFromUserResource member,
                           List<String> roles) {
}
