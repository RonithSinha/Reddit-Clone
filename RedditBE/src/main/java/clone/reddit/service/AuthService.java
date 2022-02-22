package clone.reddit.service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import clone.reddit.dto.AuthenticationResponse;
import clone.reddit.dto.LoginRequest;
import clone.reddit.dto.RefreshTokenRequest;
import clone.reddit.dto.RegisterRequest;
import clone.reddit.exceptions.SpringRedditException;
import clone.reddit.model.NotificationEmail;
import clone.reddit.model.User;
import clone.reddit.model.VerificationToken;
import clone.reddit.repository.UserRepository;
import clone.reddit.repository.VerificationTokenRepository;
import clone.reddit.security.JwtProvider;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class AuthService {
	
	private final PasswordEncoder passwordEncoder;
	private final UserRepository userRepository;
	private final VerificationTokenRepository verificationTokenRepository;
	private final MailService mailService;
	private final AuthenticationManager authenticationManager;
	private final JwtProvider jwtProvider;
	private final RefreshTokenService refreshTokenService;
	
	@Transactional
	public void signup(RegisterRequest registerRequest) {
		Optional<User> userNameCheck = userRepository.findByUsername(registerRequest.getUsername());
		Optional<User> emailCheck = userRepository.findByEmail(registerRequest.getEmail());
		if(userNameCheck.isPresent())	{
			throw new SpringRedditException("A user with the username "+ registerRequest.getUsername()+ " already exists!");
		}
		if(emailCheck.isPresent())	{
			throw new SpringRedditException("A user with the email "+ registerRequest.getEmail()+ " already exists!");
		}
		
		User user=new User();
		user.setUsername(registerRequest.getUsername());
		user.setEmail(registerRequest.getEmail());
		user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
		user.setCreated(Instant.now());
		user.setEnabled(false);
		userRepository.save(user);
		
		String token= generateVerificationToken(user);
		
		NotificationEmail notificationEmail=new NotificationEmail();
		notificationEmail.setSubject("Please Activate your account!");
		notificationEmail.setRecipient(user.getEmail());
		
		notificationEmail.setBody("Thank you for signing up to Spring Reddit, " +
                "please click on the below url to activate your account : " +
                "http://localhost:8080/api/auth/accountVerification/" + token);
		mailService.sendMail(notificationEmail);
	}

	private String generateVerificationToken(User user) {
		String token= UUID.randomUUID().toString();
		VerificationToken verificationToken=new VerificationToken();
		verificationToken.setToken(token);
		verificationToken.setUser(user);
		verificationTokenRepository.save(verificationToken);
		return token;
	}

	public void verifyAccount(String token) {
		VerificationToken verificationToken= verificationTokenRepository.findByToken(token).
				orElseThrow(()->new SpringRedditException("Invalid Token"));
		fetchUserAndEnable(verificationToken);
	}

	private void fetchUserAndEnable(VerificationToken verificationToken) {
		String username= verificationToken.getUser().getUsername();
		User user= userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User name not found - " + username));
		user.setEnabled(true);
		userRepository.save(user);
	}

	public AuthenticationResponse login(LoginRequest loginRequest) {
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
		SecurityContextHolder.getContext().setAuthentication(authentication);
		String token= jwtProvider.generateToken(authentication);
		return AuthenticationResponse.builder()
                .authenticationToken(token)
                .refreshToken(refreshTokenService.generateRefreshToken().getToken())
                .expiresAt(Instant.now().plusMillis(jwtProvider.getJwtExpirationInMillis()))
                .username(loginRequest.getUsername())
                .build();
	}
	
    public AuthenticationResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        refreshTokenService.validateRefreshToken(refreshTokenRequest.getRefreshToken());
        String token = jwtProvider.generateTokenWithUserName(refreshTokenRequest.getUsername());
        return AuthenticationResponse.builder()
                .authenticationToken(token)
                .refreshToken(refreshTokenRequest.getRefreshToken())
                .expiresAt(Instant.now().plusMillis(jwtProvider.getJwtExpirationInMillis()))
                .username(refreshTokenRequest.getUsername())
                .build();
    }

	public boolean isLoggedIn() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return !(authentication instanceof AnonymousAuthenticationToken) && authentication.isAuthenticated();
    }

	@Transactional(readOnly = true)
    public User getCurrentUser() {
		//Checking values for various parameters of SecurityContextHolder
//		log.info("getCurrentUser()"+ SecurityContextHolder.
//                getContext());
//		log.info("getCurrentUser()"+ SecurityContextHolder.
//                getContext().getAuthentication());
//		log.info("getCurrentUser()"+ SecurityContextHolder.
//                getContext().getAuthentication().getName());
//		log.info("getCurrentUser()"+ SecurityContextHolder.
//				getContext().getAuthentication().getCredentials());
//		log.info("getCurrentUser()"+ SecurityContextHolder.
//				getContext().getAuthentication().getDetails());
//		log.info("getCurrentUser()"+ SecurityContextHolder.
//				getContext().getAuthentication().getPrincipal());
//		log.info("getCurrentUser()"+ SecurityContextHolder.
//				getContext().getAuthentication().getAuthorities());
//		
		
//        org.springframework.security.core.userdetails.User principal = (org.springframework.security.core.userdetails.User) SecurityContextHolder.
//                getContext().getAuthentication().getPrincipal();
        String username=SecurityContextHolder.getContext().getAuthentication().getName();
//        return userRepository.findByUsername(principal.getUsername())
//                .orElseThrow(() -> new UsernameNotFoundException("User name not found - " + principal.getUsername()));
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User name not found - " + username));        
    }

}