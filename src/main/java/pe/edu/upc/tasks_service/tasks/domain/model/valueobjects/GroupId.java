package pe.edu.upc.tasks_service.tasks.domain.model.valueobjects;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;

@Embeddable
public record GroupId(@NotNull Long value) {
  public GroupId {
    if (value == null || value <= 0) {
      throw new IllegalArgumentException("Group ID cannot be null or negative.");
    }
  }
}
