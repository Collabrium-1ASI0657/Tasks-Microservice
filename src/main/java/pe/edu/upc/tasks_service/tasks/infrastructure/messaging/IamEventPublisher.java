package pe.edu.upc.tasks_service.tasks.infrastructure.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import pe.edu.upc.tasks_service.tasks.domain.model.events.MemberCreatedSuccessfullyEvent;
import pe.edu.upc.tasks_service.tasks.infrastructure.config.RabbitMQConfig;

@Service
public class IamEventPublisher {
  private final RabbitTemplate rabbitTemplate;

  public IamEventPublisher(RabbitTemplate rabbitTemplate) {
    this.rabbitTemplate = rabbitTemplate;
  }

  public void publishMemberCreatedSuccessfully(Long userId,
                                               Long memberId) {
    var event = new MemberCreatedSuccessfullyEvent(userId, memberId);

    rabbitTemplate.convertAndSend(
        RabbitMQConfig.EXCHANGE_NAME,
        RabbitMQConfig.ROUTING_KEY_MEMBER_CREATED_SUCCESS,
        event
    );
  }
}
