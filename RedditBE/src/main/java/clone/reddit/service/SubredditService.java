package clone.reddit.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import clone.reddit.dto.SubredditDto;
import clone.reddit.mapper.SubredditMapper;
import clone.reddit.model.Subreddit;
import clone.reddit.repository.SubredditRepository;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class SubredditService {
	
	private final SubredditRepository subredditRepository;
	private final SubredditMapper subredditMapper;
	
	@Transactional
	public SubredditDto save(SubredditDto subredditDto) {
		Subreddit subreddit= subredditMapper.mapDtoToSubreddit(subredditDto);
		Subreddit savedSubreddit= subredditRepository.save(subreddit);
		subredditDto.setId(savedSubreddit.getId());
		return subredditDto;
	}

	@Transactional(readOnly = true)
	public List<SubredditDto> getAll() {
		return subredditRepository.findAll().stream().map(subredditMapper::mapSubredditToDto).collect(Collectors.toList());
	}

	public SubredditDto getSubreddit(Long id) throws Exception {
		Subreddit subreddit= subredditRepository.findById(id).orElseThrow(()->
				new Exception("Subreddit with the id "+id+ " does not exist."));
		return subredditMapper.mapSubredditToDto(subreddit);
	}
}