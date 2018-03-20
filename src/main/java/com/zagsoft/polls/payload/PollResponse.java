package com.zagsoft.polls.payload;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
public class PollResponse {
    private Long id;
    private String question;
    private List<ChoiceRequest> choices;
    private UserSummary createdBy;
    private Instant creationDateTime;
    private Instant expirationDatetime;
    private Boolean isExpired;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long selectedChoice;
    private Long TotalVotes;
}
