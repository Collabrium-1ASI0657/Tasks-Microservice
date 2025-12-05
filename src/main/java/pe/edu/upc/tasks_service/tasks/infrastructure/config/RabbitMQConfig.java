package pe.edu.upc.tasks_service.tasks.infrastructure.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
  // -------------------------------------------------------
  // EXCHANGE GENERAL (usado por IAM)
  // -------------------------------------------------------
  public static final String EXCHANGE_NAME = "iam-events-exchange";

  // -------------------------------------------------------
  // EVENTOS QUE TASKS CONSUME (desde IAM)
  // -------------------------------------------------------
  public static final String ROUTING_KEY_MEMBER_CREATED = "member.created";
  public static final String QUEUE_MEMBER_CREATED = "tasks.member-created";

  // -------------------------------------------------------
  // EVENTOS QUE TASKS CONSUME (desde GROUPS)
  // -------------------------------------------------------
  public static final String TASKS_EXCHANGE_NAME = "tasks-events-exchange";
  public static final String ROUTING_KEY_GROUP_ACCEPTED = "group.accepted";
  public static final String QUEUE_GROUP_ACCEPTED = "tasks.group-accepted";

  // Evento: un miembro decide salir de un grupo
  public static final String ROUTING_KEY_MEMBER_LEFT = "group.member.left";
  public static final String QUEUE_MEMBER_LEFT = "tasks.member-left";

  public static final String ROUTING_KEY_MEMBER_REMOVED = "group.member.removed";
  public static final String QUEUE_MEMBER_REMOVED = "tasks.member-removed";

  // -------------------------------------------------------
  // EVENTOS QUE TASKS PRODUCE (respuesta a IAM)
  // -------------------------------------------------------
  public static final String ROUTING_KEY_MEMBER_CREATED_SUCCESS = "member.created.success";

  // -------------------------------------------------------
  // BEANS DE INFRAESTRUCTURA
  // -------------------------------------------------------
  @Bean
  public TopicExchange exchange() {
    return new TopicExchange(EXCHANGE_NAME);
  }

  @Bean
  public TopicExchange tasksExchange() {
    return new TopicExchange(TASKS_EXCHANGE_NAME);
  }

  @Bean
  public MessageConverter jsonMessageConverter() {
    return new Jackson2JsonMessageConverter();
  }

  // -------------------------------------------------------
  // COLAS Y BINDINGS: IAM → TASKS
  // -------------------------------------------------------
  @Bean
  public Queue memberCreatedQueue() {
    return new Queue(QUEUE_MEMBER_CREATED, true);
  }

  @Bean
  public Binding memberCreatedBinding(Queue memberCreatedQueue, TopicExchange exchange) {
    return BindingBuilder.bind(memberCreatedQueue)
        .to(exchange)
        .with(ROUTING_KEY_MEMBER_CREATED);
  }

  // -------------------------------------------------------
  // COLAS Y BINDINGS: GROUPS → TASKS
  // -------------------------------------------------------
  @Bean
  public Queue groupAcceptedQueue() {
    return new Queue(QUEUE_GROUP_ACCEPTED, true);
  }

  @Bean
  public Binding groupAcceptedBinding(Queue groupAcceptedQueue, TopicExchange tasksExchange) {
    return BindingBuilder.bind(groupAcceptedQueue)
        .to(tasksExchange)
        .with(ROUTING_KEY_GROUP_ACCEPTED);
  }

  @Bean
  public Queue memberRemovedQueue() {
    return new Queue(QUEUE_MEMBER_REMOVED, true);
  }

  @Bean
  public Binding memberRemovedBinding(Queue memberRemovedQueue, TopicExchange tasksExchange) {
    return BindingBuilder.bind(memberRemovedQueue)
        .to(tasksExchange)
        .with(ROUTING_KEY_MEMBER_REMOVED);
  }

  @Bean
  public Queue memberLeftQueue() {
    return new Queue(QUEUE_MEMBER_LEFT, true);
  }

  @Bean
  public Binding memberLeftBinding(Queue memberLeftQueue, TopicExchange tasksExchange) {
    return BindingBuilder.bind(memberLeftQueue)
        .to(tasksExchange)
        .with(ROUTING_KEY_MEMBER_LEFT);
  }
}
