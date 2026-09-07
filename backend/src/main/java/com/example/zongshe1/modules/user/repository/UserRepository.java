package com.example.zongshe1.repository;

import com.example.zongshe1.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户数据访问层接口
 * 使用Spring Data JPA，无需实现方法，自动生成SQL
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 根据用户UUID查询用户
     * @param userId 用户UUID
     * @return Optional包装的用户对象
     */
    Optional<User> findByUserId(String userId);

    /**
     * 根据手机号查询用户
     * @param phoneNumber 手机号
     * @return 用户对象
     */
    User findByPhoneNumber(String phoneNumber);

    /**
     * 根据身份证号查询用户
     * @param idCardNumber 身份证号
     * @return 用户对象
     */
    User findByIdCardNumber(String idCardNumber);

    /**
     * 根据用户名模糊查询
     * @param userName 用户名
     * @return 用户列表
     */
    List<User> findByUserNameContaining(String userName);

    /**
     * 根据用户状态查询
     * @param userStatus 用户状态
     * @return 用户列表
     */
    List<User> findByUserStatus(Integer userStatus);

    /**
     * 查询信用分大于等于指定值的用户
     * @param minCreditScore 最低信用分
     * @return 用户列表
     */
    @Query("SELECT u FROM User u WHERE u.creditScore >= :minCreditScore")
    List<User> findUsersWithMinCreditScore(@Param("minCreditScore") Integer minCreditScore);

    /**
     * 检查手机号是否已存在
     * @param phoneNumber 手机号
     * @return 是否存在
     */
    boolean existsByPhoneNumber(String phoneNumber);

    /**
     * 检查身份证号是否已存在
     * @param idCardNumber 身份证号
     * @return 是否存在
     */
    boolean existsByIdCardNumber(String idCardNumber);
}