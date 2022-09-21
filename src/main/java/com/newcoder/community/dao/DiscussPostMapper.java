package com.newcoder.community.dao;

import com.newcoder.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiscussPostMapper {

    // orderMode为0则普通排序，为1则按热度排序
    List<DiscussPost> selectDiscussPosts(@Param("userId") int userId,
                                         @Param("offset")int offset,
                                         @Param("limit")int limit,
                                         @Param("orderMode") int orderMode);

    // @Param注解用于给参数取别名,
    // 如果只有一个参数,并且在<if>里使用,则必须加别名.
    int selectDiscussPostRows(@Param("userId") int userId);

    DiscussPost selectDiscussPostById(int id);

    int insertDiscussPost(DiscussPost discussPost);

    int updateCommentCount(@Param("id") int id, @Param("commentCount") int commentCount);

    int updateType( @Param("id")int id, @Param("type")int type);

    int updateStatus(@Param("id")int id, @Param("status")int status);

    int updateScore(@Param("id")int id, @Param("score")double score);

}
