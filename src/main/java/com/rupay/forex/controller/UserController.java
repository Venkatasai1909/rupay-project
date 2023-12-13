package com.rupay.forex.controller;

import com.rupay.forex.dto.UserRequestDto;
import com.rupay.forex.dto.UserResponse;
import com.rupay.forex.service.UserService;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api")
@RestController
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/register-user")
    public ResponseEntity<UserResponse> registerUser(@NonNull @RequestBody UserRequestDto userRequestDto){
        return userService.registerUser(userRequestDto);
    }
}
