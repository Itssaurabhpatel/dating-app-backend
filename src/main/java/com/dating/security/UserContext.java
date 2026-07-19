package com.dating.security;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class UserContext {

    private String userId;
    private String email;
    private Set<String> roles;
    private boolean verified;
    private boolean premium;
}
