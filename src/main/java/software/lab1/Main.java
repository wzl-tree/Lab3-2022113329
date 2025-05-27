package software.lab1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

/**
 * The main class for the Word Graph application.
 * This class serves as the entry point for the program, providing a command-line interface
 * for users to interact with graph operations such as building the graph,
 * querying bridge words, generating new text, calculating shortest paths,
 * computing PageRank, and performing interactive random walks.
 */
public class Main {

  /**
   * The main method that starts the Word Graph application.
   * It initializes the graph, builds it from a sample text file (or a specified file),
   * and then presents a menu of operations to the user, handling their choices.
   *
   * @param args Command line arguments (not used in this application).
   */
  public static void main(String[] args) {
    WordGraph graph = new WordGraph();
    Scanner scanner = new Scanner(System.in);
    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    System.out.print("Enter text file path: ");
    String filePath = scanner.nextLine();
    if (!graph.buildGraphFromFile(filePath)) {
      System.out.println("Failed to build graph. Exiting.");
      return;
    }

    int choice;
    do {
      System.out.println("\n--- Graph Operations Menu ---");
      System.out.println("1. Show Directed Graph");
      System.out.println("2. Query Bridge Words");
      System.out.println("3. Generate New Text");
      System.out.println("4. Calculate Shortest Path");
      System.out.println("5. Calculate PageRank");
      System.out.println("6. Random Walk");
      System.out.println("0. Exit");
      System.out.print("Enter your choice: ");
      choice = -1;
      try {
        choice = scanner.nextInt();
      } catch (Exception e) {
        scanner.nextLine(); // Consume newline
        System.out.println("Invalid choice. Please try again.");
        continue;
      }
      scanner.nextLine(); // Consume newline

      switch (choice) {
        case 1:
          graph.showDirectedGraph();
          break;
        case 2:
          System.out.print("Enter word1: ");
          String word1 = scanner.nextLine();
          System.out.print("Enter word2: ");
          String word2 = scanner.nextLine();
          System.out.println(graph.queryBridgeWords(word1, word2));
          break;
        case 3:
          System.out.print("Enter new text: ");
          String inputText = scanner.nextLine();
          System.out.println("Generated Text: " + graph.generateNewText(inputText));
          break;
        case 4:
          System.out.print("Enter start word: ");
          String startWord = scanner.nextLine();
          System.out.print("Enter end word: ");
          String endWord = scanner.nextLine();
          System.out.println(graph.calcShortestPath(startWord, endWord));
          break;
        case 5:
          System.out.print("Enter word to calculate PageRank for: ");
          String prWord = scanner.nextLine();
          Double prValue = graph.calPageRank(prWord);
          if (prValue != 0.0) { // Check for actual calculated value rather than error code 0.0
            System.out.printf("PageRank of \"%s\": %.4f%n", prWord, prValue);
          }
          break;
        case 6:
          performRandomWalkInteractive(graph, reader);
          break;
        case 0:
          System.out.println("Exiting program.");
          break;
        default:
          System.out.println("Invalid choice. Please try again.");
      }
    } while (choice != 0);

    scanner.close();
  }

  /**
   * Executes an interactive random walk with per-second output and user interruption.
   * This function encapsulates the logic previously in the main method's case 6.
   *
   * @param graph  The WordGraph instance to perform the walk on.
   * @param reader A BufferedReader for non-blocking user input (e.g., System.in).
   */
  public static void performRandomWalkInteractive(WordGraph graph, BufferedReader reader) {
    System.out.println("Starting random walk. Press ENTER at any time to stop.");
    final boolean[] stopRandomWalk = {false}; // Reset the stop flag

    // Thread to listen for user input (ENTER key)
    Thread inputListener = getInputListener(reader, stopRandomWalk);
    inputListener.start();

    List<String> path = List.of(graph.randomWalk(new Random()).split("\\s+"));

    StringBuilder outputBuilder = new StringBuilder("Random Walk Path: ");
    int count = 0;
    for (String wordInPath : path) {
      if (stopRandomWalk[0]) {
        break; // Stop if user pressed ENTER
      }
      System.out.print(wordInPath + " ");
      outputBuilder.append(wordInPath).append(" ");

      try {
        Thread.sleep(1000); // Pause for 1 second
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt(); // Restore the interrupted status
        System.out.println("\nRandom walk interrupted.");
        break;
      }
      count++;
      if (count % 10 == 0) { // Add a newline every 10 words for readability
        System.out.println();
      }
    }
    System.out.println("\nRandom walk finished or stopped.");
    // Save output to a file
    try (java.io.FileWriter fileWriter = new java.io.FileWriter("random_walk_output.txt")) {
      fileWriter.write(outputBuilder.toString().trim());
    } catch (IOException e) {
      System.out.println("Failed saving file: " + e);
    }
  }

  private static Thread getInputListener(BufferedReader reader, boolean[] stopRandomWalk) {
    Thread inputListener = new Thread(() -> {
      try {
        // Check if System.in has data without blocking
        while (!stopRandomWalk[0] && System.in.available() == 0) {
          Thread.sleep(100); // Small delay to avoid busy-waiting
        }
        if (System.in.available() > 0) {
          reader.readLine(); // Consume the input (the Enter key)
          stopRandomWalk[0] = true; // Set the flag to stop the walk
          System.out.println("\nStopping random walk...");
        }
      } catch (InterruptedException | IOException e) {
        // Handle exceptions, e.g., if the stream is closed
        System.err.println("Input listener error: " + e.getMessage());
      }
    });
    inputListener.setDaemon(true); // Set as daemon so it doesn't prevent JVM exit
    return inputListener;
  }

}