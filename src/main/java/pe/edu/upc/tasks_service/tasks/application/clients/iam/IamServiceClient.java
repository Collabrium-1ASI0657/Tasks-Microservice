package pe.edu.upc.tasks_service.tasks.application.clients.iam;

import pe.edu.upc.tasks_service.tasks.application.clients.iam.resources.UserResource;

import java.util.Optional;

public interface IamServiceClient {
  Optional<UserResource> fetchUserByUsername(String username, String authorizationHeader);

  Optional<UserResource> fetchUserByMemberId(Long memberId, String authorizationHeader);
}