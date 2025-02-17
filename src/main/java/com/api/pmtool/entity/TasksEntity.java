package com.api.pmtool.entity;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Index;

import org.hibernate.annotations.GenericGenerator;

import com.api.pmtool.enums.Priority;
import com.api.pmtool.enums.Status;
import com.api.pmtool.enums.TaskType;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "tasks",
indexes = {
    @Index(name = "idx_tasks_demand_id", columnList = "demand_id"),
    @Index(name = "idx_tasks_developer_id", columnList = "assigned_to_id"),
})
public class TasksEntity extends Auditable<UUID> {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(columnDefinition = "UUID")
    private UUID id;

    private String taskName;

    @Enumerated(EnumType.STRING)
    private TaskType taskType; 

    @Enumerated(EnumType.STRING)
    private Status workProgress = Status.NOT_STARTED; // Default value

    private int developmentInPercentage;;

    @Temporal(TemporalType.DATE)
    private Date dueDate; // Task due date


    @Temporal(TemporalType.DATE)
    private Date newDueDate;

    private int dueDateChangeCount = 0;

    @Temporal(TemporalType.DATE)
    private Date completeDate; // Task completion date

    @Enumerated(EnumType.STRING)
    private Priority priority; 

    @ManyToOne
    @JoinColumn(name = "demand_id", nullable = false)
   @JsonBackReference
    private Demand demand; // Task belongs to a demand

/*     @ManyToOne
    @JoinColumn(name = "developer_id", nullable = true)
    private User assignedDeveloper; // Assigned to a Developer role user */
    @ManyToOne
    @JoinColumn(name = "assigned_to_id")
    private User assignedTo;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Comments> comments = new ArrayList<>();

    
    public void extendDueDate(Date newDueDate) {
        this.newDueDate = newDueDate;
        this.dueDateChangeCount++;
    }
}
