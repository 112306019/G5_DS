package algorithm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class GoogleQuery {
	public String searchKeyword;
	public String url;
	public String content;

	public GoogleQuery(String searchKeyword) {
		this.searchKeyword = searchKeyword;
		try {
			String encodeKeyword = java.net.URLEncoder.encode(searchKeyword, "utf-8");
			this.url = "https://www.google.com/search?q=" + encodeKeyword + "&oe=utf8&num=20";
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	private String fetchContent() throws IOException {
		StringBuilder retVal = new StringBuilder();

		URL u = new URL(url);
		URLConnection conn = u.openConnection();
		conn.setRequestProperty("User-agent",
				"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/107.0.5304.107 Safari/537.36");

		InputStream in = conn.getInputStream();
		InputStreamReader inReader = new InputStreamReader(in, "utf-8");
		BufferedReader bufReader = new BufferedReader(inReader);
		String line;

		while ((line = bufReader.readLine()) != null) {
			retVal.append(line);
		}
		return retVal.toString();
	}

	private boolean isRelevantSite(String url) {
		return url.contains(".gov") || url.contains(".edu") || url.contains(".org") || url.contains(".com");
	}

	public HashMap<String, String> query() throws IOException {
		if (content == null) {
			content = fetchContent();
		}

		HashMap<String, String> retVal = new HashMap<>();
		Document doc = Jsoup.parse(content);

		Elements links = doc.select("div.tF2Cxc a");

		int resultCount = 0;
		for (Element link : links) {
			if (resultCount >= 20)
				break;
			try {
				String rawUrl = link.attr("href");
				String cleanedUrl = URLDecoder.decode(rawUrl, "UTF-8");
				String title = link.text();

				if (!title.isEmpty() && isRelevantSite(cleanedUrl)) {
					System.out.println("Title: " + title + " , URL: " + cleanedUrl);
					retVal.put(title, cleanedUrl);
					resultCount++;
				}
			} catch (Exception e) {

				System.err.println("Error parsing link: " + e.getMessage());
			}
		}

		return retVal.entrySet().stream().sorted(Map.Entry.comparingByKey()).collect(Collectors.toMap(Map.Entry::getKey,
				Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));
	}
}