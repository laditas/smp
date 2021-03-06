package com.alipapa.smp.user.mapper;

import com.alipapa.smp.user.pojo.User;
import com.alipapa.smp.user.pojo.UserExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface UserMapper {
    long countByExample(UserExample example);

    int deleteByExample(UserExample example);

    int deleteByPrimaryKey(Long id);

    int insert(User record);

    int insertSelective(User record);

    List<User> selectByExample(UserExample example);

    User selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") User record, @Param("example") UserExample example);

    int updateByExample(@Param("record") User record, @Param("example") UserExample example);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);

    List<User> fuzzyUserSearch(Map<String, String> params);

    Long selectMaxId();

    List<User> listConsumerFollowers(@Param("consumerId") Long consumerId, @Param("groupId") Long groupId);


}