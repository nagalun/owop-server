package me.andreww7985.owopserver.helper;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpRequestHelper {
	public interface RequestCallback {
		public void done(final String data, final HttpURLConnection conn);

		public void error(final Exception e);
	}

	private static final ExecutorService exec = Executors.newSingleThreadExecutor();

	public static void doRequest(final String url, final String query, final RequestCallback callback) {
		exec.submit(new Runnable() {
			public void run() {
				try {
					URLConnection c = new URL(url + "?" + query).openConnection();
					c.setConnectTimeout(5000);
					c.setUseCaches(false);
					try (Scanner scanner = new Scanner(c.getInputStream())) {
						callback.done(scanner.useDelimiter("\\A").next(), (HttpURLConnection) c);
					}
				} catch (final IOException e) {
					callback.error(e);
				}
			}
		});
	}
}
