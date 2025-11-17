package pe.edu.upc.tasks_service.tasks.domain.model.aggregates;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pe.edu.upc.tasks_service.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import pe.edu.upc.tasks_service.tasks.domain.model.commands.CreateMemberCommand;
import pe.edu.upc.tasks_service.tasks.domain.model.valueobjects.GroupId;

import java.util.List;

@Getter
@Setter
@Entity
@AttributeOverrides({
    @AttributeOverride(name = "groupId.value", column = @Column(name = "group_id"))
})
public class Member extends AuditableAbstractAggregateRoot<Member> {
  @OneToMany(mappedBy = "member", fetch = FetchType.EAGER)
  private List<Task> tasks;

  @Embedded
  private GroupId groupId;

  public Member() {}

  public Member(CreateMemberCommand command) {}

  public void addTask(Task task) {
    this.tasks.add(task);
    task.setMember(this);
  }

  public void removeTask(Task task) {
    this.tasks.remove(task);
    task.setMember(null);
  }

  public void clearTasks() {
    if (this.tasks != null) {
      for (Task task : this.tasks) {
        task.setMember(null); // romper relación bidireccional
      }
      this.tasks.clear(); // vaciar la lista
    }
  }
}
