package algorithm;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;

@Controller
public class SearchController {

    @GetMapping("/")
    public String index() {
        return "index"; // Serve the HTML page
    }

    @CrossOrigin(origins = "http://localhost:8080")
    @GetMapping("/api/search")
    @ResponseBody
    public HashMap<String, Integer> search(@RequestParam("keyword") String keyword) {
        GoogleQuery googleQuery = new GoogleQuery(keyword);
        Ranking ranking = new Ranking(keyword);

        try {
            // Query Google for search results
            HashMap<String, String> searchResults = googleQuery.query();

            // Rank the search results
            HashMap<String, Integer> rankedResults = ranking.rank(searchResults);

            if (rankedResults.isEmpty()) {
                System.out.println("No results found for keyword: " + keyword);
            }
            return rankedResults; // Return ranked results as JSON

        } catch (IOException e) {
            System.err.println("Error querying Google or ranking results: " + e.getMessage());
            return new HashMap<>(); // Return empty on error
        }
    }
}
