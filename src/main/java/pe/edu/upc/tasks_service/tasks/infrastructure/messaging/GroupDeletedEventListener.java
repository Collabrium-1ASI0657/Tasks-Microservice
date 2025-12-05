package pe.edu.upc.tasks_service.tasks.infrastructure.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import pe.edu.upc.tasks_service.tasks.domain.model.commands.DeleteMembersByGroupIdCommand;
import pe.edu.upc.tasks_service.tasks.domain.model.commands.DeleteTasksByGroupIdCommand;
import pe.edu.upc.tasks_service.tasks.domain.model.events.GroupDeletedEvent;
import pe.edu.upc.tasks_service.tasks.domain.services.MemberCommandService;
import pe.edu.upc.tasks_service.tasks.domain.services.TaskCommandService;
import pe.edu.upc.tasks_service.tasks.infrastructure.config.RabbitMQConfig;

@Component
public class GroupDeletedEventListener {
  private final MemberCommandService memberCommandService;
  private final TaskCommandService taskCommandService;

  public GroupDeletedEventListener(MemberCommandService memberCommandService,
                                   TaskCommandService taskCommandService) {
    this.memberCommandService = memberCommandService;
    this.taskCommandService = taskCommandService;
  }

  @RabbitListener(queues = RabbitMQConfig.QUEUE_GROUP_DELETED)
  public void handleGroupDeletedEvent(GroupDeletedEvent event) {
    Long groupId = event.groupId();
    System.out.println("Recibido evento group.deleted para groupId = " + groupId);

    var deleteMembersByGroupIdCommand = new DeleteMembersByGroupIdCommand(groupId);
    var deleteTasksByGroupIdCommand = new DeleteTasksByGroupIdCommand(groupId);

    taskCommandService.handle(deleteTasksByGroupIdCommand);
    memberCommandService.handle(deleteMembersByGroupIdCommand);
  }
}
