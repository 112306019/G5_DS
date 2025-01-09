package algorithm;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Ranking {
	private final String keyword;

	public Ranking(String keyword) {
		this.keyword = keyword.toLowerCase();
	}

	private boolean isValidUrl(String url) {
		return url != null && url.matches("^(http|https)://.*");
	}

	private int countKeywordOccurrences(String content) {

		String lowerContent = content.toLowerCase();
		return lowerContent.split("\\b" + keyword + "\\b").length - 1;
	}

	private int countKeywordInSecondLayer(String url) {
		int totalOccurrences = 0;
		try {
			Document doc = Jsoup.connect(url).userAgent("Mozilla/5.0").timeout(1000).get();
			//Elements links = doc.select("a[href]"); // 提取所有超連結
			int linkCount = 0;
			Elements body = doc.select("body");
			Elements target = doc.select("main");
	        Elements links;

	        if (!target.isEmpty()) {
	            // Extract links only from the <body> section
	            links = target.select("a[href]");
	        } else if(!body.isEmpty()){
	            // Fallback: Extract all links from the document
	            links = body.select("a[href]");
	        }else {
	        	links = doc.select("a[href]");
	        }

			for (Element link : links) {
				if (linkCount >= 5)
					break; // 限制最多處理 10 個超連結
				String secondLayerUrl = link.absUrl("href");
				if (!isValidUrl(secondLayerUrl)) {
					System.err.println("Skipping invalid URL: " + secondLayerUrl);
					continue;
				}

				// 篩選出政府或組織網站
				if (isRelevantSite(secondLayerUrl)) {
					linkCount++;
					try {
						System.out.println("Fetching second-layer URL: " + secondLayerUrl);
						Document secondDoc = Jsoup.connect(secondLayerUrl).userAgent("Mozilla/5.0").timeout(1000)
								.get();
						String secondContent = secondDoc.text();
						totalOccurrences += countKeywordOccurrences(secondContent);
					} catch (IOException e) {
						System.err
								.println("Error fetching second-layer URL: " + secondLayerUrl + " - " + e.getMessage());
					}
				}
			}
		} catch (IOException e) {
			System.err.println(
					"Error fetching first-layer URL for second-layer extraction: " + url + " - " + e.getMessage());
		}
		return totalOccurrences;
	}

	private boolean isRelevantSite(String url) {
		return (url.contains(".gov") || url.contains(".edu") || url.contains(".org")) && !url.contains("dictionary")
				&& !url.contains("wikipedia") && !url.contains("translate") && !url.contains(".pdf")&& !url.contains("wiktionary")&& !url.contains("dict.");
	}

	public HashMap<String, Integer> rank(HashMap<String, String> searchResults) {
		HashMap<String, Integer> rankedResults = new HashMap<>();

		for (Map.Entry<String, String> entry : searchResults.entrySet()) {
			String title = entry.getKey();
			String url = entry.getValue();
			try {
				System.out.println("Fetching URL for ranking: " + url);
				Document doc = Jsoup.connect(url).userAgent("Mozilla/5.0").timeout(1000).get();
				String content = doc.text();

				// 計算第一層關鍵詞出現次數
				int count = countKeywordOccurrences(content);

				// 計算第二層關鍵詞出現次數
				count += countKeywordInSecondLayer(url);

				rankedResults.put(title, count);
			} catch (IOException e) {
				System.err.println("Error fetching or ranking URL: " + url + " - " + e.getMessage());
				rankedResults.put(title, 0);
			}
		}

		return rankedResults.entrySet().stream().sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue())) // Descending
																											// by count
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue,
						LinkedHashMap::new));

	}
}