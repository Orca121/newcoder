package com.newcoder.community.dao;

import com.newcoder.community.entity.LoginTicket;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LoginTicketMapper {

    LoginTicket selectLoginTicket(String ticket);

    int insertLoginTicket(LoginTicket loginTicket);

    int updateStatus(@Param("ticket") String ticket, @Param("status") int status);

}
