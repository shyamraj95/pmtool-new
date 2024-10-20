package com.api.pmtool.entity;
import com.api.pmtool.config.UserRoleMapSerializer;
import com.api.pmtool.enums.DemandTypes;
import com.api.pmtool.enums.Priority;
import com.api.pmtool.enums.Status;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

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
import javax.persistence.MapKeyJoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.time.LocalDate;
import org.hibernate.annotations.GenericGenerator;


@Getter
@Setter
@Entity
//@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(name = "demands")
public class Demand extends Auditable<UUID> {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(columnDefinition="UUID")
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "project_id", nullable = false)
    private ProjectEntity project;

    private String demandName;

    //@DateTimeFormat("dd/MM/yyyy")
   // @Temporal(TemporalType.DATE)
    private LocalDate dueDate;

    //@Temporal(TemporalType.DATE)
    private LocalDate newDueDate;

    private int dueDateChangeCount = 0;


    // Map of Users and their roles in the demand (MANAGER, TECH_LEAD, DEVELOPER)
    @JsonSerialize(using = UserRoleMapSerializer.class)
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "demand_user_roles",joinColumns = @JoinColumn(name = "demand_id"))
    @MapKeyJoinColumn(name = "user_id") // The map's key is the User entity
    @Column(name = "role")  // The map's value is the role (MANAGER, TECH_LEAD, DEVELOPER)
    private Map<User, String> userRoles = new HashMap<>();

    @Enumerated(EnumType.STRING)
    private Priority priority; 
    
    @Enumerated(EnumType.STRING)
    private DemandTypes demandTypes; 

    @Enumerated(EnumType.STRING)
    private Status status;

    private LocalDate assignDate;  // Date when the demand is assigned

    private LocalDate statusChangeDate;  // Date when the status was last changed

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "status_journey", joinColumns = @JoinColumn(name = "demand_id"))
    @Column(name = "status_change_date")
    private Map<Status, LocalDate> statusJourney = new HashMap<>();  // To track status changes with dates

    @OneToMany(mappedBy = "demand", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Comments> comments= new ArrayList<>(); // One demand has many comments

    public void extendDueDate(LocalDate newDueDate) {
        this.newDueDate = newDueDate;
        this.dueDateChangeCount++;
    }

}
