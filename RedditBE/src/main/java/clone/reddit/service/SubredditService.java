package clone.reddit.service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import clone.reddit.dto.SubredditDto;
import clone.reddit.mapper.SubredditMapper;
import clone.reddit.model.Subreddit;
import clone.reddit.model.User;
import clone.reddit.repository.SubredditRepository;
import clone.reddit.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class SubredditService {
	
	private final SubredditRepository subredditRepository;
	private final UserRepository userRepository;
	private final SubredditMapper subredditMapper;
	
	@Transactional
	public SubredditDto save(SubredditDto subredditDto) {
        //temporary fix - FIXME later
        //****************************
        		
		User user = userRepository.findByUsername("alby3342").orElseThrow(()->new UsernameNotFoundException("Username not found - alby3342"));
        log.info("subredditDto: {}",subredditDto);
        
        //****************************

		Subreddit subreddit= subredditMapper.mapDtoToSubreddit(subredditDto);
		subreddit.setCreatedDate(Instant.now());
		subreddit.setUser(user);
		Subreddit savedSubreddit= subredditRepository.save(subreddit);
		subredditDto.setId(savedSubreddit.getId());
		log.info("subredditDto: {}",subredditDto);
		log.info("savedSubreddit: {}",savedSubreddit);
        
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