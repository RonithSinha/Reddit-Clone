package clone.reddit.service;

import static java.util.stream.Collectors.toList;

import java.util.List;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import clone.reddit.dto.CommentsDto;
import clone.reddit.exception.PostNotFoundException;
import clone.reddit.exceptions.SpringRedditException;
import clone.reddit.mapper.CommentMapper;
import clone.reddit.model.Comment;
import clone.reddit.model.NotificationEmail;
import clone.reddit.model.Post;
import clone.reddit.model.User;
import clone.reddit.repository.CommentRepository;
import clone.reddit.repository.PostRepository;
import clone.reddit.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class CommentService {
	
	private static final String POST_URL="";
	private final CommentMapper commentMapper;
	private final PostRepository postRepository;
	private final AuthService authService;
    private final MailContentBuilder mailContentBuilder;
    private final MailService mailService;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    
	@Transactional
	public void save(CommentsDto commentsDto) {
		Post post = postRepository.findById(commentsDto.getPostId())
                .orElseThrow(() -> new PostNotFoundException(commentsDto.getPostId().toString()));
		
        //temporary fix - FIXME later
        //****************************
        		
		User user = userRepository.findByUsername("alby3342").orElseThrow(()->new UsernameNotFoundException("Username not found - alby3342"));
        log.info("commentsDto: {}",commentsDto);
        Comment commentToSave=new Comment();
        commentToSave= commentMapper.map(commentsDto, post, user);
        log.info("commentToSave: {}",commentToSave);
        commentToSave.setUser(user);
        commentToSave.setPost(post);
        log.info("commentToSave: {}",commentToSave);
        
        //****************************
        
        //Comment comment=commentMapper.map(commentsDto,post,authService.getCurrentUser());
        commentRepository.save(commentToSave);
		
		String message = mailContentBuilder.build(post.getUser().getUsername() + " posted a comment on your post." + POST_URL);
        sendCommentNotification(message, post.getUser());
	}
	
	private void sendCommentNotification(String message, User user) {
        mailService.sendMail(new NotificationEmail(user.getUsername() + " Commented on your post", user.getEmail(), message));
    }

    public List<CommentsDto> getAllCommentsForPost(Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new PostNotFoundException(postId.toString()));
        return commentRepository.findByPost(post)
                .stream()
                .map(commentMapper::mapToDto).collect(toList());
    }

    public List<CommentsDto> getAllCommentsForUser(String userName) {
        User user = userRepository.findByUsername(userName)
                .orElseThrow(() -> new UsernameNotFoundException(userName));
        return commentRepository.findAllByUser(user)
                .stream()
                .map(commentMapper::mapToDto)
                .collect(toList());
    }

    public boolean containsSwearWords(String comment) {
        if (comment.contains("shit")) {
            throw new SpringRedditException("Comments contains unacceptable language");
        }
        return false;
    }	
}
