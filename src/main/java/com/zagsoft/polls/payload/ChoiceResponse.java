package com.zagsoft.polls.payload;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ChoiceResponse {
    private long id;
    private String text;
    private long voteCount;
}
