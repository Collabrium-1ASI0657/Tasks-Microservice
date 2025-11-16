package pe.edu.upc.tasks_service.tasks.infrastructure.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import pe.edu.upc.tasks_service.tasks.application.internal.commandservices.MemberCommandServiceImpl;
import pe.edu.upc.tasks_service.tasks.domain.model.commands.RemoveMemberFromGroupCommand;
import pe.edu.upc.tasks_service.tasks.domain.model.events.MemberRemovedEvent;
import pe.edu.upc.tasks_service.tasks.infrastructure.config.RabbitMQConfig;

@Component
public class MemberRemovedEventListener {
  private final MemberCommandServiceImpl memberCommandService;

  public MemberRemovedEventListener(MemberCommandServiceImpl memberCommandService) {
    this.memberCommandService = memberCommandService;
  }

  @RabbitListener(queues = RabbitMQConfig.QUEUE_MEMBER_REMOVED)
  public void handleMemberRemovedEvent(MemberRemovedEvent event) {
    var command = new RemoveMemberFromGroupCommand(event.groupId(), event.memberId());
    memberCommandService.handle(command);
  }
}
