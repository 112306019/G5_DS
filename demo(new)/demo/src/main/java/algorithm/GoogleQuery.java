package algorithm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
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
            System.err.println("Error encoding search keyword: " + e.getMessage());
        }
    }

    private String fetchContent() throws IOException {
        StringBuilder retVal = new StringBuilder();

        URL u = new URL(url);
        URLConnection conn = u.openConnection();
        conn.setRequestProperty("User-agent", "Chrome/107.0.5304.107");
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
        return url.contains(".gov") || url.contains(".edu") || url.contains(".org");
    }

    public HashMap<String, String> query() throws IOException {
        if (content == null) {
            content = fetchContent();
        }

        HashMap<String, String> retVal = new HashMap<>();
        Document doc = Jsoup.parse(content);
        Elements lis = doc.select("div").select(".kCrYT");

        int resultCount = 0;
        for (Element li : lis) {
            if (resultCount >= 20) {
                break;
            }
            try {
                String citeUrl = li.select("a").get(0).attr("href").replace("/url?q=", "").split("&")[0];
                String title = li.select("a").get(0).select(".vvjwJb").text();

                if (!title.isEmpty() && isRelevantSite(citeUrl)) {
                    System.out.println("Title: " + title + " , url: " + citeUrl);
                    retVal.put(title, citeUrl);
                    resultCount++;
                }
            } catch (IndexOutOfBoundsException e) {
                System.err.println("Error parsing search result: " + e.getMessage());
            }
        }

        return retVal.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue,
                        LinkedHashMap::new));
    }
}
