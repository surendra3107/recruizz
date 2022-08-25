package com.bbytes.recruiz.utils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;

public class NetworkUtils {

	public static boolean pingHost(String host, int port, int timeout) {
		try (Socket socket = new Socket()) {
			socket.connect(new InetSocketAddress(host, port), timeout);
			return true;
		} catch (IOException e) {
			return false; // Either timeout or unreachable or failed DNS lookup.
		}
	}

	public static boolean pingURL(String urlString) {

		try {
			URL url = new URL(urlString);
			String SERVER_ADDRESS = url.getHost();
			int TCP_SERVER_PORT = url.getPort();

			try (Socket s = new Socket(SERVER_ADDRESS, TCP_SERVER_PORT)) {
				return true;
			} catch (IOException ex) {
				/* ignore */
			}
			return false;

		} catch (MalformedURLException e) {
			/* ignore */
		}

		return false;
	}
}
