package com.login.common;

import lombok.Data;

@Data
public class UserProfileVO {
    private Long id;
    private String userName;
    private String nickname;
    private String email;
    private String avatar;
    private String role;
    private Integer status;
    private Integer articleCount;
    private Integer likeCount;
}
