package software.lab1;

import java.io.File;
import java.io.FileNotFoundException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import org.apache.commons.io.FilenameUtils;

/**
 * Represents a directed graph where nodes are words and edges represent word sequences.
 * Edges have weights based on their frequency of appearance in the input text.
 * This class provides functionalities to build the graph, query bridge words,
 * generate new text, calculate the shortest paths, compute PageRank, and perform random walks.
 */
public class WordGraph {

  // Adjacency list representation: Map<sourceWord, Map<destinationWord, weight>>
  private final Map<String, Map<String, Integer>> adjList;
  final Set<String> allWordsInGraph; // To quickly check if a word exists in the graph

  /**
   * Constructs an empty WordGraph.
   * Initializes the adjacency list and the set of all words in the graph.
   */
  public WordGraph() {
    adjList = new HashMap<>();
    allWordsInGraph = new HashSet<>();
  }

  /**
   * Reads text from a file, cleans it, and builds the directed graph.
   * This method fulfills part of Functional Requirement 1.
   *
   * @param filePath The path to the input text file.
   * @return true if the graph was built successfully, false otherwise.
   */
  @SuppressWarnings("checkstyle:LambdaParameterName")
  public boolean buildGraphFromFile(String filePath) {
    try {
      File file = new File(FilenameUtils.getName(filePath));
      Scanner scanner = new Scanner(file);
      StringBuilder rawText = new StringBuilder();
      while (scanner.hasNextLine()) {
        rawText.append(scanner.nextLine()).append(" "); // Treat newline as space
      }
      scanner.close();

      // Clean the text: replace punctuation with spaces, ignore non-alphabetic chars
      // Convert to lowercase for case-insensitivity
      String cleanedText = rawText.toString().replaceAll("[^a-zA-Z]", " ").toLowerCase();
      String[] words = cleanedText.trim().split("\\s+"); // Split by one or more spaces

      if (words.length < 2) {
        System.out.println("Not enough words to build a graph.");
        return false;
      }

      // Add all unique words to the set
      allWordsInGraph.addAll(Arrays.asList(words));

      // Build the graph
      for (int i = 0; i < words.length - 1; i++) {
        String word1 = words[i];
        String word2 = words[i + 1];

        adjList.computeIfAbsent(word1, _ -> new HashMap<>())
            .merge(word2, 1, Integer::sum); // Increment weight if edge exists
      }
      return true;

    } catch (FileNotFoundException e) {
      System.err.println("File not found: " + filePath);
      return false;
    }
  }

  /**
   * Displays the generated directed graph in a clear, understandable format.
   * This fulfills Functional Requirement 2.
   */
  public void showDirectedGraph() {
    if (adjList.isEmpty()) {
      System.out.println("The graph is empty.");
      return;
    }

    System.out.println("Directed Graph Representation:");
    for (Map.Entry<String, Map<String, Integer>> entry : adjList.entrySet()) {
      String sourceWord = entry.getKey();
      Map<String, Integer> destinations = entry.getValue();
      System.out.print(sourceWord + " -> ");
      List<String> edges = new ArrayList<>();
      for (Map.Entry<String, Integer> destEntry : destinations.entrySet()) {
        edges.add(destEntry.getKey() + " (weight: " + destEntry.getValue() + ")");
      }
      System.out.println(String.join(", ", edges));
    }
  }

