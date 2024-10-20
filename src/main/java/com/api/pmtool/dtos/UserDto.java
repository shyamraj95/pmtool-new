package com.api.pmtool.dtos;
import com.api.pmtool.enums.Department;
import com.api.pmtool.enums.Designation;
import com.api.pmtool.enums.Expertise;
import com.api.pmtool.enums.Gender;
import com.api.pmtool.enums.UserStatus;
import com.api.pmtool.enums.VendorsName;

import java.util.UUID;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDto {
    private String pfId;
    private String fullName;
    private Set<UUID> roleIds; 
    private String mobileNumber;
    private String email;
    private Gender gender;
    private Designation designation;
    private Department department;
    private UserStatus status;
    private VendorsName vendorName;
    private Expertise expertise;
}
