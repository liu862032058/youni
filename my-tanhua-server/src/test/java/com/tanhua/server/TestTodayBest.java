package com.tanhua.server;

import com.tanhua.server.service.TodayBestService;
import com.tanhua.server.vo.PageResult;
import com.tanhua.server.vo.RecommendUserQueryParam;
import com.tanhua.server.vo.TodayBest;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
public class TestTodayBest {

    @Autowired
    private TodayBestService todayBestService;

    @Test
    public void testQueryTodayBest(){
        String token = "";
        TodayBest todayBest = this.todayBestService.queryTodayBest(token);
        System.out.println(todayBest);
    }

    @Test
    public void testQueryTodayBestList(){
        String token = "";
        PageResult pageResult = this.todayBestService.queryRecommendUserList(new RecommendUserQueryParam(), token);
        System.out.println(pageResult);
    }

    @Test
    public void testMongoDBData() {
        /*db.recommend_user*/
//        for (int i = 50; i < 100; i++) {
//            int score = RandomUtils.nextInt(30, 99);
//            System.out.println("db.recommend_user.insert({\"userId\":"
//                    + i
//                    +",\"toUserId\":1,\"score\":"
//                    +score
//                    +",\"date\":\"2019/1/1\"})");
//            }


        for (int i = 1; i < 100; i++) {
            String mobile = "13"+ RandomStringUtils.randomNumeric(9);
            System.out.println("INSERT INTO `tb_user` (`id`, `mobile`,`password`, `created`, `updated`) VALUES ('"+i+"', '"+mobile+"', 'e10adc3949ba59abbe56e057f20f883e', '2019-08-02 16:43:46', '2019-08-02 16:43:46');");
        }
        System.out.println("------------------------");


        for (int i = 3; i < 100; i++) {
            String logo = "http://mytanhua.oss-cn-beijing.aliyuncs.com/images/logo/"+RandomUtils.nextInt(1,20)+".jpg";
            System.out.println("INSERT INTO `tb_user_info` (`id`,`user_id`, `nick_name`, `logo`, `tags`, `sex`, `age`, `edu`, `city`,`birthday`, `cover_pic`, `industry`, `income`, `marriage`, `created`,`updated`) VALUES ('"+i+"', '"+i+"', 'heima_"+i+"', '"+logo+"', '单身,本科,年 龄相仿', '1', '"+RandomUtils.nextInt(20,50)+"', '本科', '北京市-北京城区-东城 区', '2019-08-01', '"+logo+"', '计算机行业', '40', '未婚', '2019-08-02 16:44:23', '2019-08-02 16:44:23');");
        }
    }

}
