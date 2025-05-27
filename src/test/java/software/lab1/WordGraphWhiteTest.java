package software.lab1; // TestRandomWalk.java (示例测试类)

import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

// 假设 WordGraph 和 WordGraph.java 在同一个包中
// import software.lab1.WordGraph; 

class TestRandomWalk {

  private WordGraph graph;
  private static final String TEST_FILE_PREFIX = "test_graph_";
  private static int fileCounter = 0;

  @BeforeEach
  void setUp() {
    graph = new WordGraph();
  }

  // 辅助方法：创建临时文件并写入内容
  private String createTempFile(String content) throws IOException {
    String fileName = TEST_FILE_PREFIX + (fileCounter++) + ".txt";
    Path filePath = Path.of(fileName);
    Files.writeString(filePath, content);
    return fileName;
  }

  // 辅助方法：删除临时文件
  private void deleteTempFile(String filePath) {
    try {
      Files.deleteIfExists(Path.of(filePath));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  // --- 基本路径测试用例 ---

  @Test
  void testRandomWalk_Path1_EmptyGraph() throws IOException {
    // 图是空的，不调用 buildGraphFromFile()
    String result = graph.randomWalk(new Random());
    assertEquals("", result, "Path 1: Empty graph should return an empty string.");
  }

  @Test
  void testRandomWalk_Path2_IsolatedNodeStart() throws IOException {
    String content = "b a";
    String filePath = createTempFile(content);
    graph.buildGraphFromFile(filePath);
    Random mockRandom = new Random() {
      @Override
      public int nextInt(int bound) {
        return 0; // Choose 'a' as start
      }
    };
    String result = graph.randomWalk(mockRandom);
    // 期望结果是起始节点，因为没有出边，游走立即停止
    assertEquals("a", result, "Path 2: Should return only the starting isolated word a.");

    deleteTempFile(filePath);
  }

  @Test
  void testRandomWalk_Path3_SingleLoop() throws IOException {
    String content = "a a";
    String filePath = createTempFile(content);
    graph.buildGraphFromFile(filePath);

    String result = graph.randomWalk(new Random());
    assertEquals("a a", result, "Path 3: Should return a a.");

    deleteTempFile(filePath);
  }

  @Test
  void testRandomWalk_Path4_WeightedNextWordsEmpty() throws IOException {
    String content = "a b";
    String filePath = createTempFile(content);
    graph.buildGraphFromFile(filePath);

    // 模拟 Random，使其总是从 'a' 开始，然后到 'b'
    Random mockRandom = new Random() {
      @Override
      public int nextInt(int bound) {
        // Choose 'a' as start and 'b' as next
        return 0;
      }
    };

    String result = graph.randomWalk(mockRandom);
    // 期望路径是 "a b"，因为 b 没有出边
    assertEquals("a b", result, "Path 4: Walk should stop after reaching a node with no valid next word.");

    deleteTempFile(filePath);
  }


  @Test
  void testRandomWalk_Path5_InnerLoopExecutionThenExit() throws IOException {
    // 图： a -> b (w=2), b -> c (w=1)
    // 确保能选择到权重大于1的边，使 B13 循环至少执行一次。
    // 然后确保路径能顺利走到 B19 并退出。
    String content = "a b a c"; // a->b (weight 2), b->c (weight 1)
    String filePath = createTempFile(content);
    graph.buildGraphFromFile(filePath);

    // 模拟 Random，使其路径可预测
    Random mockRandom = new Random() {
      int callCount = 0;
      int[] sequence = {0, 0, 0, 1}; // a b a c
      @Override
      public int nextInt(int bound) {
        int result = sequence[callCount];
        callCount++;
        return result;
      }
    };

    String result = graph.randomWalk(mockRandom);
    // 期望路径类似 "a b a c"
    assertEquals("a b a c", result, "Path 5: Should traverse inner loops and exit normally.");

    deleteTempFile(filePath);
  }
}