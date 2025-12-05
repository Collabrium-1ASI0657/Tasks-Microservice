package pe.edu.upc.tasks_service.tasks.application.internal.commandservices;

import org.springframework.stereotype.Service;
import pe.edu.upc.tasks_service.tasks.application.clients.groups.GroupsServiceClient;
import pe.edu.upc.tasks_service.tasks.domain.model.aggregates.Task;
import pe.edu.upc.tasks_service.tasks.domain.model.commands.*;
import pe.edu.upc.tasks_service.tasks.domain.model.valueobjects.GroupId;
import pe.edu.upc.tasks_service.tasks.domain.services.TaskCommandService;
import pe.edu.upc.tasks_service.tasks.infrastructure.messaging.GroupEventsPublisher;
import pe.edu.upc.tasks_service.tasks.infrastructure.persistence.jpa.repositories.MemberRepository;
import pe.edu.upc.tasks_service.tasks.infrastructure.persistence.jpa.repositories.TaskRepository;

import java.util.Optional;

@Service
public class TaskCommandServiceImpl implements TaskCommandService {
  private final TaskRepository taskRepository;
  private final MemberRepository memberRepository;
  private final GroupsServiceClient groupsServiceClient;
  private final GroupEventsPublisher groupEventsPublisher;

  public TaskCommandServiceImpl(TaskRepository taskRepository,
                                MemberRepository memberRepository,
                                GroupsServiceClient groupsServiceClient,
                                GroupEventsPublisher groupEventsPublisher) {
    this.taskRepository = taskRepository;
    this.memberRepository = memberRepository;
    this.groupsServiceClient = groupsServiceClient;
    this.groupEventsPublisher = groupEventsPublisher;
  }

  @Override
  public Optional<Task> handle(CreateTaskCommand command) {
    var task = new Task(command);
    var member = this.memberRepository.findById(command.memberId());
    if (member.isEmpty()) {
      throw new IllegalArgumentException("Member with id " + command.memberId() + " does not exist");
    }

    var groupId = member.get().getGroupId().value();
    if(groupId == null) {
      throw new IllegalArgumentException("Member with id " + command.memberId() + " does not belong to any group");
    }

    var group = this.groupsServiceClient.fetchGroupByGroupId(groupId);
    if (group.isEmpty()) {
      throw new IllegalArgumentException("Group with id " + groupId + " does not exist");
    }

    task.setMember(member.get());
    task.setGroupId(new GroupId(groupId));
    member.get().addTask(task);

    this.memberRepository.save(member.get());
    var createdTask = this.taskRepository.save(task);

    return Optional.of(createdTask);
  }

  @Override
  public Optional<Task> handle(UpdateTaskCommand command) {
    var taskOpt = this.taskRepository.findById(command.taskId());
    if (taskOpt.isEmpty()) {
      throw new IllegalArgumentException("Task with id " + command.taskId() + " does not exist");
    }

    var newMemberOpt = this.memberRepository.findById(command.memberId());
    if (newMemberOpt.isEmpty()) {
      throw new IllegalArgumentException("Member with id " + command.memberId() + " does not exist");
    }

    var task = taskOpt.get();
    var currentMember = task.getMember();
    var newMember = newMemberOpt.get();
    var groupId = newMember.getGroupId().value();
    if(groupId == null) {
      throw new IllegalArgumentException("Member with id " + command.memberId() + " does not belong to any group");
    }

    var group = this.groupsServiceClient.fetchGroupByGroupId(groupId);
    if (group.isEmpty()) {
      throw new IllegalArgumentException("Group with id " + groupId + " does not exist");
    }

    var newGroup = this.groupsServiceClient.fetchGroupByGroupId(newMember.getGroupId().value());
    if (newGroup.isEmpty()) {
      throw new IllegalArgumentException("Group with id " + groupId + " does not exist");
    }

    if (currentMember != null && !currentMember.equals(newMember)) {
      currentMember.removeTask(task);
      this.memberRepository.save(currentMember);
      newMember.addTask(task);
      task.setMember(newMember);
      task.setGroupId(new GroupId(newGroup.get().id()));
      this.memberRepository.save(newMember);
    } else if (currentMember == null) {
      newMember.addTask(task);
      task.setMember(newMember);
      task.setGroupId(new GroupId(newGroup.get().id()));
      this.memberRepository.save(newMember);
    }

    task.updateTask(command);

    try{
      var updatedTask = this.taskRepository.save(task);
      return Optional.of(updatedTask);
    } catch (Exception e){
      throw new IllegalArgumentException("Error updating task: " + e.getMessage());
    }
  }

  @Override
  public void handle(DeleteTaskCommand command) {
    var taskId = command.taskId();
    if(!taskRepository.existsById(taskId)) {
      throw new IllegalArgumentException("Task with id " + taskId + " does not exist");
    }
    try {
      var member = this.taskRepository.findById(taskId).get().getMember();
      if (member != null) {
        member.removeTask(this.taskRepository.findById(taskId).get());
        this.memberRepository.save(member);
      }
      this.taskRepository.deleteById(taskId);
    } catch (Exception e) {
      throw new IllegalArgumentException("Error deleting task: " + e.getMessage());
    }
  }

  @Override
  public Optional<Task> handle(UpdateTaskStatusCommand command) {
    var taskId = command.taskId();
    if(!taskRepository.existsById(taskId)) {
      throw new IllegalArgumentException("Task with id " + taskId + " does not exist");
    }

    var taskToUpdate = this.taskRepository.findById(taskId).get();

    try{
      taskToUpdate.updateStatus(command);
      var updatedTask = this.taskRepository.save(taskToUpdate);
      return Optional.of(updatedTask);
    } catch (Exception e){
      throw new IllegalArgumentException("Error updating task status: " + e.getMessage());
    }
  }

  @Override
  public void handle(DeleteTasksByMemberId command) {
    var memberId = command.memberId();
    if(!this.memberRepository.existsById(memberId)) {
      throw new IllegalArgumentException("Member with id " + memberId + " does not exist");
    }
    try {
      var member = memberRepository.findById(memberId)
          .orElseThrow(() -> new IllegalArgumentException("Member not found"));

      // 1. Eliminar sus tasks
      var tasks = this.taskRepository.findByMember_Id(memberId);
      if (!tasks.isEmpty()) {
        for (var task : tasks) {
          member.removeTask(task);
          this.taskRepository.delete(task);
        }
      }

      // 2. Romper relación con el grupo
      member.setGroupId(null);
      memberRepository.save(member);

      // 3. Publicar evento group.member.left
      groupEventsPublisher.publishMemberLeft(
          command.memberId(),
          command.groupId()
      );

    } catch (Exception e) {
      throw new IllegalArgumentException("Error deleting tasks for member: " + e.getMessage());
    }
  }
}
