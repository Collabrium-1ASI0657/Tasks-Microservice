package pe.edu.upc.tasks_service.tasks.infrastructure.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import pe.edu.upc.tasks_service.tasks.domain.model.events.MemberLeftEvent;
import pe.edu.upc.tasks_service.tasks.infrastructure.config.RabbitMQConfig;

@Service
public class GroupEventsPublisher {
  private final RabbitTemplate rabbitTemplate;

  public GroupEventsPublisher(RabbitTemplate rabbitTemplate) {
    this.rabbitTemplate = rabbitTemplate;
  }

  public void publishMemberLeft(Long memberId, Long groupId) {
    var event = new MemberLeftEvent(memberId, groupId);

    rabbitTemplate.convertAndSend(
        RabbitMQConfig.TASKS_EXCHANGE_NAME,
        RabbitMQConfig.ROUTING_KEY_MEMBER_LEFT,
        event
    );
  }
}
