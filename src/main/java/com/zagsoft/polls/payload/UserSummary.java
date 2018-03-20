package com.zagsoft.polls.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class UserSummary {
    private Long id;
    private String username;
    private String name;
}
