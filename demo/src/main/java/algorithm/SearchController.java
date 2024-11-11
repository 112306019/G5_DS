package algorithm;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;

@Controller
public class SearchController {

    @GetMapping("/")
    public String index() {
        return "index"; // Serve the HTML page
    }

    @CrossOrigin(origins = "http://localhost:8080") 
    @GetMapping("/api/search")
    @ResponseBody
    public HashMap<String, String> search(@RequestParam("keyword") String keyword) {
        GoogleQuery googleQuery = new GoogleQuery(keyword);
        try {
            HashMap<String, String> results = googleQuery.query();
            if (results.isEmpty()) {
                System.out.println("No results found for keyword: " + keyword);
            }
            return results; // Return search results
        } catch (IOException e) {
            System.err.println("Error querying Google: " + e.getMessage());
            return new HashMap<>(); // Return empty on error
        }
    }
}