  /**
   * Queries bridge words between word1 and word2.
   * This fulfills Functional Requirement 3.
   *
   * @param word1 The first word.
   * @param word2 The second word.
   * @return A string containing the bridge words or an appropriate message.
   */
  public String queryBridgeWords(String word1, String word2) {
    word1 = word1.toLowerCase();
    word2 = word2.toLowerCase();

    if (!allWordsInGraph.contains(word1) || !allWordsInGraph.contains(word2)) {
      // Check if word1 or word2 is not in graph
      StringBuilder message = new StringBuilder("No ");
      if (!allWordsInGraph.contains(word1)) {
        message.append("\"").append(word1).append("\"");
      }
      if (!allWordsInGraph.contains(word1) && !allWordsInGraph.contains(word2)) {
        message.append(" and ");
      }
      if (!allWordsInGraph.contains(word2)) {
        message.append("\"").append(word2).append("\"");
      }
      message.append(" in the graph!");
      return message.toString();
    }

    List<String> bridgeWords = new ArrayList<>();
    Map<String, Integer> word1Successors = adjList.getOrDefault(word1, Collections.emptyMap());

    for (String bridgeCandidate : word1Successors.keySet()) {
      Map<String, Integer> bridgeCandidateSuccessors =
          adjList.getOrDefault(bridgeCandidate, Collections.emptyMap());
      if (bridgeCandidateSuccessors.containsKey(word2)) {
        bridgeWords.add(bridgeCandidate); // Found a bridge word
      }
    }

    if (bridgeWords.isEmpty()) {
      return "No bridge words from \"" + word1 + "\" to \"" + word2 + "\"!";
    } else {
      StringBuilder result = new StringBuilder("The bridge words from \"")
          .append(word1).append("\" to \"").append(word2).append("\" ");
      if (bridgeWords.size() == 1) {
        result.append("is: ");
      } else {
        result.append("are: ");
      }
      for (int i = 0; i < bridgeWords.size(); i++) {
        result.append("\"").append(bridgeWords.get(i)).append("\"");
        if (i < bridgeWords.size() - 2) {
          result.append(", ");
        } else if (i == bridgeWords.size() - 2) {
          result.append(" and ");
        }
      }
      result.append(".");
      return result.toString();
    }
  }

  /**
   * Generates new text by inserting bridge words.
   * This fulfills Functional Requirement 4.
   *
   * @param inputText The input text from the user.
   * @return The new text with bridge words inserted.
   */
  public String generateNewText(String inputText) {
    String cleanedInput = inputText.replaceAll("[^a-zA-Z]", " ");
    String[] words = cleanedInput.trim().split("\\s+");

    if (words.length <= 1) {
      return inputText; // No pairs to insert bridge words
    }

    StringBuilder newText = new StringBuilder();
    SecureRandom rand = new SecureRandom();

    for (int i = 0; i < words.length - 1; i++) {
      String word1 = words[i];
      String word2 = words[i + 1];
      String word1Lower = word1.toLowerCase();
      String word2Lower = word2.toLowerCase();
      newText.append(word1);

      // Find bridge words
      List<String> bridgeWords = new ArrayList<>();
      if (allWordsInGraph.contains(word1Lower) && allWordsInGraph.contains(word2Lower)) {
        Map<String, Integer> word1Successors =
            adjList.getOrDefault(word1Lower, Collections.emptyMap());
        for (String bridgeCandidate : word1Successors.keySet()) {
          Map<String, Integer> bridgeCandidateSuccessors =
              adjList.getOrDefault(bridgeCandidate, Collections.emptyMap());
          if (bridgeCandidateSuccessors.containsKey(word2Lower)) {
            bridgeWords.add(bridgeCandidate);
          }
        }
      }

      if (!bridgeWords.isEmpty()) {
        // Randomly select one bridge word
        String chosenBridgeWord = bridgeWords.get(rand.nextInt(bridgeWords.size()));
        newText.append(" ").append(chosenBridgeWord);
      }
      newText.append(" "); // Add space after the current word (and bridge word if inserted)
    }
    newText.append(words[words.length - 1]); // Append the last word

    return newText.toString().trim();
  }

  /**
   * Calculates the shortest path between two words using Dijkstra's algorithm.
   * This fulfills Functional Requirement 5.
   *
   * @param word1 The starting word.
   * @param word2 The ending word.
   * @return A string representing the shortest path and its length, or an appropriate message.
   */
  public String calcShortestPath(String word1, String word2) {
    word1 = word1.toLowerCase();
    word2 = word2.toLowerCase();

    if (!allWordsInGraph.contains(word1) || !allWordsInGraph.contains(word2)) {
      // Not explicitly in source, but implied by need for graph nodes
      return "One or both words are not in the graph.";
    }

    if (word1.equals(word2)) {
      return "The shortest path from \"" + word1 + "\" to \"" + word2 + "\" is: "
          + word1 + " (length: 0)";
    }

    Map<String, Integer> distances = new HashMap<>();
    final Map<String, String> predecessors = new HashMap<>();
    PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingInt(node -> node.distance));

