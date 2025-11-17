package pe.edu.upc.tasks_service.tasks.interfaces.rest.transform;

import pe.edu.upc.tasks_service.tasks.application.clients.iam.resources.UserResource;
import pe.edu.upc.tasks_service.tasks.interfaces.rest.resources.TaskMemberResource;

public class TaskMemberResourceFromEntityAssembler {
  public static TaskMemberResource toResourceFromEntity(UserResource user) {
    return new TaskMemberResource(
        user.member().id(),
        user.name(),
        user.surname(),
        user.imgUrl()
    );
  }
}
