package com.zagsoft.polls.poll;

import com.zagsoft.polls.exception.BadRequestException;
import com.zagsoft.polls.exception.ResourceNotFoundException;
import com.zagsoft.polls.payload.PagedResponse;
import com.zagsoft.polls.payload.PollResponse;
import com.zagsoft.polls.security.UserPrincipal;
import com.zagsoft.polls.user.User;
import com.zagsoft.polls.user.UserRepository;
import com.zagsoft.polls.util.AppConstants;
import com.zagsoft.polls.util.ModelMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
public class PollService {
    private final PollRepository pollRepository;
    private final VoteRepository voteRepository;
    private final UserRepository userRepository;

    @Autowired
    public PollService(PollRepository pollRepository, VoteRepository voteRepository, UserRepository userRepository) {
        this.pollRepository = pollRepository;
        this.voteRepository = voteRepository;
        this.userRepository = userRepository;
    }

    private static final Logger logger = LogManager.getLogger(PollService.class);

    public PagedResponse<PollResponse> getAllPolls(UserPrincipal currentUser, int page, int size) {
        validatePageNumberAndSize(page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
        Page<Poll> polls = pollRepository.findAll(pageable);

        if (polls.getNumberOfElements() == 0) {
            return emptyPage(polls);
        }

        List<Long> pollIds = polls.map(Poll::getId).getContent();
        Map<Long, Long> choiceVoteCountMap = getChoiceVoteCountMap(pollIds);
        Map<Long, Long> pollUserVoteMap = getPollUserVoteMap(currentUser, pollIds);
        Map<Long, User> creatorMap = getPollCreatorMap(polls.getContent());

        List<PollResponse> pollResponses = polls.map(poll ->
                ModelMapper.mapPollToPollResponse(
                        poll,
                        choiceVoteCountMap,
                        creatorMap.get(poll.getCreatedBy()),
                        pollUserVoteMap == null ? null : pollUserVoteMap.getOrDefault(poll.getId(), null))
        ).getContent();

        return new PagedResponse<>(pollResponses, polls.getNumber(), polls.getSize(), polls.getTotalElements(), polls.getTotalPages(), polls.isLast());
    }

    public PagedResponse<PollResponse> getPollsCreatedBy(String username, UserPrincipal currentUser, int page, int size) {
        validatePageNumberAndSize(page, size);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
        Page<Poll> polls = pollRepository.findByCreatedBy(user.getId(), pageable);

        if (polls.getNumberOfElements() == 0) {
            return emptyPage(polls);
        }

        // Map Polls to PollResponses containing vote counts and poll creator details
        List<Long> pollIds = polls.map(Poll::getId).getContent();
        Map<Long, Long> choiceVoteCountMap = getChoiceVoteCountMap(pollIds);
        Map<Long, Long> pollUserVoteMap = getPollUserVoteMap(currentUser, pollIds);

        List<PollResponse> pollResponses = polls.map(poll -> ModelMapper.mapPollToPollResponse(
                poll,
                choiceVoteCountMap,
                user,
                pollUserVoteMap == null ? null : pollUserVoteMap.getOrDefault(poll.getId(), null)
        )).getContent();
        return new PagedResponse<>(pollResponses, polls.getNumber(), polls.getSize(), polls.getTotalElements(), polls.getTotalPages(), polls.isLast());
    }

    public PagedResponse<PollResponse> getPollsVotedBy(String username, UserPrincipal currentUser, int page, int size) {
        validatePageNumberAndSize(page, size);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        // Retrieve all pollIds in which the given username has voted
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
        Page<Long> userVotedPollIds = voteRepository.findVotedPollIdsByUserId(user.getId(), pageable);

        if (userVotedPollIds.getNumberOfElements() == 0) {
            return emptyPage(userVotedPollIds);
        }

        // Retrieve all poll details from the voted pollIds
        List<Long> pollIds = userVotedPollIds.getContent();

        Sort sort = new Sort(Sort.Direction.DESC, "createdAt");
        List<Poll> polls = pollRepository.findByIdIn(pollIds, sort);

        // Map polls to pollresponses containing vote counts and poll creator details
        Map<Long, Long> choiceVoteCountMap = getChoiceVoteCountMap(pollIds);
        Map<Long, Long> getPolluserVoteMap = getPollUserVoteMap(currentUser, pollIds);
        Map<Long, User> createorMap = getPollCreatorMap(polls);

        List<PollResponse> pollResponses = polls.stream()
                .map(poll -> ModelMapper.mapPollToPollResponse(
                        poll,
                        choiceVoteCountMap,
                        createorMap.get(poll.getCreatedBy()),
                        getPolluserVoteMap == null ? null : getPolluserVoteMap.getOrDefault(poll.getId(), null)
                )).collect(Collectors.toList());

        return new PagedResponse<>(pollResponses, userVotedPollIds.getNumber(), userVotedPollIds.getSize(), userVotedPollIds.getTotalElements(), userVotedPollIds.getTotalPages(), userVotedPollIds.isLast());

    }

    private PagedResponse<PollResponse> emptyPage(Page<?> polls) {
        return new PagedResponse<>(
                Collections.emptyList(), polls.getNumber(),
                polls.getSize(), polls.getTotalElements(),
                polls.getTotalPages(), polls.isLast());
    }

    private void validatePageNumberAndSize(int page, int size) {
        if (page < 0) {
            throw new BadRequestException("Page number cannot be less than 0");
        }

        if (size > AppConstants.MAX_PAGE_SIZE) {
            throw new BadRequestException("Page size must not be greater than " + AppConstants.MAX_PAGE_SIZE);
        }
    }

    private Map<Long, Long> getChoiceVoteCountMap(List<Long> pollIds) {
        List<ChoiceVoteCount> votes = voteRepository.countByPollIdInGroupByChoiceId(pollIds);

        return votes.stream()
                .collect(Collectors.toMap(ChoiceVoteCount::getChoiceId, ChoiceVoteCount::getVoteCount));
    }

    private Map<Long, Long> getPollUserVoteMap(UserPrincipal currentUser, List<Long> pollIds) {
        Map<Long, Long> pollUserVoteMap = null;
        if (currentUser != null) {
            List<Vote> userVotes = voteRepository.findByUserIdAndPollIdIn(currentUser.getId(), pollIds);

            pollUserVoteMap = userVotes.stream()
                    .collect(Collectors.toMap(
                            vote -> vote.getPoll().getId(),
                            vote -> vote.getChoice().getId()));
        }
        return pollUserVoteMap;
    }

    private Map<Long, User> getPollCreatorMap(List<Poll> polls) {
        List<Long> creatorIds = polls.stream()
                .map(Poll::getCreatedBy)
                .distinct()
                .collect(Collectors.toList());
        List<User> creators = userRepository.findByIdIn(creatorIds);

        return creators.stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
    }
}
