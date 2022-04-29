package clone.reddit.service;

import static java.util.stream.Collectors.toList;

import java.util.List;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import clone.reddit.dto.PostRequest;
import clone.reddit.dto.PostResponse;
import clone.reddit.exception.PostNotFoundException;
import clone.reddit.exception.SubredditNotFoundException;
import clone.reddit.mapper.PostMapper;
import clone.reddit.model.Post;
import clone.reddit.model.Subreddit;
import clone.reddit.model.User;
import clone.reddit.repository.PostRepository;
import clone.reddit.repository.SubredditRepository;
import clone.reddit.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
@Transactional
public class PostService {

    private final PostRepository postRepository;
    private final SubredditRepository subredditRepository;
    private final UserRepository userRepository;
    private final AuthService authService;
    private final PostMapper postMapper;

    public void save(PostRequest postRequest) {
        Subreddit subreddit = subredditRepository.findByName(postRequest.getSubredditName())
                .orElseThrow(() -> new SubredditNotFoundException(postRequest.getSubredditName()));
        //postRepository.save(postMapper.map(postRequest, subreddit, authService.getCurrentUser()));
        
        //temporary fix - FIXME later
        //****************************
        
        User user = userRepository.findByUsername("alby3342").orElseThrow(()->new UsernameNotFoundException("Username not found - alby3342"));
        log.info("postRequest: {}",postRequest);
        Post postToSave=new Post();
        postToSave= postMapper.map(postRequest, subreddit, user);
        postToSave.setUser(user);
        postToSave.setSubreddit(subreddit);
        log.info("postToSave: {}",postToSave);
        
        //****************************
        
        //postRepository.save(postMapper.map(postRequest, subreddit, user));
        postRepository.save(postToSave);
    }

    @Transactional(readOnly = true)
    public PostResponse getPost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(id.toString()));
        return postMapper.mapToDto(post);
    }

    @Transactional(readOnly = true)
    public List<PostResponse> getAllPosts() {
        return postRepository.findAll()
                .stream()
                .map(postMapper::mapToDto)
                .collect(toList());
    }

    @Transactional(readOnly = true)
    public List<PostResponse> getPostsBySubreddit(Long subredditId) {
        Subreddit subreddit = subredditRepository.findById(subredditId)
                .orElseThrow(() -> new SubredditNotFoundException(subredditId.toString()));
        List<Post> posts = postRepository.findAllBySubreddit(subreddit);
        return posts.stream().map(postMapper::mapToDto).collect(toList());
    }

    @Transactional(readOnly = true)
    public List<PostResponse> getPostsByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));
        return postRepository.findByUser(user)
                .stream()
                .map(postMapper::mapToDto)
                .collect(toList());
    }
}
