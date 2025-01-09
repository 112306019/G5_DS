package algorithm;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class GoogleQuery {
    private final String searchKeyword;
    private String url;
    private String content;

    public GoogleQuery(String searchKeyword) {
        this.searchKeyword = searchKeyword;
        try {
            String encodeKeyword = java.net.URLEncoder.encode(searchKeyword, "utf-8");
            this.url = "https://www.google.com/search?q=" + encodeKeyword + "&oe=utf8&num=20";
        } catch (Exception e) {
            System.err.println("Error encoding search keyword: " + e.getMessage());
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
        return (url.contains(".gov") || url.contains(".edu") || url.contains(".org"))
                && !url.contains("dictionary") && !url.contains("wikipedia")
                && !url.contains("translate") && !url.contains(".pdf") && !url.contains("wiktionary")&& !url.contains("dict.");
    }

    public HashMap<String, String> query() throws IOException {
        if (content == null) {
            content = fetchContent();
        }

        HashMap<String, String> retVal = new HashMap<>();
        Document doc = Jsoup.parse(content);

        Elements links = doc.select("div.tF2Cxc a");
        
        for (Element link : links) {
            // Limit results to 10

            try {
                String rawUrl = link.attr("href");
                String cleanedUrl = URLDecoder.decode(rawUrl, "UTF-8");
                String title = link.text();
                if (title.contains("http") || title.contains("www")) {
                    title = title.split("http")[0].trim();
                }
                if (!title.isEmpty() && isRelevantSite(cleanedUrl)) {
                    System.out.println("Title: " + title + " , URL: " + cleanedUrl);
                    retVal.put(title, cleanedUrl);
                }
            } catch (Exception e) {
                System.err.println("Error parsing link: " + e.getMessage());
            }
        }

        return retVal.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
    }

    public List<String> fetchRelatedKeywords() throws IOException {
        List<String> relatedKeywords = new ArrayList<>();
        HashMap<String, String> searchResults = query();

        ExecutorService executor = Executors.newFixedThreadPool(5);
        List<Future<List<String>>> futures = new ArrayList<>();

        for (String url : searchResults.values()) {
            futures.add(executor.submit(() -> fetchKeywordsFromUrl(url)));
        }

        for (Future<List<String>> future : futures) {
            try {
                List<String> keywords = future.get(1, TimeUnit.SECONDS);
                relatedKeywords.addAll(keywords);
            } catch (TimeoutException e) {
                System.err.println("Timeout fetching keywords.");
            } catch (Exception e) {
                System.err.println("Error fetching keywords: " + e.getMessage());
            }
        }

        
        return relatedKeywords.stream().distinct().limit(7).collect(Collectors.toList());
    }

    private List<String> fetchKeywordsFromUrl(String url) {
        List<String> relatedKeywords = new ArrayList<>();
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .timeout(1000) 
                    .get();

            Element body = doc.body(); // Only look at the body
            if (body == null) {
                body = doc;
            }

            Elements metaTags = body.select("meta[name=description], meta[name=keywords]");
            for (Element meta : metaTags) {
                String content = meta.attr("content");
                if (content.toLowerCase().contains(searchKeyword.toLowerCase())) {
                    extractKeywords(content, relatedKeywords);
                }
            }

            Elements headers = body.select("h1, h2, h3, h4, h5, h6");
            for (Element header : headers) {
                String text = header.text();
                if (text.toLowerCase().contains(searchKeyword.toLowerCase())) {
                    extractKeywords(text, relatedKeywords);
                }
            }

        } catch (IOException e) {
            System.err.println("Error fetching URL content: " + url + " - " + e.getMessage());
        }
        return relatedKeywords;
    }

    private void extractKeywords(String content, List<String> relatedKeywords) {
        String[] words = content.split("\\s+");
        for (int i = 0; i < words.length; i++) {
            if (words[i].toLowerCase().contains(searchKeyword.toLowerCase())) {
                int start = Math.max(0, i - 3);
                int end = Math.min(words.length, i + 4);
                String phrase = String.join(" ", Arrays.copyOfRange(words, start, end));
                if (!relatedKeywords.contains(phrase)) {
                    relatedKeywords.add(phrase.trim());
                }
            }
        }
    }
}
