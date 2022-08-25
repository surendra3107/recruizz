package com.bbytes.recruiz.auth.jwt;

import java.util.Date;
import java.util.Locale;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;

import com.bbytes.recruiz.enums.WebMode;
import com.bbytes.recruiz.enums.WebRequestMode;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.bbytes.recruiz.utils.StringUtils;
import com.bbytes.recruiz.utils.TokenUtils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public final class TokenHandler {

	private static Logger logger = LoggerFactory.getLogger(TokenHandler.class);

	// this secret key is used to sign user x-auth token header
	private final String secret;

	// this secret key is used to sign api token
	private final String apiSecret;

	private final boolean saasMode;
	
	private final int tokenValidity;

	public TokenHandler(String secret, String apiSecret, boolean saasMode, int tokenValidity) {
		this.secret = StringUtils.checkNotBlank(secret);
		this.apiSecret = StringUtils.checkNotBlank(apiSecret);
		this.saasMode = saasMode;
		this.tokenValidity =tokenValidity;
	}

	/**
	 * Create an api key for the given user
	 * 
	 * @param tokenDataHolder
	 * @return
	 */
	public String createApiTokenForUser(TokenDataHolder tokenDataHolder) {
		return generateJWTToken(apiSecret, tokenDataHolder, null);
	}

	/**
	 * Create a jwt token for the given user that is valid for next 24 hrs
	 * 
	 * @param tokenDataHolder
	 * @return
	 */
	public String createJWTStringTokenForUser(TokenDataHolder tokenDataHolder) {
		return generateJWTToken(secret, tokenDataHolder, Integer.valueOf(tokenValidity));
	}

	/**
	 * Create a External access token for the given user that is valid for next
	 * tokenValidityInHrs hrs. Base 64 encoded token is returned
	 * 
	 * @param tokenDataHolder
	 * @param tokenValidityInHrs
	 * @return
	 */
	public String createJWTStringTokenForUser(TokenDataHolder tokenDataHolder, Integer tokenValidityInHrs) {
		return generateJWTToken(secret, tokenDataHolder, tokenValidityInHrs);
	}

	private String generateJWTToken(String secret, TokenDataHolder tokenDataHolder, Integer tokenValidityInHrs) {
		String timeZoneId = null;
		String locale = null;
		// checking it before because during multi tenant login it comes null
		// for the first time
		if (tokenDataHolder.getUserTimeZone() != null)
			timeZoneId = tokenDataHolder.getUserTimeZone().getID();
		if (tokenDataHolder.getUserLocale() != null)
			locale = tokenDataHolder.getUserLocale().getLanguage();

		Claims claims = Jwts.claims().setSubject(tokenDataHolder.getUser().getUsername());
		claims.put(GlobalConstants.HEADER_TENANT_ID, tokenDataHolder.getTenantId());
		claims.put(GlobalConstants.WEB_MODE, tokenDataHolder.getWebMode());
		claims.put(GlobalConstants.WEB_REQUEEST_MODE, tokenDataHolder.getWebRequestMode());
		claims.put(GlobalConstants.TOKEN_CREATION_TIME, tokenDataHolder.getCreationTime());
		claims.put(GlobalConstants.TIME_ZONE, timeZoneId);
		claims.put(GlobalConstants.USER_LOCALE, locale);
		// user can have only one role so directly fetch it
		String role = tokenDataHolder.getUser().getAuthorities().iterator().next().getAuthority();
		claims.put(GlobalConstants.USER_ROLE, role);

		Date expiration = null;
		if (tokenValidityInHrs != null)
			expiration = DateTime.now().plusHours(tokenValidityInHrs).toDate();

		String token = Jwts.builder().setClaims(claims).signWith(SignatureAlgorithm.HS256, secret).setExpiration(expiration).compact();
		logger.debug(String.format("JWT Token with username %s is created successfully", tokenDataHolder.getUser().getUsername()));
		return TokenUtils.encode(token);
	}

	/**
	 * Parse the jwt token and extract the token data ie user and tenant id
	 * 
	 * @param jwtStringToken
	 * @return
	 * @throws AuthenticationServiceException
	 */
	public TokenDataHolder parseAPITokenForUser(String jwtStringToken) throws AuthenticationServiceException {
		TokenDataHolder tokenDataHolder = parseJWTToken(apiSecret, jwtStringToken);
		return tokenDataHolder;
	}

	/**
	 * Parse the jwt token and extract the token data ie user and tenant id
	 * 
	 * @param jwtStringToken
	 * @return
	 * @throws AuthenticationServiceException
	 */
	public TokenDataHolder parseJWTStringTokenForUser(String jwtStringToken) throws AuthenticationServiceException {
		return parseJWTToken(secret, jwtStringToken);
	}

	private TokenDataHolder parseJWTToken(String secret, String jwtStringToken) {
		try {

			String token = TokenUtils.decode(jwtStringToken);

			Claims body = Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();

			String email = body.getSubject();
			String tenantId = (String) body.get(GlobalConstants.HEADER_TENANT_ID);
			String role = (String) body.get(GlobalConstants.USER_ROLE);
			String webMode = (String) body.get(GlobalConstants.WEB_MODE);
			String webRequestMode = (String) body.get(GlobalConstants.WEB_REQUEEST_MODE);

			// it will be null for old token - migration fix
			// it is usually null when we use api token
			// can be removed after few months of usage
			if (webRequestMode == null)
				webRequestMode = WebRequestMode.API.toString();

			DateTimeZone userTimeZone = DateTimeZone.forID((String) body.get(GlobalConstants.TIME_ZONE));
			Locale userLocale = Locale.ENGLISH;
			if (body.get(GlobalConstants.USER_LOCALE) != null)
				userLocale = new Locale((String) body.get(GlobalConstants.USER_LOCALE));
			// need to add expired , account locked etc to
			if (email != null && role != null && !role.trim().isEmpty()) {
				// if saas mode then auth token should have tenant info
				if (saasMode) {
					if (tenantId == null || tenantId.trim().isEmpty())
						throw new JwtException("Auth Token not valid, missing key values");
				}

				User userDetail = new User(email, "N/A", AuthorityUtils.createAuthorityList(role));
				TokenDataHolder tokenDataHolder = new TokenDataHolder(userDetail, tenantId, WebMode.valueOf(webMode),
						WebRequestMode.valueOf(webRequestMode), userTimeZone, userLocale);
				return tokenDataHolder;
			}

			throw new JwtException("Auth Token not valid, missing key values");

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new AuthenticationServiceException("Auth Token not valid or expired or tampered");
		}
	}

	public boolean isJWTPassKeyTokenValid(String jwtStringToken, String emailToVerify) {

		try {
			String token = TokenUtils.decode(jwtStringToken);
			Claims body = Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();

			String email = body.getSubject();
			// need to add expired , account locked etc to
			if (email != null && emailToVerify.equalsIgnoreCase(email)) {
				return true;
			}
		} catch (Exception e) {
			return false;
		}

		return false;
	}

}
