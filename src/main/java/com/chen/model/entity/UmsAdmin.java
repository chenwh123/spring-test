package com.chen.model.entity;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.FieldFill;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@TableName("ums_admin")
@NoArgsConstructor
public class UmsAdmin {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField(value = "username")
    private String username;

    @TableField(value = "password")
    private String password;

    @TableField(value = "icon")
    private String icon;

    @TableField(value = "email")
    private String email;

    @TableField(value = "nick_name")
    private String nickName;

    @TableField(value = "note")
    private String note;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(value = "login_time")
    private Date loginTime;

    @TableField(value = "status")
    private Integer status;


}