package pe.edu.upc.tasks_service.tasks.infrastructure.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import pe.edu.upc.tasks_service.tasks.domain.model.commands.CreateMemberCommand;
import pe.edu.upc.tasks_service.tasks.domain.model.events.MemberCreatedEvent;
import pe.edu.upc.tasks_service.tasks.domain.services.MemberCommandService;
import pe.edu.upc.tasks_service.tasks.infrastructure.config.RabbitMQConfig;

@Component
public class MemberCreatedEventListener {
  private final MemberCommandService memberCommandService;

  public MemberCreatedEventListener(MemberCommandService memberCommandService) {
    this.memberCommandService = memberCommandService;
  }

  @RabbitListener(queues = RabbitMQConfig.QUEUE_MEMBER_CREATED)
  public void handleMemberCreatedEvent(MemberCreatedEvent event) {
    Long memberUserId = event.userId();

    var createMemberCommand = new CreateMemberCommand(memberUserId);
    memberCommandService.handle(createMemberCommand);
  }
}
