package clone.reddit.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import clone.reddit.dto.SubredditDto;
import clone.reddit.service.SubredditService;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/subreddit")
@AllArgsConstructor
public class SubredditController {
	
	private final SubredditService subredditService;
	
	@PostMapping
	public ResponseEntity<SubredditDto> createSubreddit(@RequestBody SubredditDto subredditDto) {
		return new ResponseEntity<>(subredditService.save(subredditDto),HttpStatus.CREATED);
	}
	
	@GetMapping
	public ResponseEntity<List<SubredditDto>> getAllSubreddits() {
		return new ResponseEntity<>(subredditService.getAll(),HttpStatus.OK);
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<SubredditDto> getSubreddit(@PathVariable Long id) throws Exception{
		return new ResponseEntity<> (subredditService.getSubreddit(id),HttpStatus.OK);
	}
}
