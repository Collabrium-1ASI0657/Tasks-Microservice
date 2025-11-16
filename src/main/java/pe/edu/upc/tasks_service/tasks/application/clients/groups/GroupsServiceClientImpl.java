package pe.edu.upc.tasks_service.tasks.application.clients.groups;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import pe.edu.upc.tasks_service.tasks.application.clients.groups.resources.GroupDetailsResource;

import java.util.Optional;

@Service
public class GroupsServiceClientImpl implements GroupsServiceClient{
  private final WebClient webClient;

  public GroupsServiceClientImpl(@Qualifier("loadBalancedWebClientBuilder") WebClient.Builder webClientBuilder) {
    this.webClient = webClientBuilder
        .baseUrl("http://groups-service/api/v1")
        .build();
  }

  @Override
  public Optional<GroupDetailsResource> fetchGroupByGroupId(Long groupId) {
    try {
      var request = webClient.get()
          .uri(uriBuilder -> uriBuilder
              .path("/groups/{groupId}")
              .build(groupId));

      GroupDetailsResource groupDetailsResource = request
          .retrieve()
          .bodyToMono(GroupDetailsResource.class)
          .block();


      return Optional.ofNullable(groupDetailsResource);

    } catch (WebClientResponseException e) {
      if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
        return Optional.empty();
      }
    }
    return Optional.empty();
  }
}
