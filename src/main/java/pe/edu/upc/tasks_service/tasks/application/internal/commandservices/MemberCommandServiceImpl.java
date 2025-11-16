package pe.edu.upc.tasks_service.tasks.application.internal.commandservices;

import org.springframework.stereotype.Service;
import pe.edu.upc.tasks_service.tasks.domain.model.aggregates.Member;
import pe.edu.upc.tasks_service.tasks.domain.model.commands.AddGroupToMemberCommand;
import pe.edu.upc.tasks_service.tasks.domain.model.commands.CreateMemberCommand;
import pe.edu.upc.tasks_service.tasks.domain.model.commands.RemoveMemberFromGroupCommand;
import pe.edu.upc.tasks_service.tasks.domain.model.valueobjects.GroupId;
import pe.edu.upc.tasks_service.tasks.domain.services.MemberCommandService;
import pe.edu.upc.tasks_service.tasks.infrastructure.messaging.IamEventPublisher;
import pe.edu.upc.tasks_service.tasks.infrastructure.persistence.jpa.repositories.MemberRepository;

import java.util.Optional;

@Service
public class MemberCommandServiceImpl implements MemberCommandService {
  private final MemberRepository memberRepository;
  private final IamEventPublisher iamEventPublisher;

  public MemberCommandServiceImpl(MemberRepository memberRepository,
                                  IamEventPublisher iamEventPublisher) {
    this.memberRepository = memberRepository;
    this.iamEventPublisher = iamEventPublisher;
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
  public Optional<Member> handle(RemoveMemberFromGroupCommand command) {
    var member = memberRepository.findById(command.memberId());
    if (member.isEmpty()){ throw new RuntimeException("Member not found"); }
    member.get().setGroupId(null);
    memberRepository.save(member.get());
    return member;
  }
}
