package algorithm;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SearchController {

	@GetMapping("/")
	public String index() {
		return "index"; // Serve the HTML page
	}

	@CrossOrigin(origins = "http://localhost:8080")
	@GetMapping("/api/search")
	@ResponseBody
	public HashMap<String, Object> search(@RequestParam("keyword") String keyword) {
		GoogleQuery googleQuery = new GoogleQuery(keyword);
		Ranking ranking = new Ranking(keyword);

		try {
			// Query Google for search results
			HashMap<String, String> searchResults = googleQuery.query();

			// Rank the search results
			HashMap<String, Integer> rankedResults = ranking.rank(searchResults);

			// Prepare the sorted results (limit to 10 results)
			HashMap<String, String> sortedResults = new HashMap<>();
			rankedResults.keySet().stream().limit(10).forEach(title -> {
				String url = searchResults.get(title);
				if (searchResults.containsKey(title)) {
					sortedResults.put(title, url);
				} else {
					System.err.println("Warning: Title not found in searchResults: " + title);
				}
			});

			// Fetch related keywords from the Google page
			List<String> relatedKeywords = googleQuery.fetchRelatedKeywords();

			// Prepare the response with both search results and related keywords
			HashMap<String, Object> response = new HashMap<>();
			response.put("results", sortedResults);
			response.put("relatedKeywords", relatedKeywords);

			return response;

		} catch (IOException e) {
			System.err.println("Error querying Google or ranking results: " + e.getMessage());
			return new HashMap<>(); // Return empty on error
		}
	}
}
