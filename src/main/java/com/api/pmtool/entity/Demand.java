package com.api.pmtool.entity;
import com.api.pmtool.config.UserRoleMapSerializer;
import com.api.pmtool.enums.Status;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
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
import javax.persistence.MapKeyJoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;


@Getter
@Setter
@Entity
//@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "userRoles"}) 
@Table(name = "demands")
public class Demand extends Auditable<UUID> {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(columnDefinition="UUID")
    private UUID id;
    private String projectName;
    private String demandName;

    //@DateTimeFormat("dd/MM/yyyy")
    @Temporal(TemporalType.DATE)
    private Date dueDate;

    @Temporal(TemporalType.DATE)
    private Date newDueDate;
    private int dueDateChangeCount = 0;


    // Map of Users and their roles in the demand (MANAGER, TECH_LEAD, DEVELOPER)
    @JsonSerialize(using = UserRoleMapSerializer.class)
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "demand_user_roles",joinColumns = @JoinColumn(name = "demand_id"))
    @MapKeyJoinColumn(name = "user_id") // The map's key is the User entity
    @Column(name = "role")  // The map's value is the role (MANAGER, TECH_LEAD, DEVELOPER)
    private Map<User, String> userRoles = new HashMap<>();
    
    @Enumerated(EnumType.STRING)
    private Status status;
    
    @OneToMany(mappedBy = "demand", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Comments> comments= new ArrayList<>(); // One demand has many comments

    public void extendDueDate(Date newDueDate) {
        this.newDueDate = newDueDate;
        this.dueDateChangeCount++;
    }

}
