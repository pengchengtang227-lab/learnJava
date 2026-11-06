package com.example.springstudy.model;

import java.time.LocalDateTime;

/**
 * 对应 study_user 表的数据对象，字段映射由 UserMapper.xml 的 resultMap 明确配置。
 * 使用普通 JavaBean 而不是不可变 record，是为了让 MyBatis 回填数据库生成的 ID。
 * 本例没有密码等敏感字段，因此和账户示例一样直接返回；业务扩大后可单独定义响应 VO。
 */
public class UserDO {

    /** 数据库自增主键；插入前为空，插入后通过 keyProperty="id" 回填。 */
    private Long id;

    /** 用户名，对应数据库列 user_name，数据库唯一索引保证不能重复。 */
    private String userName;

    private Integer age;
    private String email;

    /** 性别：0 未知，1 男，2 女；已有用户迁移后为 0。 */
    private Integer gender;

    /** 时间由数据库生成和更新，客户端不能通过请求修改。 */
    private LocalDateTime gmtCreate;
    private LocalDateTime gmtModified;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getGender() {
        return gender;
    }

    public void setGender(Integer gender) {
        this.gender = gender;
    }

    public LocalDateTime getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(LocalDateTime gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    public LocalDateTime getGmtModified() {
        return gmtModified;
    }

    public void setGmtModified(LocalDateTime gmtModified) {
        this.gmtModified = gmtModified;
    }

    @Override
    public String toString() {
        // 调试时只显示定位所需的 ID，不在日志中输出邮箱等个人信息。
        return "UserDO{id=" + id + "}";
    }
}
