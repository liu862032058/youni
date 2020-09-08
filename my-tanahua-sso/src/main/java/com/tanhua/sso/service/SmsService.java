package com.tanhua.sso.service;


import com.alibaba.druid.support.json.JSONUtils;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tanhua.sso.utils.HttpUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Service
public class SmsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmsService.class);

    @Autowired
    private RestTemplate restTemplate;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private RedisTemplate<String, String> redisTemplate;



    /**
     * 发送验证码
     *
     * @param mobile
     * @return
     */
    public Map<String, Object> sendCheckCode(String mobile) {
        Map<String, Object> result = new HashMap<>(2);
        try {
            String redisKey = "CHECK_CODE_" + mobile;
            String value = this.redisTemplate.opsForValue().get(redisKey);
            if (StringUtils.isNotEmpty(value)) {
                result.put("code", 1);
                result.put("msg", "上一次发送的验证码还未失效");
                return result;
            }
//            String code = this.sendSms(mobile);//yunzhixun
            String code = this.sendAliSms(mobile);//ali

            if (null == code) {
                result.put("code", 2);
                result.put("msg", "发送短信验证码失败");
                return result;
            }
            //发送验证码成功
            result.put("code", 3);
            result.put("msg", "ok");

            //将验证码存储到Redis,2分钟后失效
            this.redisTemplate.opsForValue().set(redisKey, code, Duration.ofMinutes(5));
            return result;
        } catch (Exception e) {
            LOGGER.error("发送验证码出错！" + mobile, e);
            result.put("code", 4);
            result.put("msg", "发送验证码出现异常");
            return result;
        }
    }


    /**
     * 发送验证码短信(云资讯)
     *
     * @param mobile
     */
    public String sendSms(String mobile) {

        String url = "";
        Map<String, Object> params = new HashMap<>();
        params.put("sid", "");
        params.put("token", "");
        params.put("appid", "");
        params.put("templateid", "");
        params.put("mobile", mobile);
        // 生成6位数验证
        params.put("param", RandomUtils.nextInt(100000, 999999));
        ResponseEntity<String> responseEntity = this.restTemplate.postForEntity(url, params, String.class);


        String body = responseEntity.getBody();

        try {
            JsonNode jsonNode = MAPPER.readTree(body);
            //000000 表示发送成功
            if (StringUtils.equals(jsonNode.get("code").textValue(), "000000")) {
                return String.valueOf(params.get("param"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;


    }

    /**
     * 发送验证码短信(阿里)
     *
     * @param mobile
     */
    public String sendAliSms(String mobile) {

        String url = "http://dingxin.market.alicloudapi.com";
        String path = "/dx/sendSms";
        String method = "POST";
        String appcode = "4f75649e87ae42ab84377aa39a9db3bd";
        Map<String, String> headers = new HashMap<String, String>();
        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
        headers.put("Authorization", "APPCODE " + appcode);
        Map<String, String> params = new HashMap<String, String>();
        params.put("mobile", mobile);
        params.put("tpl_id", "TP1711063");
        // 生成6位数验证
        params.put("param", ""+RandomUtils.nextInt(100000, 999999));


        Map<String, String> bodys = new HashMap<String, String>();


        try {
            /**
             * 重要提示如下:
             * HttpUtils请从
             * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/src/main/java/com/aliyun/api/gateway/demo/util/HttpUtils.java
             * 下载
             *
             * 相应的依赖请参照
             * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/pom.xml
             */
            HttpResponse response = HttpUtils.doPost(url, path, method, headers, params, bodys);
            //读取服务器返回过来的json字符串数据
            String str = EntityUtils.toString(response.getEntity());
            //把json字符串转换成json对象
            JSONObject jsonResult = JSONObject.parseObject(str);

            int statusCode = response.getStatusLine().getStatusCode();
//            JsonNode jsonNode = MAPPER.readTree(body);
                if (statusCode==200) {
                    return String.valueOf(params.get("param"));
                }

        } catch (Exception e) {
            e.printStackTrace();
        }



        return null;


    }

}
