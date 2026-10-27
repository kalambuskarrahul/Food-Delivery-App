package com.fooddelivery.dto.response;

import com.fooddelivery.entity.User;
import com.fooddelivery.enums.Role;

public record UserResponse(Long id, String name, String email, String phone, Role role) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getPhone(), user.getRole());
    }
}