    // Initialize distances
    for (String word : allWordsInGraph) {
      distances.put(word, Integer.MAX_VALUE);
    }
    distances.put(word1, 0);
    pq.add(new Node(word1, 0));

    while (!pq.isEmpty()) {
      Node current = pq.poll();

      if (current.distance > distances.get(current.word)) {
        continue;
      }

      // Get neighbors of current word
      Map<String, Integer> neighbors = adjList.getOrDefault(current.word, Collections.emptyMap());
      for (Map.Entry<String, Integer> neighborEntry : neighbors.entrySet()) {
        String neighborWord = neighborEntry.getKey();
        int edgeWeight = neighborEntry.getValue();

        int newDist = current.distance + edgeWeight;
        if (newDist < distances.getOrDefault(neighborWord, Integer.MAX_VALUE)) {
          distances.put(neighborWord, newDist);
          predecessors.put(neighborWord, current.word);
          pq.add(new Node(neighborWord, newDist));
        }
      }
    }

    // Reconstruct path
    if (distances.get(word2) == Integer.MAX_VALUE) {
      return "\"" + word1 + "\" and \"" + word2 + "\" are unreachable."; // If unreachable
    } else {
      List<String> path = new LinkedList<>();
      String current = word2;
      while (current != null) {
        path.addFirst(current); // Add to the beginning to reverse order
        current = predecessors.get(current);
      }
      return "The shortest path from \"" + word1 + "\" to \"" + word2 + "\" is: "
          + String.join(" -> ", path) + " (length: " + distances.get(word2) + ")";
    }
  }

  /**
   * Calculates the PageRank for a given word, using the formula
   * PR(u) = (1-d)/N + d * SUM(PR(v) / L(v)),
   * Nodes with zero outgoing edges (dangling nodes) distribute their PR equally to all nodes.
   *
   * @param word The word (node) for which to calculate PageRank.
   * @return The PageRank value of the specified word.
   */
  public Double calPageRank(String word) {
    final double damping = 0.85;
    final int max_iter = 100;
    final double epsilon = 0.01;
    final int n = allWordsInGraph.size(); // Get number of vertices

    if (word == null) {
      return -1.0;
    }

    word = word.toLowerCase();

    if (n == 0 || !allWordsInGraph.contains(word)) {
      return -1.0;
    }

    // pr_current: Current PR values, initialized uniformly
    Map<String, Double> prCurrent = new HashMap<>();
    for (String node : allWordsInGraph) {
      prCurrent.put(node, 1.0 / n);
    }

    // out_weight_sums: Sum of outgoing edge weights for each node
    Map<String, Double> outWeightSums = new HashMap<>();
    // dangling_nodes: List of nodes with no outgoing edges (sum of weights is 0)
    Set<String> danglingNodes = new HashSet<>();

    // Preprocessing phase: Calculate sum of outgoing edge weights and identify dangling nodes
    for (String u : allWordsInGraph) {
      Map<String, Integer> successorEdges = adjList.getOrDefault(u, Collections.emptyMap());
      double sumOfWeights;
      // Using Stream API for sum
      sumOfWeights = successorEdges.values().stream().mapToDouble(Integer::doubleValue).sum();

      if (sumOfWeights == 0) {
        // Node with no outgoing edges (sum of weights is 0) is a dangling node
        danglingNodes.add(u);
      }
      outWeightSums.put(u, sumOfWeights);
    }

    // Pre-calculate constant term for the PageRank formula: (1 - d) / N
    final double constTerm = (1.0 - damping) / n;
    Map<String, Double> prNext = new HashMap<>(); // Next iteration's PR values

    // PageRank Iteration Loop
    for (int iter = 0; iter < max_iter; ++iter) {
      // Initialize prNext with the constant term for all nodes
      for (String node : allWordsInGraph) {
        prNext.put(node, constTerm);
      }

      // First part: Distribute PageRank from non-dangling nodes
      for (String u : allWordsInGraph) {
        // Skip dangling nodes as their contribution is handled separately
        if (outWeightSums.getOrDefault(u, 0.0) == 0.0) {
          continue;
        }

        // Calculate the factor to distribute from node u
        // This is PR(u) * damping / sum_of_out_weights(u)
        final double factor = damping * prCurrent.getOrDefault(u, 0.0) / outWeightSums.get(u);

        // Distribute the factor to all successor nodes v based on edge weights
        Map<String, Integer> successors = adjList.getOrDefault(u, Collections.emptyMap());
        for (Map.Entry<String, Integer> edge : successors.entrySet()) {
          String v = edge.getKey();
          int edgeWeight = edge.getValue();
          // Add weighted contribution to successor's PR
          prNext.merge(v, factor * edgeWeight,
              Double::sum);
        }
      }

      // Second part: Handle contribution from dangling nodes
      //  distribute their entire PR equally among all nodes
      if (!danglingNodes.isEmpty()) {
        double danglingSum = 0.0;
        // Sum the PR of all dangling nodes
        for (String u : danglingNodes) {
          danglingSum += prCurrent.getOrDefault(u, 0.0);
        }
        // Calculate the contribution from all dangling nodes to each node
        final double danglingContrib = damping * danglingSum / n;

        // Add this contribution to every node's PR
        for (String v : allWordsInGraph) {
          prNext.merge(v, danglingContrib,
              Double::sum); // Equivalent to pr_next[v] += dangling_contrib;
        }
      }

      // Calculate the total difference between the current and next PR vectors
      double diff = 0.0;
      for (String node : allWordsInGraph) {
        diff += Math.abs(prNext.getOrDefault(node, 0.0) - prCurrent.getOrDefault(node, 0.0));
      }

      // Check for convergence
      if (diff < epsilon) {
        break; // Stop iterating if the change is below the threshold
      }

      // Prepare for the next iteration: Swap prCurrent and prNext (by reassigning references)
      prCurrent = prNext; // This effectively makes prNext the new prCurrent for the next iteration
      prNext = new HashMap<>(); // Re-initialize prNext for the next iteration
    }

    // Return the final converged (or max_iter reached) PageRank values
    return prCurrent.get(word);
  }

  /**
   * Performs a random walk on the graph.
   * This method now returns the full list of traversed words, to be managed by main for output.
   *
   * @return A List of words representing the traversed path.
   */
  public String randomWalk(Random rand) {
    List<String> traversedPath = new ArrayList<>();
    if (allWordsInGraph.isEmpty()) {
      return ""; // Return empty list for empty graph
    }

    List<String> nodesList = new ArrayList<>(allWordsInGraph);
    String currentWord = nodesList.get(rand.nextInt(nodesList.size())); // Random starting node
    traversedPath.add(currentWord);

    Set<String> visitedEdges = new HashSet<>(); // Keep track of visited edges to detect repetition


    while (true) {
      Map<String, Integer> possibleNextWords =
          adjList.getOrDefault(currentWord, Collections.emptyMap());

      if (possibleNextWords.isEmpty()) {
        break; // No outgoing edges from current node
      }

      // Select next word based on edge weights
      List<String> weightedNextWords = new ArrayList<>();
      for (Map.Entry<String, Integer> entry : possibleNextWords.entrySet()) {
        String destWord = entry.getKey();
        int weight = entry.getValue();
        for (int i = 0; i < weight; i++) {
          weightedNextWords.add(destWord);
        }
      }

      String nextWord = weightedNextWords.get(rand.nextInt(weightedNextWords.size()));

      String edge = currentWord + "->" + nextWord;
      if (visitedEdges.contains(edge)) {
        break; // Repeated edge encountered
      }

      visitedEdges.add(edge);
      traversedPath.add(nextWord);
      currentWord = nextWord;
    }
    return String.join(" ", traversedPath);
  }

  // Helper class for Dijkstra's algorithm
  private static class Node {
    String word;
    int distance;

    public Node(String word, int distance) {
      this.word = word;
      this.distance = distance;
    }
  }
}