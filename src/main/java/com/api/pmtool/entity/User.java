package com.api.pmtool.entity;

import java.util.UUID;
import java.util.Set;
import java.util.HashSet;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Index;

import org.hibernate.annotations.GenericGenerator;

import com.api.pmtool.enums.Gender;
import com.api.pmtool.enums.Designation;
import com.api.pmtool.enums.Expertise;
import com.api.pmtool.enums.Department;
import com.api.pmtool.enums.UserStatus;
import com.api.pmtool.enums.VendorsName;
import com.fasterxml.jackson.annotation.JsonBackReference;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "users",
indexes = {
    @Index(name = "idx_users_pf_id", columnList = "pfId"),
})
public class User extends Auditable<UUID> {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(unique=true)
    private String pfId;

    private String fullName;

    @JsonBackReference
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    private String mobileNumber;
    private String email;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    private Designation designation;

    @Enumerated(EnumType.STRING)
    private Department department;

    @Enumerated(EnumType.STRING)
    private UserStatus status;
    
    @Enumerated(EnumType.STRING)
    private VendorsName vendorName;

    @Enumerated(EnumType.STRING)
    private Expertise expertise;
}
