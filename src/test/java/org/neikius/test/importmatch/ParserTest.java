package org.neikius.test.importmatch;

import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class ParserTest {

  @Test
  public void parseMatch() throws IOException {
    Parser parser = new Parser("test.txt");
    List<Match> matches = parser.parse().collect(Collectors.toList());

    assertNotNull(matches);
    assertEquals(matches.size(), 20);
    assertEquals(matches.get(0).getMatchId(), "sr:match:11842304");
  }

  @Test
  public void parseMatch2() throws IOException {
    Parser parser = new Parser("test2.txt");
    List<Match> matches = parser.parse().collect(Collectors.toList());

    assertIterableEquals(matches, Arrays.asList(
        new Match("sr:match:12261810", 1, "3", null, null),
        new Match("sr:match:13762991", 60, "2", null, null),
        new Match("sr:match:14198799", 218, "5", "setnr=2|gamenr=6|pointnr=1", null)
    ));
  }

  @Test
  public void parseTime() throws IOException {
    long time = System.nanoTime();

    Parser parser = new Parser("fo_random.txt");
    List<Match> matches = parser.parseToList();
    assertEquals(matches.size(), 302536);

    long diff = System.nanoTime() - time;
    System.out.println("Duration in ns:" + diff);
    System.out.println("Duration in ms:" + (diff/1_000_000));
  }

}
