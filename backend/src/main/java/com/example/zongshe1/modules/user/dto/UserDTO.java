package com.example.zongshe1.dto;

import com.example.zongshe1.entity.User;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户数据传输对象
 * 用于API响应，可控制返回字段和脱敏处理
 */
@Data
public class UserDTO {
    private Long id;
    private String userId;
    private String userName;
    private String phoneNumber; // 需要在前端脱敏
    private String idCardNumber; // 需要在前端脱敏
    /** 渠道标识 ID（可空） */
    private String channelId;
    private Integer creditScore;
    private Integer userStatus;
    private LocalDateTime createTime;

    /**
     * 将User实体转换为UserDTO
     * @param user 用户实体
     * @return 用户DTO
     */
    public static UserDTO fromEntity(User user) {
        if (user == null) {
            return null;
        }
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUserId(user.getUserId());
        dto.setUserName(user.getUserName());
        dto.setPhoneNumber(maskPhoneNumber(user.getPhoneNumber()));
        dto.setIdCardNumber(maskIdCardNumber(user.getIdCardNumber()));
        dto.setCreditScore(user.getCreditScore());
        dto.setUserStatus(user.getUserStatus());
        // 注意：原实体类没有createTime字段，如需可添加
        return dto;
    }

    /**
     * 手机号脱敏：保留前3位+后4位
     */
    private static String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() != 11) {
            return phoneNumber;
        }
        return phoneNumber.substring(0, 3) + "****" + phoneNumber.substring(7);
    }

    /**
     * 身份证号脱敏：保留前6位+后4位
     */
    private static String maskIdCardNumber(String idCardNumber) {
        if (idCardNumber == null || idCardNumber.length() != 18) {
            return idCardNumber;
        }
        return idCardNumber.substring(0, 6) + "********" + idCardNumber.substring(14);
    }
    
    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }
}