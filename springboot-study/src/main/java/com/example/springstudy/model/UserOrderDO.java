package com.example.springstudy.model;

import lombok.Getter;
import lombok.Setter;

/** 用户订单，对应 study_user_order 表；一个用户可以有多条订单。 */
@Getter
@Setter
public class UserOrderDO {

    private Long id;
    private String orderNo;

    /** MyBatis 按枚举名称读写数据库，JSON 中仍输出 PAID、SHIPPED 等字符串。 */
    private UserOrderStatusEnum status;

    private Long amountInCents;

    @Override
    public String toString() {
        return "UserOrderDO{id=" + id + ", orderNo='" + orderNo + "', status='" + status + "'}";
    }
}
