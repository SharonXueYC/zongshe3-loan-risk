package com.example.zongshe1.service.impl;

import com.example.zongshe1.dto.UserDTO;
import com.example.zongshe1.dto.api.LoginRequest;
import com.example.zongshe1.dto.api.RegisterRequest;
import com.example.zongshe1.entity.User;
import com.example.zongshe1.repository.UserRepository;
import com.example.zongshe1.service.CreditScoreCalculator;
import com.example.zongshe1.service.UserService;
import com.example.zongshe1.util.EncryptionUtil;
import com.example.zongshe1.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final JwtUtil jwtUtil;
    private final List<CreditScoreCalculator> creditScoreCalculators; // 注入所有信誉分计算策略

    @Value("${app.system.default-credit-score:500}")
    private Integer defaultCreditScore;

    @Value("${app.system.min-credit-score-for-loan:450}")
    private Integer minCreditScoreForLoan;

    @Value("${app.system.initial-loan-limit:5000}")
    private Integer initialLoanLimit;

    private static boolean hasIdentity(User user) {
        return user.getIdCardNumber() != null && !user.getIdCardNumber().isBlank();
    }

    /**
     * 获取优先级最高的信誉分计算策略
     * 
     * <p>如果有多个实现，选择优先级最高的（数字最小）</p>
     */
    private CreditScoreCalculator getCreditScoreCalculator() {
        return creditScoreCalculators.stream()
                .min((a, b) -> Integer.compare(a.getPriority(), b.getPriority()))
                .orElseThrow(() -> new RuntimeException("未找到信誉分计算策略"));
    }

    @Override
    @Transactional
    public Map<String, Object> register(RegisterRequest request) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 检查手机号是否已注册
            if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
                result.put("success", false);
                result.put("message", "手机号已注册");
                return result;
            }

            // 2. 检查身份证号（注册阶段可不填）
            String idCard = request.getIdCardNumber();
            if (idCard != null && !idCard.isBlank()) {
                idCard = idCard.trim().toUpperCase();
                if (!idCard.matches("^[1-9]\\d{5}(18|19|20)\\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$")) {
                    result.put("success", false);
                    result.put("message", "身份证号格式不正确");
                    return result;
                }
                if (userRepository.existsByIdCardNumber(idCard)) {
                    result.put("success", false);
                    result.put("message", "身份证号已注册");
                    return result;
                }
            }

            // 3. 创建用户实体
            User user = new User();
            user.setUserId(UUID.randomUUID().toString().replace("-", ""));
            user.setPhoneNumber(request.getPhoneNumber());
            String userName = request.getUserName();
            if (userName == null || userName.isBlank()) {
                userName = "用户" + request.getPhoneNumber().substring(request.getPhoneNumber().length() - 4);
            }
            user.setUserName(userName);
            if (request.getIdCardNumber() != null && !request.getIdCardNumber().isBlank()) {
                user.setIdCardNumber(request.getIdCardNumber().trim().toUpperCase());
            }
            user.setUserStatus(1);
            user.setCreditDocuments(null);
            
            // 新增这一行：将前端传过来的渠道 ID 存入用户实体
            user.setChannelId(request.getChannelId());

            // 4. 使用信誉分计算策略计算初始信誉分（预留扩展点）
            CreditScoreCalculator calculator = getCreditScoreCalculator();
            Integer initialCreditScore = calculator.calculateInitialCreditScore(user);
            user.setCreditScore(initialCreditScore != null ? initialCreditScore : defaultCreditScore);
            log.info("使用策略[{}]计算初始信誉分：userId={}, score={}", 
                    calculator.getStrategyName(), user.getUserId(), user.getCreditScore());

            // 4. 密码加密（注意：原User实体没有password字段，需要添加）
            // 这里假设User实体已添加passwordHash字段
            // user.setPasswordHash(EncryptionUtil.bcryptEncode(request.getPassword()));

            // 5. 保存用户
            userRepository.save(user);

            // 6. 发送短信验证码（模拟）
            sendSmsVerificationCode(request.getPhoneNumber());

            // 7. 返回结果
            result.put("success", true);
            result.put("userId", user.getUserId());
            result.put("message", "注册成功，请查收短信验证码");

            log.info("用户注册成功：phone={}, userId={}", request.getPhoneNumber(), user.getUserId());

        } catch (Exception e) {
            log.error("用户注册失败", e);
            result.put("success", false);
            result.put("message", "注册失败，请稍后重试");
        }

        return result;
    }

    @Override
    public Map<String, Object> login(LoginRequest request) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 根据手机号查询用户
            User user = userRepository.findByPhoneNumber(request.getPhoneNumber());
            if (user == null) {
                result.put("success", false);
                result.put("message", "用户不存在");
                return result;
            }

            // 2. 检查账户状态
            if (user.getUserStatus() == 2) {
                result.put("success", false);
                result.put("message", "账户已冻结，请联系客服");
                return result;
            }

            // 3. 验证密码（假设已添加密码验证）
            // if (!EncryptionUtil.bcryptVerify(request.getPassword(), user.getPasswordHash())) {
            //     result.put("success", false);
            //     result.put("message", "密码错误");
            //     return result;
            // }

            // 4. 生成JWT令牌
            String token = jwtUtil.generateToken(user.getUserId(), user.getPhoneNumber());

            // 5. 返回结果
            result.put("success", true);
            result.put("token", token);
            result.put("expiresIn", 7200); // 2小时
            result.put("userInfo", UserDTO.fromEntity(user));

            log.info("用户登录成功：userId={}", user.getUserId());

        } catch (Exception e) {
            log.error("用户登录失败", e);
            result.put("success", false);
            result.put("message", "登录失败，请稍后重试");
        }

        return result;
    }

    @Override
    public UserDTO getUserInfo(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        return UserDTO.fromEntity(user);
    }

    @Override
    public boolean updateUserInfo(String userId, String userName, String phoneNumber) {
        try {
            User user = userRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));

            if (userName != null && !userName.isEmpty()) {
                user.setUserName(userName);
            }

            if (phoneNumber != null && !phoneNumber.isEmpty()) {
                user.setPhoneNumber(phoneNumber);
            }

            userRepository.save(user);
            return true;
        } catch (Exception e) {
            log.error("更新用户信息失败", e);
            return false;
        }
    }

    @Override
    public boolean updateCreditScore(String userId, Integer creditScore) {
        try {
            User user = userRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));

            user.setCreditScore(creditScore);
            userRepository.save(user);
            return true;
        } catch (Exception e) {
            log.error("更新用户信用分失败", e);
            return false;
        }
    }

    @Override
    public boolean updateUserStatus(String userId, Integer userStatus) {
        try {
            User user = userRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));

            user.setUserStatus(userStatus);
            userRepository.save(user);
            return true;
        } catch (Exception e) {
            log.error("更新用户状态失败", e);
            return false;
        }
    }

    @Override
    public boolean checkLoanQualification(String userId) {
        try {
            User user = userRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));

            // 检查信用分是否达标
            if (user.getCreditScore() < minCreditScoreForLoan) {
                log.info("用户信用分不足：userId={}, creditScore={}", userId, user.getCreditScore());
                return false;
            }

            // 检查用户状态
            if (user.getUserStatus() != 1) { // 不是正常状态
                log.info("用户状态异常：userId={}, status={}", userId, user.getUserStatus());
                return false;
            }

            return true;
        } catch (Exception e) {
            log.error("检查贷款资格失败", e);
            return false;
        }
    }

    @Override
    public User findUserByPhoneNumber(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber);
    }

    /**
     * 发送短信验证码（模拟）
     */
    private void sendSmsVerificationCode(String phoneNumber) {
        // 生成6位随机验证码
        String code = String.format("%06d", (int) (Math.random() * 1000000));

        // 存储到Redis，有效期5分钟
        String key = "sms_code:" + phoneNumber;
        redisTemplate.opsForValue().set(key, code, 5, TimeUnit.MINUTES);

        // 模拟发送短信
        log.info("发送短信验证码：phone={}, code={}", phoneNumber, code);
    }

    @Override
    public Map<String, Object> sendVerificationCode(String phoneNumber) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 检查发送频率限制（1分钟内不能重复发送）
            String rateLimitKey = "sms_rate_limit:" + phoneNumber;
            if (redisTemplate.hasKey(rateLimitKey)) {
                result.put("success", false);
                result.put("message", "验证码发送过于频繁，请稍后再试");
                return result;
            }

            // 发送验证码
            sendSmsVerificationCode(phoneNumber);

            // 设置频率限制，1分钟内不能重复发送
            redisTemplate.opsForValue().set(rateLimitKey, "1", 1, TimeUnit.MINUTES);

            result.put("success", true);
            result.put("message", "验证码已发送，请查收短信");
            log.info("发送验证码成功：phone={}", phoneNumber);
        } catch (Exception e) {
            log.error("发送验证码失败", e);
            result.put("success", false);
            result.put("message", "发送验证码失败，请稍后重试");
        }
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> loginWithOtp(String phoneNumber, String verificationCode) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 1. 验证验证码
            String key = "sms_code:" + phoneNumber;
            String storedCode = redisTemplate.opsForValue().get(key);
            
            if (storedCode == null || !storedCode.equals(verificationCode)) {
                result.put("success", false);
                result.put("message", "验证码错误或已过期");
                return result;
            }

            // 2. 验证成功后删除验证码
            redisTemplate.delete(key);

            // 3. 查找用户，如果不存在则自动注册
            User user = userRepository.findByPhoneNumber(phoneNumber);
            if (user == null) {
                // 自动注册新用户（未实名，初始信用分 500）
                user = new User();
                user.setUserId(UUID.randomUUID().toString().replace("-", ""));
                user.setPhoneNumber(phoneNumber);
                user.setUserName("用户" + phoneNumber.substring(phoneNumber.length() - 4));
                user.setUserStatus(1);
                user.setCreditDocuments(null);
                CreditScoreCalculator calculator = getCreditScoreCalculator();
                Integer initialCreditScore = calculator.calculateInitialCreditScore(user);
                user.setCreditScore(initialCreditScore != null ? initialCreditScore : defaultCreditScore);
                userRepository.save(user);
                log.info("验证码登录自动注册新用户：phone={}, userId={}, score={}",
                        phoneNumber, user.getUserId(), user.getCreditScore());
            }

            // 4. 检查账户状态
            if (user.getUserStatus() == 2) {
                result.put("success", false);
                result.put("message", "账户已冻结，请联系客服");
                return result;
            }

            // 5. 生成JWT令牌
            String token = jwtUtil.generateToken(user.getUserId(), user.getPhoneNumber());

            // 6. 返回结果
            result.put("success", true);
            result.put("token", token);
            result.put("expiresIn", 7200); // 2小时
            result.put("userInfo", UserDTO.fromEntity(user));
            // 判断是否为新用户（简化处理，实际应该检查创建时间）
            result.put("isNewUser", user.getUserName() == null || user.getUserName().isEmpty());

            log.info("验证码登录成功：userId={}", user.getUserId());

        } catch (Exception e) {
            log.error("验证码登录失败", e);
            result.put("success", false);
            result.put("message", "登录失败，请稍后重试");
        }

        return result;
    }

    @Override
    public Map<String, Object> getUserLoanLimit(String userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            User user = userRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));

            int creditScore = user.getCreditScore() != null ? user.getCreditScore() : defaultCreditScore;
            int loanLimit;
            if (!hasIdentity(user)) {
                loanLimit = initialLoanLimit;
            } else {
                int baseLimit = 5000;
                int creditBonus = (creditScore - 500) / 10 * 1000;
                loanLimit = Math.min(baseLimit + creditBonus, 500000);
                if (creditScore < 500) {
                    loanLimit = 0;
                }
            }

            result.put("success", true);
            result.put("loanLimit", loanLimit);
            result.put("creditScore", user.getCreditScore());
            result.put("available", loanLimit > 0);

            log.info("获取用户额度：userId={}, limit={}, creditScore={}", userId, loanLimit, user.getCreditScore());

        } catch (Exception e) {
            log.error("获取用户额度失败", e);
            result.put("success", false);
            result.put("message", "获取额度失败");
        }

        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> submitIdentityProfile(String userId, String userName, String idCardNumber) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (userName == null || userName.isBlank()) {
                result.put("success", false);
                result.put("message", "请填写真实姓名");
                return result;
            }
            if (idCardNumber == null || !idCardNumber.matches("\\d{17}[\\dXx]")) {
                result.put("success", false);
                result.put("message", "请输入有效的18位身份证号");
                return result;
            }

            User user = userRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));

            User existing = userRepository.findByIdCardNumber(idCardNumber);
            if (existing != null && !existing.getUserId().equals(userId)) {
                result.put("success", false);
                result.put("message", "该身份证号已被其他账号使用");
                return result;
            }

            user.setUserName(userName.trim());
            user.setIdCardNumber(idCardNumber.toUpperCase());
            userRepository.save(user);

            CreditScoreCalculator calculator = getCreditScoreCalculator();
            calculator.recalculateCreditScore(userId);

            result.put("success", true);
            result.put("message", "身份信息已提交");
            result.put("userInfo", UserDTO.fromEntity(userRepository.findByUserId(userId).orElse(user)));
            log.info("用户提交实名信息：userId={}", userId);
        } catch (Exception e) {
            log.error("提交实名信息失败：userId={}", userId, e);
            result.put("success", false);
            result.put("message", "提交失败：" + e.getMessage());
        }
        return result;
    }
}