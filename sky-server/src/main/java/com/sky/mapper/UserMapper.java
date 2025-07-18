package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface UserMapper {

    /**
     * 根据openId查询用户
     * @param openId
     * @return
     */
    @Select("SELECT * FROM user WHERE openid = #{openId}")
    User getByOpenId(String openId);

    /**
     * 插入用户
     * @param user
     */
    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("INSERT INTO user (openid, name, phone, sex, id_number, avatar, create_time) " +
            "VALUES (#{openid},#{name},#{phone},#{sex},#{idNumber},#{avatar},#{createTime})")
    void insert(User user);

    /**
     * 根据id查询用户
     * @param userId
     * @return
     */
    @Select("SELECT * FROM user WHERE id = #{userId}")
    User getById(Long userId);

    /**
     * 根据参数统计用户数量
     * @param map
     * @return
     */
    Integer countUserByMap(Map<String, Object> map);
}
