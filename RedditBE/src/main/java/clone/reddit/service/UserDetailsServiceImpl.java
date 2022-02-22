package clone.reddit.service;

import static java.util.Collections.singletonList;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import clone.reddit.exceptions.SpringRedditException;
import clone.reddit.model.User;
import clone.reddit.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService{

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username){
    	log.info("loadUserByUsername(): "+username);
	    User user= userRepository.findByUsername(username)
	        .orElseThrow(()->new SpringRedditException("A user with the username "+username+ " does not exist!"));
	    log.info("{} {} {}", user.getUsername(),user.getEmail(),user.getPassword());
	    return new org.springframework.security
	            .core.userdetails.User(user.getUsername(), user.getPassword(),
	            user.isEnabled(), true, true,
	            true, getAuthorities("USER"));
    }

    private Collection<? extends GrantedAuthority> getAuthorities(String role) {
        return singletonList(new SimpleGrantedAuthority(role));
    }


}
