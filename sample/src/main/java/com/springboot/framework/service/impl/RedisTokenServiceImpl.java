package com.springboot.framework.service.impl;

import com.alibaba.fastjson.JSON;
import com.springboot.framework.config.AppConfig;
import com.springboot.framework.vo.UserVO;
import com.springboot.framework.constant.Const;
import com.springboot.framework.service.RedisTokenService;
import com.springboot.framework.utils.RedisUtil;
import com.springboot.framework.utils.StringUtil;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 基础的控制器，如对Token的操作等..
 *
 * @author haungpengfei
 * @version 1.1.0315
 * @since 2019/1/10
 */
@Component
public class RedisTokenServiceImpl implements RedisTokenService {
    @Resource
    private RedisUtil redisUtils;
    @Resource
    private AppConfig appConfig;

    /**
     * 创建token
     */
    @Override
    public String getToken(UserVO userVO) {
        //1.使用（应用名称+应用环境+缓存对象类型+adminId）作为源token
//        String adminId = Const.SERVER_USER_KEY + userVO.getId();
        String adminId = appConfig.getAppName() + "_" + appConfig.getEnv() + "_" + Const.SERVER_USER_KEY + userVO.getId();
        //2.使用uuid作为源token
//        String token = UUID.randomUUID().toString().replace("-", "");
        String token = userVO.getAccessToken();
        //判断用户是否存在
        if (redisUtils.hasKey(adminId)) {
            redisUtils.del((String) redisUtils.get(adminId));
        }
        redisUtils.set(adminId, token, Const.SERVER_USER_EXP_KEY);

        // JSON格式
        String userJson = JSON.toJSONString(userVO);
//        String token_format = String.format(Const.SERVER_USER_KEY, token);
        redisUtils.set(token, userJson, Const.SERVER_USER_EXP_KEY);
        return token;
    }

    /**
     * 刷新用户
     */
    @Override
    public void refreshUserToken(String token) {
        token = String.format(Const.SERVER_USER_KEY, token);
        if (redisUtils.hasKey(token)) {
            redisUtils.expire(token, Const.SERVER_USER_EXP_KEY);
        }
    }

    /**
     * 用户退出登陆
     */
    @Override
    public void loginOff(UserVO userVO) {
//        token = String.format(Const.SERVER_USER_KEY, token);
        redisUtils.del(Const.SERVER_USER_KEY + userVO.getId());
        redisUtils.del(userVO.getAccessToken());
    }

    /**
     * 用户退出登陆
     */
    @Override
    public void loginOff(HttpServletRequest request) {
        String key = getUserSessionKey(request);
        redisUtils.del(Const.SERVER_USER_KEY + getUserInfoByToken(key).getId());
        redisUtils.del(getUserInfoByToken(key).getAccessToken());
    }

    /**
     * 获取用户信息
     */
    @Override
    public UserVO getUserInfoByToken(String token) {
//        token = String.format(Const.SERVER_USER_KEY, token);
        if (redisUtils.hasKey(token)) {
            String jsonStr = (String) redisUtils.get(token);
            return JSON.parseObject(jsonStr, UserVO.class);
        }
        return null;
    }

    /**
     * 获取登录用户
     */
    @Override
    public UserVO getSessionUser(HttpServletRequest request) {
        String key = getUserSessionKey(request);
        return getUserInfoByToken(key);
//        String jsonStr = (String) redisUtils.get(key);
//        System.out.println(jsonStr);
//        if (StringUtil.isBlank(jsonStr)) {
//            ExceptionUtil.throwException(Errors.SYSTEM_NOT_LOGIN);
//        }
//        UserVO user = JSON.parseObject(jsonStr, UserVO.class);
//        if (user != null) {
//            memcachedService.set(key, Const.SERVER_USER_EXP_KEY, jsonStr);
//            String accessKey = Const.SERVER_USER_KEY + user.getId();
//            memcachedService.set(accessKey, Const.SERVER_USER_EXP_KEY, key);
//        }
//        return user;
    }

    /**
     * 获取真实ip
     */
    @Override
    public String getRemoteIp(HttpServletRequest request) {
        if (request.getHeader("x-forwarded-for") == null) {
            return request.getRemoteAddr();
        }
        return request.getHeader("x-forwarded-for");
    }

    /**
     * 获取用户缓存key
     */
    private String getUserSessionKey(HttpServletRequest request) {
//        return Const.SERVER_USER_KEY + getSessionKey(request);
        return getSessionKey(request);
    }

    /**
     * 获取缓存key
     * 同时使用，使用token保存登录信息，优先使用token，如果获取失败则取session
     */
    private String getSessionKey(HttpServletRequest request) {
        String sessionId = "";
        Object sessionIdAttribute = request.getAttribute(Const.ACCESS_TOKEN_HEADER_NAME);

        if (StringUtil.isNotBlank(sessionIdAttribute)) {
            sessionId = sessionIdAttribute.toString();
        }
        if (StringUtil.isBlank(sessionId)) {
            sessionId = request.getHeader(Const.ACCESS_TOKEN_HEADER_NAME);
        }
        if (StringUtil.isBlank(sessionId)) {
            sessionId = request.getParameter("token");
        }
        if (StringUtil.isBlank(sessionId)) {
            sessionId = request.getSession().getId();
        }
        return sessionId;
    }
}