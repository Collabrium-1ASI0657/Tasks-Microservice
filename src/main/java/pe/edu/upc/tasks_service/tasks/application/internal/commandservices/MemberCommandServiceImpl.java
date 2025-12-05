package pe.edu.upc.tasks_service.tasks.application.internal.commandservices;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import pe.edu.upc.tasks_service.tasks.domain.model.aggregates.Member;
import pe.edu.upc.tasks_service.tasks.domain.model.commands.AddGroupToMemberCommand;
import pe.edu.upc.tasks_service.tasks.domain.model.commands.CreateMemberCommand;
import pe.edu.upc.tasks_service.tasks.domain.model.commands.RemoveMemberFromGroupCommand;
import pe.edu.upc.tasks_service.tasks.domain.model.valueobjects.GroupId;
import pe.edu.upc.tasks_service.tasks.domain.services.MemberCommandService;
import pe.edu.upc.tasks_service.tasks.infrastructure.messaging.IamEventPublisher;
import pe.edu.upc.tasks_service.tasks.infrastructure.persistence.jpa.repositories.MemberRepository;
import pe.edu.upc.tasks_service.tasks.infrastructure.persistence.jpa.repositories.TaskRepository;

import java.util.Optional;

@Service
public class MemberCommandServiceImpl implements MemberCommandService {
  private final MemberRepository memberRepository;
  private final IamEventPublisher iamEventPublisher;
  private final TaskRepository taskRepository;

  public MemberCommandServiceImpl(MemberRepository memberRepository,
                                  IamEventPublisher iamEventPublisher,
                                  TaskRepository taskRepository) {
    this.memberRepository = memberRepository;
    this.iamEventPublisher = iamEventPublisher;
    this.taskRepository = taskRepository;
  }

  @Override
  public Optional<Member> handle(CreateMemberCommand command) {
    var member = new Member(command);
    memberRepository.save(member);
    iamEventPublisher.publishMemberCreatedSuccessfully(
        command.memberUserId(),
        member.getId());
    return Optional.of(member);
  }

  @Override
  public Optional<Member> handle(AddGroupToMemberCommand command) {
    var member = memberRepository.findById(command.memberId());
    if (member.isEmpty()){ throw new RuntimeException("Member not found"); }

    member.get().setGroupId(new GroupId(command.groupId()));
    var updatedMember = memberRepository.save(member.get());

    return Optional.of(updatedMember);
  }

  @Override
  @Transactional
  public Optional<Member> handle(RemoveMemberFromGroupCommand command) {
    var memberId = command.memberId();
    if(!this.memberRepository.existsById(memberId)) {
      throw new IllegalArgumentException("Member with id " + memberId + " does not exist");
    }
    try {
      var member = memberRepository.findById(memberId)
          .orElseThrow(() -> new IllegalArgumentException("Member not found"));

      // 1. Eliminar sus tasks
      taskRepository.deleteAllByMember_Id(memberId);

      // 2. Romper relación con el grupo
      member.setGroupId(null);
      memberRepository.save(member);

      return Optional.of(member);
    } catch (Exception e) {
      throw new IllegalArgumentException("Error deleting tasks for member: " + e.getMessage());
    }
  }
}
