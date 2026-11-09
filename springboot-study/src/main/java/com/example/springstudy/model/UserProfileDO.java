package com.example.springstudy.model;

import lombok.Getter;
import lombok.Setter;

/** 用户资料，对应 study_user_profile 表；一个用户最多一条资料。 */
@Getter
@Setter
public class UserProfileDO {

    private Long id;
    private String nickname;
    private String city;

    @Override
    public String toString() {
        return "UserProfileDO{id=" + id + ", nickname='" + nickname + "', city='" + city + "'}";
    }
}
