package com.tanhua.server.service;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tanhua.server.pojo.User;
import com.tanhua.server.pojo.UserInfo;
import com.tanhua.server.vo.PageResult;
import com.tanhua.server.vo.RecommendUserQueryParam;
import com.tanhua.server.vo.TodayBest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;
import server.pojo.RecommendUser;
import server.vo.PageInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class TodayBestService {

    @Autowired
    private UserService userService;

    @Autowired
    private RecommendUserService recommendUserService;

    @Autowired
    private UserInfoService userInfoService;


    /**
     * 默认I推荐id
     */
    @Value("${tanhua.sso.default.user}")
    private Long defaultUserId;

    public TodayBest queryTodayBest(String token) {

        User user = userService.queryUserByToken(token);
        if (user == null){
            return null;
        }

        TodayBest todayBest = recommendUserService.queryTodayBest(user.getId());
        if (todayBest == null) {
            //未找到最高得分的推荐用户，给出一个默认推荐用户
            todayBest = new TodayBest();
            todayBest.setId(defaultUserId);
            todayBest.setFateValue(95L);
        }

        // 补全用户信息
        UserInfo userInfo = this.userInfoService.queryUserInfoById(todayBest.getId());
        if (null != userInfo) {
            todayBest.setAge(userInfo.getAge());
            todayBest.setAvatar(userInfo.getLogo());
            todayBest.setGender(userInfo.getSex().name().toLowerCase());
            todayBest.setNickname(userInfo.getNickName());
            todayBest.setTags(StringUtils.split(userInfo.getTags(), ','));
        }
        return todayBest;
    }

    public PageResult queryRecommendUserList(RecommendUserQueryParam queryParam, String token) {

        //根据token查询当前登录的用户信息
        User user = userService.queryUserByToken(token);
        if (user == null){
            return null;
        }

        PageInfo<RecommendUser> pageInfo = this.recommendUserService.queryRecommendUserList(user.getId(), queryParam.getPage(), queryParam.getPagesize());
        List<RecommendUser> recommendUsers = pageInfo.getRecords();
        List<Long> userIds = new ArrayList<>();
        for (RecommendUser recommendUser : recommendUsers) {
            userIds.add(recommendUser.getUserId());
        }

        QueryWrapper<UserInfo> userInfoQueryWrapper = new QueryWrapper<>();
        userInfoQueryWrapper.in("user_id",userIds);//用户id
        if (queryParam.getAge() != null) {
            userInfoQueryWrapper.lt("age", queryParam.getAge()); //年龄
        }

        if (StringUtils.isNotEmpty(queryParam.getCity())) {
            userInfoQueryWrapper.eq("city", queryParam.getCity()); //城市
        }

        //需要查询用户的信息，并且按照条件查询
        List<UserInfo> userInfos = this.userInfoService.queryUserInfoList(userInfoQueryWrapper);
        List<TodayBest> todayBests = new ArrayList<>();
        for (UserInfo userInfo : userInfos) {
            TodayBest todayBest = new TodayBest();

            todayBest.setId(userInfo.getUserId());
            todayBest.setAge(userInfo.getAge());
            todayBest.setAvatar(userInfo.getLogo());
            todayBest.setGender(userInfo.getSex().name().toLowerCase());
            todayBest.setNickname(userInfo.getNickName());
            todayBest.setTags(StringUtils.split(userInfo.getTags(), ','));

            for (RecommendUser record : recommendUsers) {
                if(record.getUserId().longValue() == todayBest.getId().longValue()){
                    double score = Math.floor(record.getScore());
                    todayBest.setFateValue(Double.valueOf(score).longValue()); //缘分值
                }
            }

            todayBests.add(todayBest);
        }
        //对结果集做排序，按照缘分值倒序排序
        Collections.sort(todayBests, (o1, o2) -> Long.valueOf(o2.getFateValue() - o1.getFateValue()).intValue());

        return new PageResult(0, queryParam.getPagesize(), 0, queryParam.getPage(), todayBests);
    }
}
