package com.bbytes.recruiz.people.service;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Service;

/**
 * Access profile pic service https://en.gravatar.com/ to get user profile image
 * using the email
 */
@Service
public class GravatarProfileService {

	private final static int DEFAULT_SIZE = 60;
	private final static String GRAVATAR_URL = "https://s.gravatar.com/avatar/";

	/**
	 * Get the gravatar url for given email with rating as GENERAL_AUDIENCES g
	 * and HTTP_404 404 as default image
	 * 
	 * @param email
	 * @return
	 * @throws Exception
	 */
	public String getAvatarURL(String email) {
		Validate.notNull(email, "email");

		String emailHash = DigestUtils.md5Hex(email.toLowerCase().trim());
		String params = formatUrlParameters();
		String url = GRAVATAR_URL + emailHash + params;
		return url;
	}

	/**
	 * Downloads the gravatar for the given URL using Java {@link URL} and
	 * returns a byte array containing the gravatar jpg, returns null if no
	 * gravatar was found.
	 */
	public byte[] getAvatar(String email) {

		String avatarURL = getAvatarURL(email);

		InputStream stream = null;
		try {
			URL url = new URL(avatarURL);
			stream = url.openStream();
			return IOUtils.toByteArray(stream);
		} catch (Exception e) {
			return null;
		} finally {
			IOUtils.closeQuietly(stream);
		}
	}

	private String formatUrlParameters() {
		List<String> params = new ArrayList<String>();
		params.add("s=" + DEFAULT_SIZE);
		params.add("r=" + "g");
		params.add("d=" + "404");
		if (params.isEmpty())
			return "";
		else
			return "?" + StringUtils.join(params.iterator(), "&");
	}

}
