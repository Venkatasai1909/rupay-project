package com.rupay.forex.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Long id;
    @ManyToMany(targetEntity = User.class, mappedBy = "roles")
    private List<User> users = new ArrayList<>();
    private String role;

   public static Role createRole(String requestRole, User user){
       Role role = Role.builder().role(requestRole).users(new ArrayList<>()).build();

       role.getUsers().add(user);

       return role;
   }
}
