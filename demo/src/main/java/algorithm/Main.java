package algorithm;

import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try {
            // Prompt the user to enter a search keyword directly
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter a search keyword: ");
            String keyword = scanner.nextLine();
            scanner.close();
            
            // Perform the Google query with the provided keyword
            GoogleQuery googleQuery = new GoogleQuery(keyword);
            System.out.println(googleQuery.query());
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
