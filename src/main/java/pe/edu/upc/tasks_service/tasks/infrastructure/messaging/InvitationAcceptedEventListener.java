package pe.edu.upc.tasks_service.tasks.infrastructure.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import pe.edu.upc.tasks_service.tasks.domain.model.commands.AddGroupToMemberCommand;
import pe.edu.upc.tasks_service.tasks.domain.model.events.InvitationAcceptedEvent;
import pe.edu.upc.tasks_service.tasks.domain.services.MemberCommandService;
import pe.edu.upc.tasks_service.tasks.infrastructure.config.RabbitMQConfig;

@Component
public class InvitationAcceptedEventListener {
  private final MemberCommandService memberCommandService;

  public InvitationAcceptedEventListener(MemberCommandService memberCommandService) {
    this.memberCommandService = memberCommandService;
  }

  @RabbitListener(queues = RabbitMQConfig.QUEUE_GROUP_ACCEPTED)
  public void handleInvitationAcceptedEvent(InvitationAcceptedEvent event){
    System.out.println("📥 [Tasks] Evento recibido: groupId=" + event.groupId() + ", memberId=" + event.memberId());

    Long groupId = event.groupId();
    Long memberId = event.memberId();

    // Crear el comando para asociar el grupo al miembro
    var command = new AddGroupToMemberCommand(groupId, memberId);

    memberCommandService.handle(command);
  }
}
