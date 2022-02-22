package clone.reddit.security;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtProvider {
    
	private final JwtEncoder jwtEncoder;

	@Value("${jwt.expiration.time}")
    private Long jwtExpirationInMillis;

	public String generateToken(Authentication authentication) {
		log.info("generateToken()");
		User principal = (User) authentication.getPrincipal();
        return generateTokenWithUserName(principal.getUsername());
	}

    public String generateTokenWithUserName(String username) {
    	log.info("generateTokenWithUserName()");
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusMillis(jwtExpirationInMillis))
                .subject(username)
                .claim("scope", "ROLE_USER")
                .build();
        return this.jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
    
    public Long getJwtExpirationInMillis() {
        return jwtExpirationInMillis;
    }

}