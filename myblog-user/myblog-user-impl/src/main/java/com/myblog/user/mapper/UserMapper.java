package com.myblog.user.mapper;

import com.myblog.user.entity.UserEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 用户表 Mapper（只负责数据库访问，必须明确字段，禁止 SELECT *）
 */
@Mapper
public interface UserMapper {

    /** 按用户名查询 */
    @Select("SELECT id, username, password, nickname, avatar, email, signature, github, gitee, juejin, csdn, wechat_qr, status, create_time, update_time " +
            "FROM sys_user WHERE username = #{username}")
    UserEntity selectByUsername(@Param("username") String username);

    /** 按 ID 查询 */
    @Select("SELECT id, username, password, nickname, avatar, email, signature, github, gitee, juejin, csdn, wechat_qr, status, create_time, update_time " +
            "FROM sys_user WHERE id = #{id}")
    UserEntity selectById(@Param("id") Long id);

    /** 新增用户，回填主键 */
    @Insert("INSERT INTO sys_user (username, password, nickname, avatar, email, status, create_time, update_time) " +
            "VALUES (#{username}, #{password}, #{nickname}, #{avatar}, #{email}, #{status}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserEntity entity);

    /** 首个用户（单管理员博客场景，个人资料所指账号） */
    @Select("SELECT id, username, password, nickname, avatar, email, signature, github, gitee, juejin, csdn, wechat_qr, status, create_time, update_time " +
            "FROM sys_user ORDER BY id ASC LIMIT 1")
    UserEntity selectFirst();

    /** 更新个人资料（不含用户名与密码） */
    @Update("UPDATE sys_user SET nickname = #{nickname}, avatar = #{avatar}, email = #{email}, signature = #{signature}, " +
            "github = #{github}, gitee = #{gitee}, juejin = #{juejin}, csdn = #{csdn}, wechat_qr = #{wechatQr}, update_time = NOW() " +
            "WHERE id = #{id}")
    int updateProfile(UserEntity entity);

    /** 更新密码 */
    @Update("UPDATE sys_user SET password = #{password}, update_time = NOW() WHERE id = #{id}")
    int updatePassword(@Param("id") Long id, @Param("password") String password);
}