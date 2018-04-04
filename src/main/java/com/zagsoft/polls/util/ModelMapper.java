package com.zagsoft.polls.util;

import com.zagsoft.polls.payload.ChoiceResponse;
import com.zagsoft.polls.payload.PollResponse;
import com.zagsoft.polls.poll.Poll;
import com.zagsoft.polls.user.User;
import com.zagsoft.polls.user.UserSummary;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ModelMapper {

    public static PollResponse mapPollToPollResponse(Poll poll, Map<Long, Long> choiceVotesMap, User creator, Long userVote) {
        PollResponse pollResponse = PollResponse.builder()
                .id(poll.getId())
                .question(poll.getQuestion())
                .creationDateTime(poll.getCreatedAt())
                .expirationDatetime(poll.getExpirationDateTime())
                .build();
        Instant now = Instant.now();

        List<ChoiceResponse> choiceResponses = poll.getChoices().stream()
                .map(choice -> {
                    ChoiceResponse choiceResponse = ChoiceResponse.builder()
                            .id(choice.getId())
                            .text(choice.getText())
                            .build();
                    if (choiceVotesMap.containsKey(choice.getId())) {
                        choiceResponse.setVoteCount(choiceVotesMap.get(choice.getId()));
                    } else {
                        choiceResponse.setVoteCount(0);
                    }
                    return choiceResponse;
                }).collect(Collectors.toList());

        pollResponse.setChoices(choiceResponses);

        UserSummary creatorSummary = new UserSummary(creator.getId(), creator.getUsername(), creator.getName());
        pollResponse.setCreatedBy(creatorSummary);

        if (userVote != null) {
            pollResponse.setSelectedChoice(userVote);
        }

        long totalVotes = pollResponse.getChoices().stream()
                .mapToLong(ChoiceResponse::getVoteCount)
                .sum();
        pollResponse.setTotalVotes(totalVotes);

        return pollResponse;
    }
}
