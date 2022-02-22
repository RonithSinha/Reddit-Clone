package clone.reddit.mapper;

import static clone.reddit.model.VoteType.DOWNVOTE;
import static clone.reddit.model.VoteType.UPVOTE;

import java.util.Date;
import java.util.Optional;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.ocpsoft.prettytime.PrettyTime;
import org.springframework.beans.factory.annotation.Autowired;

import clone.reddit.dto.PostRequest;
import clone.reddit.dto.PostResponse;
import clone.reddit.model.Post;
import clone.reddit.model.Subreddit;
import clone.reddit.model.User;
import clone.reddit.model.Vote;
import clone.reddit.model.VoteType;
import clone.reddit.repository.CommentRepository;
import clone.reddit.repository.VoteRepository;
import clone.reddit.service.AuthService;
import lombok.extern.slf4j.Slf4j;

@Mapper(componentModel = "spring")
@Slf4j
public abstract class PostMapper {

    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private VoteRepository voteRepository;
    @Autowired
    private AuthService authService;


    @Mapping(target = "createdDate", expression = "java(java.time.Instant.now())")
    @Mapping(target = "description", source = "postRequest.description") //because Subreddit and PostRequest both contain a field named "description"
    public abstract Post map(PostRequest postRequest, Subreddit subreddit, User user);

    @Mapping(target = "id", source = "postId")
    @Mapping(target = "subredditName", source = "subreddit.name") //"subreddit" in "subreddit.name" refers to the subreddit field inside Post entity.
    @Mapping(target = "userName", source = "user.username") //"userName" in "user.username" refers to the user field inside Post entity.
    @Mapping(target = "commentCount", expression = "java(commentCount(post))")
    @Mapping(target = "duration", expression = "java(getDuration(post))")
    @Mapping(target = "upVote", expression = "java(isPostUpVoted(post))")
    @Mapping(target = "downVote", expression = "java(isPostDownVoted(post))")
    public abstract PostResponse mapToDto(Post post);

    Integer commentCount(Post post) {
        return commentRepository.findByPost(post).size();
    }

    String getDuration(Post post){
//    	String sDate1="05/12/2021";  
//        Date date1=new SimpleDateFormat("dd/MM/yyyy").parse(sDate1);
//        String sDate2="05/12/2022";  
//        Date date2=new SimpleDateFormat("dd/MM/yyyy").parse(sDate2);
//    	log.info("date1: "+p.format(date1));
//    	log.info("date2: "+p.format(date2));
    	Date myDate = Date.from(post.getCreatedDate());
        PrettyTime p = new PrettyTime();
    	log.info("current: "+p.format(new Date()));
    	log.info("post.getCreatedDate(): "+ post.getCreatedDate());
    	log.info("myDate: "+p.format(myDate));
    	
    	//return TimeAgo.using(post.getCreatedDate().toEpochMilli());
    	
    	return p.format(myDate);
    }

    boolean isPostUpVoted(Post post) {
        return checkVoteType(post, UPVOTE);
    }

    boolean isPostDownVoted(Post post) {
        return checkVoteType(post, DOWNVOTE);
    }

    private boolean checkVoteType(Post post, VoteType voteType) {
        if (authService.isLoggedIn()) {
            Optional<Vote> voteForPostByUser =
                    voteRepository.findTopByPostAndUserOrderByVoteIdDesc(post,
                            authService.getCurrentUser());
            return voteForPostByUser.filter(vote -> vote.getVoteType().equals(voteType))
                    .isPresent();
        }
        return false;
    }

}