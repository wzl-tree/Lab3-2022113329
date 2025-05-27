package software.lab1; // Must match the package of WordGraph

import org.junit.jupiter.api.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import static org.junit.jupiter.api.Assertions.*;


// SystemOutCapture.java
class SystemOutCapture implements BeforeEachCallback, AfterEachCallback, ParameterResolver {
  private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
  private final PrintStream originalOut = System.out;

  @Override
  public void beforeEach(ExtensionContext context) {
    System.setOut(new PrintStream(outputStream));
  }

  // New methods for ParameterResolver
  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws
      ParameterResolutionException {
    // This extension supports parameters of type SystemOutCapture
    return parameterContext.getParameter().getType() == SystemOutCapture.class;
  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    // Return this instance of SystemOutCapture
    return this;
  }

  @Override
  public void afterEach(ExtensionContext context) {
    System.setOut(originalOut);
    try {
      outputStream.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  String getOutput() {
    return outputStream.toString().trim();
  }
}

// WordGraphQueryBridgeWordsTest.java
class WordGraphQueryBridgeWordsTest {
  private WordGraph wordGraph;
  private Path tempFile;

  @BeforeEach
  void setUp() throws IOException {
    wordGraph = new WordGraph();
    // Setup common graph: "seek new life and new civilization"
    tempFile = Files.createTempFile("bridge_words_graph", ".txt");
    Files.writeString(tempFile, "To explore strange new worlds,\n" +
        "To seek out new life and new civilizations and ...");
    wordGraph.buildGraphFromFile(tempFile.toString());
  }

  @AfterEach
  void tearDown() throws IOException {
    Files.deleteIfExists(tempFile);
  }

  private void rebuildGraph(String content) throws IOException {
    Files.writeString(tempFile, content);
    wordGraph = new WordGraph(); // Re-initialize to clear previous graph
    wordGraph.buildGraphFromFile(tempFile.toString());
  }

  @Test
  @DisplayName("TC-QBW-001: 存在一个桥接词")
  void testQueryBridgeWords_OneBridgeWord() {
    assertEquals("The bridge words from \"explore\" to \"new\" is: \"strange\".", wordGraph.queryBridgeWords("explore", "new"));
  }

  @Test
  @DisplayName("TC-QBW-002: 存在多个桥接词")
  void testQueryBridgeWords_MultipleBridgeWords() throws IOException {
    String result = wordGraph.queryBridgeWords("new", "and");
    // Order might vary, check for both possible results
    assertTrue(result.equals("The bridge words from \"new\" to \"and\" are: \"life\" and \"civilizations\".") ||
        result.equals("The bridge words from \"new\" to \"and\" are: \"civilizations\" and \"life\"."));
  }

  @Test
  @DisplayName("TC-QBW-003: 不存在桥接词")
  void testQueryBridgeWords_NoBridgeWords() {
    assertEquals("No bridge words from \"seek\" to \"to\"!", wordGraph.queryBridgeWords("seek", "to"));
  }

  @Test
  @DisplayName("TC-QBW-004: 输入单词大小写不敏感")
  void testQueryBridgeWords_CaseInsensitive() {
    assertEquals("The bridge words from \"explore\" to \"new\" is: \"strange\".", wordGraph.queryBridgeWords("Explore", "New"));
  }

  @Test
  @DisplayName("TC-QBW-005: `word1` 不在图中")
  void testQueryBridgeWords_Word1NotInGraph() {
    assertEquals("No \"unknown\" in the graph!", wordGraph.queryBridgeWords("unknown", "life"));
  }

  @Test
  @DisplayName("TC-QBW-006: `word2` 不在图中")
  void testQueryBridgeWords_Word2NotInGraph() {
    assertEquals("No \"unknown\" in the graph!", wordGraph.queryBridgeWords("seek", "unknown"));
  }

  @Test
  @DisplayName("TC-QBW-007: 输入单词：空字符串")
  void testQueryBridgeWords_EmptyWord() {
    // Assuming empty string is treated as not in graph
    assertEquals("No \"\" in the graph!", wordGraph.queryBridgeWords("", "life"));
  }

  @Test
  @DisplayName("TC-QBW-008: 输入单词：`null`")
  void testQueryBridgeWords_NullWord() {
    assertThrows(NullPointerException.class, () -> wordGraph.queryBridgeWords(null, "life"));
    assertThrows(NullPointerException.class, () -> wordGraph.queryBridgeWords("seek", null));
  }
}
