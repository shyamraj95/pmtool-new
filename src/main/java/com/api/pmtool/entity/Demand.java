package com.api.pmtool.entity;
import com.api.pmtool.enums.DemandTypes;
import com.api.pmtool.enums.Priority;
import com.api.pmtool.enums.Status;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Index;

import java.time.LocalDate;
import org.hibernate.annotations.GenericGenerator;


@Getter
@Setter
@Entity
@Table(name = "demands",
indexes = {
    @Index(name = "idx_demand_project_id", columnList = "project_id"),
    @Index(name = "idx_demand_assigned_to", columnList = "assigned_to_user_id"),
    @Index(name = "idx_demand_tech_lead", columnList = "tech_lead_user_id")
})
public class Demand extends Auditable<UUID> {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(columnDefinition="UUID")
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "project_id", nullable = false)
    private ProjectEntity project;

    @Column(unique=true)
    private String demandName;

    //@DateTimeFormat("dd/MM/yyyy")
   // @Temporal(TemporalType.DATE)
    private LocalDate dueDate;

    //@Temporal(TemporalType.DATE)
    private LocalDate newDueDate;

    private int dueDateChangeCount = 0;

    @ManyToOne
    @JoinColumn(name = "assigned_to_user_id")
    private User assignedTo; // Manager role

    @ManyToOne
    @JoinColumn(name = "tech_lead_user_id")
    private User techLead; // Technical Lead role
    @JsonIgnore
    @OneToMany(mappedBy = "demand", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TasksEntity> tasks = new ArrayList<>();


    @Enumerated(EnumType.STRING)
    private Priority priority; 
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DemandTypes demandTypes; 

    @Enumerated(EnumType.STRING)
    private Status status;

    private LocalDate assignDate;  // Date when the demand is assigned

    private LocalDate statusChangeDate;  // Date when the status was last changed

/*     @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "status_journey", joinColumns = @JoinColumn(name = "demand_id"))
    @Column(name = "status_change_date")
    private Map<Status, LocalDate> statusJourney = new HashMap<>();  // To track status changes with dates */

    @OneToMany(mappedBy = "demand", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Comments> comments= new ArrayList<>(); // One demand has many comments

    public void extendDueDate(LocalDate newDueDate) {
        this.newDueDate = newDueDate;
        this.dueDateChangeCount++;
    }

}
