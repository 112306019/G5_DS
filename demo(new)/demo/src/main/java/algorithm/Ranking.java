package algorithm;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Ranking {
    private final String keyword;

    public Ranking(String keyword) {
        this.keyword = keyword.toLowerCase();
    }

    private int countKeywordOccurrences(String content) {
        String lowerContent = content.toLowerCase();
        return lowerContent.split("\\b" + keyword + "\\b").length - 1;
    }

    public HashMap<String, Integer> rank(HashMap<String, String> searchResults) {
        HashMap<String, Integer> rankedResults = new HashMap<>();

        for (Map.Entry<String, String> entry : searchResults.entrySet()) {
            String title = entry.getKey();
            String url = entry.getValue();
            try {
                System.out.println("Fetching URL for ranking: " + url);
                Document doc = Jsoup.connect(url).userAgent("Mozilla/5.0").timeout(10000).get();
                String content = doc.text();
                int count = countKeywordOccurrences(content);
                rankedResults.put(title, count);
            } catch (IOException e) {
                System.err.println("Error fetching or ranking URL: " + url + " - " + e.getMessage());
                rankedResults.put(title, 0);
            }
        }

        return rankedResults.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue())) // Descending by count
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue,
                        LinkedHashMap::new));
    }
}
