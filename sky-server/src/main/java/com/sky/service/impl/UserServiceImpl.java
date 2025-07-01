package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {
    private static final String WECHAT_LOGIN = "https://api.weixin.qq.com/sns/jscode2session";
    @Autowired
    private WeChatProperties weChatProperties;
    @Autowired
    private UserMapper userMapper;

    /**
     * 微信用户登录
     * @param userLoginDTO
     * @return
     */
    @Override
    public User wechatLogin(UserLoginDTO userLoginDTO) {
        //向微信服务器请求获取openId
        String openId = getOpenId(userLoginDTO.getCode());
        if (openId == null) {//微信登录出现了错误
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }
        //查询数据库是否有这个用户
        User user = userMapper.getByOpenId(openId);
        if (user == null) {
            //不存在用户应当存入
            user = User.builder().openid(openId).createTime(LocalDateTime.now()).build();
            userMapper.insert(user);
        }
        return user;
    }

    /**
     * 向微信服务器请求用户openId
     * @param code
     * @return
     */
    private String getOpenId(String code) {
        //通过HttpClientUtil的doGet方法直接发送请求
        Map<String,String> request = new HashMap<>();
        request.put("appid",weChatProperties.getAppid());
        request.put("secret",weChatProperties.getSecret());
        request.put("js_code",code);
        request.put("grant_type","authorization_code");
        //发送请求
        String json = HttpClientUtil.doGet(WECHAT_LOGIN, request);
        //反序列化JSON字符串
        JSONObject jsonObject = JSONObject.parseObject(json);
        return jsonObject.getString("openid");
    }
}
