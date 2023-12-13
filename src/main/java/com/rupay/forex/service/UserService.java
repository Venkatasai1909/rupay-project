package com.rupay.forex.service;

import com.rupay.forex.dto.UserRequestDto;
import com.rupay.forex.dto.UserResponse;
import com.rupay.forex.exception.CustomException;
import com.rupay.forex.model.Role;
import com.rupay.forex.model.User;
import com.rupay.forex.repository.RoleRepository;
import com.rupay.forex.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public ResponseEntity<UserResponse> registerUser(UserRequestDto userRequestDto){
        String username = userRequestDto.getUsername();

        User existingUser = userRepository.findByName(username);

        if(existingUser != null){
            throw new CustomException("User already exists", "USER_ALREADY_EXISTS");
        }

        String bCryptPassword = passwordEncoder.encode(userRequestDto.getPassword());

        User user = User.builder().
                    name(userRequestDto.getUsername()).
                    password(bCryptPassword).
                    roles(new ArrayList<>()).
                    build();

        userRepository.save(user);

        List<String> roles = Arrays.stream(userRequestDto.getAuthorities().split(",")).toList();

        user.getRoles().addAll(createRoleAndReturn(roles, user));

        userRepository.save(user);

        UserResponse userResponse = UserResponse.builder().
                                    username(username).
                                    message("User saved successfully").build();

        return new ResponseEntity<>(userResponse, HttpStatus.CREATED);
    }

    private List<Role> createRoleAndReturn(List<String> roles, User user){
        List<Role> roleList = new ArrayList<>();

        for(String role : roles){
            Role existingRole = roleRepository.findByRole(role.trim());

            if(existingRole != null){
                existingRole.getUsers().add(user);

            } else {
                existingRole = Role.createRole(role.trim(), user);
            }

            roleRepository.save(existingRole);

            roleList.add(existingRole);
        }

        return roleList;
    }
}
