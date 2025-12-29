package com.back.shared.market.dto;

import com.back.boundedContext.market.domain.MarketMember;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class MarketMemberDto {
    private final int id;
    private final LocalDateTime createDate;
    private final LocalDateTime modifyDate;
    private final String username;
    private final String nickname;
    private final int activityScore;

    public MarketMemberDto(MarketMember member) {
        this(
                member.getId(),
                member.getCreateDate(),
                member.getModifyDate(),
                member.getUsername(),
                member.getNickname(),
                member.getActivityScore()
        );
    }
}