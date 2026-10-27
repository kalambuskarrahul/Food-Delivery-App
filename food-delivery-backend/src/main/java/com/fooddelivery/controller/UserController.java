package com.fooddelivery.controller;

import com.fooddelivery.dto.response.UserResponse;
import com.fooddelivery.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    /** Returns the logged-in user. Handy for checking that a token works. */
    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal CustomUserDetails principal) {
        return UserResponse.from(principal.getUser());
    }
}
