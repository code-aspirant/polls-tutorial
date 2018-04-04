package com.zagsoft.polls.payload;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Arrays;
import java.util.List;

@Getter
@Setter
public class PollRequest {
    @NotBlank
    @Size(max = 140)
    private String question;

    @NotNull
    @Size(min = 2, max = 6)
    @Valid
    private List<ChoiceRequest> choices;

    @NotNull
    @Valid
    private PollLength pollLength;
}

class Test {
    public static void main(String[] args) throws JsonProcessingException {
        PollRequest pollRequest = new PollRequest();
        ChoiceRequest choice1 = new ChoiceRequest();
        ChoiceRequest choice2 = new ChoiceRequest();
        PollLength pollLength = new PollLength();

        choice1.setText("This is choice 1");
        choice2.setText("This is choice 2");
        pollLength.setHours(8);
        pollLength.setDays(2);
        pollRequest.setQuestion("This is the poll question???");
        pollRequest.setChoices(Arrays.asList(choice1, choice2));
        pollRequest.setPollLength(pollLength);

        ObjectMapper objectMapper = new ObjectMapper();
        String result = objectMapper.writeValueAsString(pollRequest);
        System.out.println(result);
    }
}