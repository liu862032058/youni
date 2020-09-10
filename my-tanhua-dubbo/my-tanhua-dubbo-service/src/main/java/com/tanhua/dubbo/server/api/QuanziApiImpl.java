package com.tanhua.dubbo.server.api;


import com.alibaba.dubbo.config.annotation.Service;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import server.api.QuanZiApi;
import server.pojo.Album;
import server.pojo.Publish;
import server.pojo.TimeLine;
import server.pojo.Users;
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
}
