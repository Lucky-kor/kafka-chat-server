package com.pingpong.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChatUserDto {
    private Long id;
    private String userId;
    private String name;
    private String profile;
}
