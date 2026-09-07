package com.example.zongshe1.service;

import com.example.zongshe1.dto.UserDTO;
import com.example.zongshe1.dto.api.LoginRequest;
import com.example.zongshe1.dto.api.RegisterRequest;
import com.example.zongshe1.entity.User;

import java.util.Map;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 用户注册
     * @param request 注册请求
     * @return 注册结果
     */
    Map<String, Object> register(RegisterRequest request);

    /**
     * 用户登录
     * @param request 登录请求
     * @return 登录结果（包含JWT token）
     */
    Map<String, Object> login(LoginRequest request);

    /**
     * 根据用户ID获取用户信息
     * @param userId 用户UUID
     * @return 用户DTO
     */
    UserDTO getUserInfo(String userId);

    /**
     * 更新用户信息
     * @param userId 用户UUID
     * @param userName 用户名
     * @param phoneNumber 手机号
     * @return 更新结果
     */
    boolean updateUserInfo(String userId, String userName, String phoneNumber);

    /**
     * 更新用户信用分
     * @param userId 用户UUID
     * @param creditScore 信用分
     * @return 更新结果
     */
    boolean updateCreditScore(String userId, Integer creditScore);

    /**
     * 冻结/解冻用户账户
     * @param userId 用户UUID
     * @param userStatus 用户状态
     * @return 操作结果
     */
    boolean updateUserStatus(String userId, Integer userStatus);

    /**
     * 检查用户是否符合贷款条件
     * @param userId 用户UUID
     * @return 是否符合条件
     */
    boolean checkLoanQualification(String userId);

    /**
     * 根据手机号查找用户
     * @param phoneNumber 手机号
     * @return 用户实体
     */
    User findUserByPhoneNumber(String phoneNumber);

    /**
     * 发送验证码
     * @param phoneNumber 手机号
     * @return 发送结果
     */
    Map<String, Object> sendVerificationCode(String phoneNumber);

    /**
     * 验证码登录（如果用户不存在则自动注册）
     * @param phoneNumber 手机号
     * @param verificationCode 验证码
     * @return 登录结果（包含JWT token）
     */
    Map<String, Object> loginWithOtp(String phoneNumber, String verificationCode);

    /**
     * 获取用户可用额度
     * @param userId 用户ID
     * @return 可用额度信息
     */
    Map<String, Object> getUserLoanLimit(String userId);

    /**
     * 提交实名信息（姓名 + 身份证号），并触发信誉分重算。
     */
    Map<String, Object> submitIdentityProfile(String userId, String userName, String idCardNumber);
}