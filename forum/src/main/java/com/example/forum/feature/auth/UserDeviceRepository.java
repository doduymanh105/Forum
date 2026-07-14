package com.example.forum.feature.auth;

import com.example.forum.domain.Enum.DeviceStatus;
import com.example.forum.domain.UserDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserDeviceRepository extends JpaRepository<UserDevice, Long> {
    Optional<UserDevice> findByRefreshTokenHash(String hash);
    Optional<UserDevice> findByUserIdAndDeviceId(Long userId, String deviceId);
    boolean existsByUserIdAndDeviceId(Long userId, String deviceId);

    List<UserDevice> findAllByUserIdAndStatus(Long userId, DeviceStatus active);

    @Modifying
    @Query(
            value = """
                    update user_devices set status ='REVOKED' where user_id =:userId
                    """,
            nativeQuery = true
    )
    void revokeAllByUserId(@Param("userId") Long userId);
}