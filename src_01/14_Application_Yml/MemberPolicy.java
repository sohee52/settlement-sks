package com.back.boundedContext.member.domain;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class MemberPolicy {
    private static int PASSWORD_CHANGE_DAYS;

    @Value("${custom.member.password.changeDays}")
    public void setPasswordChangeDays(int days) {
        PASSWORD_CHANGE_DAYS = days;
    }

    // 비밀번호 변경이 필요한 기간을 Duration 객체로 반환
    public Duration getNeedToChangePasswordPeriod() {
        return Duration.ofDays(PASSWORD_CHANGE_DAYS);
    }

    // 비밀번호 변경이 필요한 일수를 정수로 반환
    public int getNeedToChangePasswordDays() {
        return PASSWORD_CHANGE_DAYS;
    }

    // 마지막으로 비밀번호가 변경된 시점을 기준으로 비밀번호 변경이 필요한지 여부를 판단
    public boolean isNeedToChangePassword(LocalDateTime lastChangedAt) {
        if (lastChangedAt == null) return true;

        return lastChangedAt.plusDays(PASSWORD_CHANGE_DAYS)
                .isBefore(LocalDateTime.now());
    }
}
