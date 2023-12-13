package com.rupay.forex.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRequestDto {
    @NonNull
    private String username;
    @NonNull
    private String password;
    @NonNull
    private String authorities;
}
