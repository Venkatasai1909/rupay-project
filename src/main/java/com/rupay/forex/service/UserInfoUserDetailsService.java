package com.rupay.forex.service;

import com.rupay.forex.exception.CustomException;
import com.rupay.forex.model.Role;
import com.rupay.forex.model.User;
import com.rupay.forex.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserInfoUserDetailsService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByName(username);

        if (user == null) {
            throw new CustomException("User not found " + username, "USER_NOT_FOUND");
        }

        System.out.println(user.getPassword());

        for(Role role : user.getRoles()){
            System.out.println(role.getRole());
        }

        user.setRoles(user.getRoles());

        return new UserInfoUserDetails(user);
    }
}