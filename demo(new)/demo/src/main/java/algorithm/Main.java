package algorithm;

import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

public class Main {
	public static void main(String[] args) {
		try {
			Scanner scanner = new Scanner(System.in);
			System.out.print("Enter a search keyword: ");
			String keyword = scanner.nextLine();
			scanner.close();

			// Query Google for search results
			GoogleQuery googleQuery = new GoogleQuery(keyword);
			HashMap<String, String> searchResults = googleQuery.query();

			// Rank the search results
			Ranking ranking = new Ranking(keyword);
			HashMap<String, Integer> rankedResults = ranking.rank(searchResults);

			// Print ranked results
			System.out.println("Ranked Results:");
			searchResults.forEach((title, count) -> {
				System.out.println("Title: " + title + " | Count: " + count);
			});

		} catch (IOException e) {
			System.err.println("Error: " + e.getMessage());
		}
	}
}
