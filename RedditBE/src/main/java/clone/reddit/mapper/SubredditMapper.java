package clone.reddit.mapper;

import java.util.List;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import clone.reddit.dto.SubredditDto;
import clone.reddit.model.Post;
import clone.reddit.model.Subreddit;

@Mapper(componentModel = "spring")
public interface SubredditMapper {
	
	@Mapping(target = "numberOfPosts", expression = "java(mapPosts(subreddit.getPosts()))")
	SubredditDto mapSubredditToDto(Subreddit subreddit);
	
	default Integer mapPosts(List<Post> posts ) {
		return posts.size();
	}
	
	@InheritInverseConfiguration
	@Mapping(target = "posts",ignore = true)
	Subreddit mapDtoToSubreddit(SubredditDto subredditDto);
}
