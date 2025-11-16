package pe.edu.upc.tasks_service.tasks.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upc.tasks_service.tasks.domain.model.aggregates.Member;
import pe.edu.upc.tasks_service.tasks.domain.model.valueobjects.GroupId;

import java.util.List;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
  List<Member> findMembersByGroupId(GroupId groupId);
}