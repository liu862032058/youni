package com.tanhua.dubbo.server.api;

import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import com.tanhua.dubbo.server.pojo.RecommendUser;
import com.tanhua.dubbo.server.vo.PageInfo;

import java.util.List;

@Service(version = "1.0.0")
public class RecommendUserApiImpl implements RecommendUserApi {

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 今日佳人
     *
     * @param userId
     * @return
     */
    @Override
    public RecommendUser queryWithMaxScore(Long userId) {
        // 条件
        Criteria criteria = Criteria.where("toUserId").is(userId);
        // 按照得分倒序排序，获取第一条数据
        Query query =Query.query(criteria).with(Sort.by(Sort.Order.desc("score"))).limit(1);
        return mongoTemplate.findOne(query,RecommendUser.class);
    }

    /**
     * 推荐用户的列表查询
     *
     * @param userId
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public PageInfo<RecommendUser> queryPageInfo(Long userId, Integer pageNum, Integer pageSize) {
        // 条件
        Criteria criteria = Criteria.where("toUserId").is(userId);
        // 说明：数据总条数，暂时不提供，如果需要的话再提供
        PageRequest pageRequest = PageRequest.of(pageNum - 1, pageSize);
        Query query =Query.query(criteria).with(Sort.by(Sort.Order.desc("score"))).with(pageRequest);
        List<RecommendUser> recommendUsers = mongoTemplate.find(query, RecommendUser.class);
        PageInfo<RecommendUser> recommendUserPageInfo = new PageInfo<RecommendUser>(0, pageNum, pageSize, recommendUsers);
        return recommendUserPageInfo;
    }
}
