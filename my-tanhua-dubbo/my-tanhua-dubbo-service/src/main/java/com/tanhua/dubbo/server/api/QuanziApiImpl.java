package com.tanhua.dubbo.server.api;


import com.alibaba.dubbo.config.annotation.Service;
import com.mongodb.client.result.DeleteResult;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import server.api.QuanZiApi;
import server.pojo.*;
import server.vo.PageInfo;

import java.util.List;

@Service(version = "1.0.0")
public class QuanziApiImpl implements QuanZiApi {

    @Autowired
    private MongoTemplate mongoTemplate;


    @Override
    public boolean savePublish(Publish publish) {
        //校验publish对象
        if (publish.getUserId() == null) {
            return false;
        }

        try {
            //填充数据
            publish.setId(ObjectId.get());
            publish.setCreated(System.currentTimeMillis()); //发布时间
            publish.setSeeType(1); //查看权限
            //保存动态信息
            mongoTemplate.save(publish);

            Album album = new Album();
            album.setId(ObjectId.get());
            album.setPublishId(publish.getId());//动态id
            album.setCreated(System.currentTimeMillis());
            //将相册对象写入到MongoDB中
            this.mongoTemplate.save(album, "quanzi_album_" + publish.getUserId());

            //查询当前用户的好友数据，将动态数据写入到好友的时间线表中
            Query query = Query.query(Criteria.where("userId").is(publish.getUserId()));
            List<Users> users = this.mongoTemplate.find(query, Users.class);
            for (Users user : users) {

                TimeLine timeLine = new TimeLine();
                timeLine.setId(ObjectId.get());
                timeLine.setUserId(publish.getUserId());
                timeLine.setPublishId(publish.getId());
                timeLine.setDate(System.currentTimeMillis());
                this.mongoTemplate.save(timeLine, "quanzi_time_line_" + user.getFriendId());
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            //TODO 事务回滚
        }
        return false;
    }

    @Override
    public PageInfo<Publish> queryPublishList(Long userId, Integer page, Integer pageSize) {
        return null;
    }

    @Override
    public boolean saveLikeComment(Long userId, String publishId) {
        //判断是否已经点赞，如果已经点赞就返回
        Criteria criteria =Criteria.where("userId").is(userId)
                .and("publishId").is(new ObjectId(publishId))
                .and("commentType").is(1);
        Query query = Query.query(criteria);
        long count = mongoTemplate.count(query, Comment.class);
        if (count > 0) {
            return false;
        }

        //type：评论类型，1-点赞，2-评论，3-喜欢
        return this.saveComment(userId, publishId, 1, null);
    }

    @Override
    public boolean removeComment(Long userId, String publishId, Integer commentType) {
        //取消点赞
        Criteria criteria =Criteria.where("userId").is(userId)
                .and("publishId").is(new ObjectId(publishId))
                .and("commentType").is(commentType);
        Query query = Query.query(criteria);
        DeleteResult deleteResult = mongoTemplate.remove(query, Comment.class);
        return deleteResult.getDeletedCount()>0;
    }

    @Override
    public boolean saveLoveComment(Long userId, String publishId) {
        //判断是否已经喜欢，如果已经喜欢就返回
        Criteria criteria =Criteria.where("userId").is(userId)
                .and("publishId").is(new ObjectId(publishId))
                .and("commentType").is(3);
        Query query = Query.query(criteria);
        long count = mongoTemplate.count(query, Comment.class);
        if (count > 0) {
            return false;
        }

        //type：评论类型，1-点赞，2-评论，3-喜欢
        return this.saveComment(userId, publishId, 3, null);
    }

    @Override
    public boolean saveComment(Long userId, String publishId, Integer type, String content) {
        try {
            Comment comment = new Comment();
            comment.setContent(content);
            comment.setIsParent(true);
            comment.setCommentType(type);
            comment.setPublishId(new ObjectId(publishId));
            comment.setUserId(userId);
            comment.setId(ObjectId.get());
            comment.setCreated(System.currentTimeMillis());

            this.mongoTemplate.save(comment);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public Long queryCommentCount(String publishId, Integer type) {
        Criteria criteria = Criteria.where("publishId").is(new ObjectId(publishId))
                .and("commentType").is(type);
        Query query = Query.query(criteria);
        return this.mongoTemplate.count(query, Comment.class);
    }

    @Override
    public Publish queryPublishById(String publishId) {
        return this.mongoTemplate.findById(new ObjectId(publishId), Publish.class);
    }

    @Override
    public PageInfo<Comment> queryCommentList(String publishId, Integer page, Integer pageSize) {
        PageRequest pageRequest = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Order.asc("created")));

        Query query = Query.query(Criteria.where("publishId").is(new ObjectId(publishId))
                .and("commentType").is(2)).with(pageRequest);

        List<Comment> commentList = this.mongoTemplate.find(query, Comment.class);

        PageInfo<Comment> pageInfo = new PageInfo<>();
        pageInfo.setTotal(0);
        pageInfo.setPageSize(pageSize);
        pageInfo.setPageNum(page);
        pageInfo.setRecords(commentList);

        return pageInfo;
    }
}
