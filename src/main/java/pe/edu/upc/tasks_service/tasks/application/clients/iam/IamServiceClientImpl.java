package pe.edu.upc.tasks_service.tasks.application.clients.iam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import pe.edu.upc.tasks_service.tasks.application.clients.iam.resources.UserResource;

import java.util.Optional;

@Service
public class IamServiceClientImpl implements IamServiceClient {
  private static final Logger log = LoggerFactory.getLogger(IamServiceClientImpl.class);
  private final WebClient webClient;

  public IamServiceClientImpl(@Qualifier("loadBalancedWebClientBuilder") WebClient.Builder webClientBuilder) {
    this.webClient = webClientBuilder
        .baseUrl("http://iam-service/api/v1")
        .build();
  }

  @Override
  public Optional<UserResource> fetchUserByUsername(String username, String authorizationHeader) {
    log.info("Intentando obtener usuario con username: {}", username);
    try {
      var request = webClient.get()
          .uri(uriBuilder -> uriBuilder
              .path("/users")
              .queryParam("username", username)
              .build());

      if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
        request = request.header("Authorization", authorizationHeader);
      }

      UserResource resource = request
          .retrieve()
          .bodyToMono(UserResource.class)
          .block();

      if (resource != null) {
        log.info("Usuario encontrado para {}. ID: {}", username, resource.id());
      } else {
        log.warn("El WebClient devolvió un recurso nulo para el username: {}", username);
      }

      return Optional.ofNullable(resource);

    } catch (WebClientResponseException e) {
      if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
        log.warn("Usuario no encontrado en IAM Service (404) para username: {}", username);
        return Optional.empty();
      }
      log.error("Error WebClient al buscar usuario {}. Status: {}. Body: {}", username, e.getStatusCode(), e.getResponseBodyAsString(), e);
      return Optional.empty();
    } catch (Exception e) {
      log.error("Error general al buscar usuario {} en IAM Service.", username, e);
      return Optional.empty();
    }
  }

  @Override
  public Optional<UserResource> fetchUserByMemberId(Long memberId, String authorizationHeader) {
    try {
      var request = webClient.get()
          .uri(uriBuilder -> uriBuilder
              .path("/users")
              .queryParam("memberId", memberId)
              .build());

      if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
        request = request.header("Authorization", authorizationHeader);
      }

      UserResource resource = request
          .retrieve()
          .bodyToMono(UserResource.class)
          .block();

      if (resource != null) {
        log.info("Usuario encontrado para memberId: {}. ID: {}", memberId, resource.id());
      } else {
        log.warn("El WebClient devolvió un recurso nulo para el memberId: {}", memberId);
      }

      return Optional.ofNullable(resource);

    } catch (WebClientResponseException e) {
      if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
        log.warn("Usuario no encontrado en IAM Service (404) para memberId: {}", memberId); // ✅ Caso de Optional.empty()
        return Optional.empty();
      }
      log.error("Error WebClient al buscar usuario con memberId {}. Status: {}. Body: {}", memberId, e.getStatusCode(), e.getResponseBodyAsString(), e);
      return Optional.empty();
    } catch (Exception e) {
      log.error("Error general al buscar usuario con memberId {} en IAM Service.", memberId, e); // ✅ Fallas de red/timeout
      return Optional.empty();
    }
  }
}
