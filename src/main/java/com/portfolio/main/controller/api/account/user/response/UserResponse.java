package com.portfolio.main.controller.api.account.user.response;

import com.portfolio.main.account.domain.User;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UserResponse {
    private Long id;
    private String loginId;
    private String username;
    private LocalDateTime createdAt;
    private String roleNames;

    public UserResponse(User user) {
        this.id = user.getId();
        this.loginId = user.getLoginId();
        this.username = user.getUsername();
        this.createdAt = user.getCreatedAt();
        this.roleNames = String.join(", ", user.getRoleNames());
    }

}
