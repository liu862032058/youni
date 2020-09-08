package com.tanhua.sso.controller;


import com.tanhua.sso.service.UserInfoService;
import com.tanhua.sso.vo.ErrorResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("user")
public class UserInfoController {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserInfoController.class);

    @Autowired
    private UserInfoService userInfoService;


    /**
     * 完善个人信息
     *
     * @param param
     * @param token
     * @return
     */
    @RequestMapping("loginReginfo")
    @PostMapping
    public ResponseEntity<Object> saveUserInfo(@RequestBody Map<String, String> param, @RequestHeader("Authorization") String token) {

        try{
            Boolean saveUserInfo = this.userInfoService.saveUserInfo(param, token);
            if (saveUserInfo) {
               return ResponseEntity.status(HttpStatus.OK).body("ok");
            }

        }catch (Exception e){
            LOGGER.error("获取信息出错",e);
        }
        ErrorResult errorResult = ErrorResult.builder().errCode("000000").errMessage("发生错误").build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResult);
    }


    /**
     * 上传头像
     *
     * @param file
     * @param token
     * @return
     */
    @RequestMapping("loginReginfo/head")
    @PostMapping
    public ResponseEntity<Object> saveLogo(@RequestParam("headPhoto") MultipartFile file, @RequestHeader("Authorization") String token) {
        try {
            Boolean bool = this.userInfoService.saveLogo(file, token);
            if(bool){
                return ResponseEntity.ok(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        ErrorResult errorResult = ErrorResult.builder().errCode("000000").errMessage("图片非人像，请重新上传!").build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResult);


    }


 }
