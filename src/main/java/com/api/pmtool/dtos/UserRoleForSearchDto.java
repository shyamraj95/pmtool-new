package com.api.pmtool.dtos;

public class UserRoleForSearchDto {
    private String pfId;  // Can be pfId or id of the user
    private String userName;  // User's full name
    private String role;  // The role (e.g., MANAGER, TECH_LEAD, DEVELOPER)

    public UserRoleForSearchDto(String pfId, String userName, String role) {
        this.pfId = pfId;
        this.userName = userName;
        this.role = role;
    }
}
