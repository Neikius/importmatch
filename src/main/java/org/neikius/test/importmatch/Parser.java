package org.neikius.test.importmatch;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class Parser {

  private String file;

  /**
   * Will parse the CSV file to Match POJO list or stream. 4 fields, separated with | and string fields (1.,3.,4.) in single quotes
   * @param fileName system filename with path or filename within the classpath
   */
  public Parser(String fileName) {
    this.file = fileName;
  }

  public Stream<Match> parse() throws IOException {
    return getDataStream()
        .map(this::parseMatch)
        .sorted(
            Comparator.comparing(Match::getMatchId)
                .thenComparing(Match::getMarketId)
                .thenComparing(Match::getOutcomeId)
                .thenComparing(Match::getSpecifiers, (o1, o2) -> {
                  if (o1==null && o2==null)
                    return 0;
                  if (o1==null)
                    return -1;
                  return o1.compareTo(o2);
                }));
  }

  public List<Match> parseToList() throws IOException {
    return parse().collect(Collectors.toList());
  }

  private Stream<String> getDataStream() throws IOException {

    InputStream is = this.getClass().getClassLoader().getResourceAsStream(file);
    // if not found in classpath, assume JAR and try filesystem
    if (is == null) {
      is = new FileInputStream(file);
    }

    BufferedReader reader = new BufferedReader(new InputStreamReader(is));

    // skip first line
    reader.readLine();

    return reader.lines().parallel();
  }

  /**
   * Parse a match line into Match POJO
   * @param line CSV string delimited by |
   * @return {@link Match}
   */
  public Match parseMatch(String line) {
    List<Integer> splitAt = new ArrayList<>(3);
    boolean quote = false;
    for (int i=0; i<line.length(); i++) {
      if (line.charAt(i) == '|' && !quote) {
        splitAt.add(i);
      } if (line.charAt(i) == '\'') {
        quote = !quote;
      }
    }
    List<String> tokens = new ArrayList<>(4);
    int previous = 0;
    for (Integer splitIdx : splitAt) {
      tokens.add(stripQuotes(line.substring(previous, splitIdx)));
      previous = splitIdx+1;
    }
    String token = stripQuotes(line.substring(previous));
    if (token != null && token.length() > 0) {
      tokens.add(token);
    }

    Match match = new Match();
    match.setMatchId(tokens.get(0));
    match.setMarketId(Integer.parseInt(tokens.get(1)));
    match.setOutcomeId(tokens.get(2));
    if (tokens.size() > 3)
      match.setSpecifiers(tokens.get(3));

    return match;
  }

  private String stripQuotes(String str) {
    if (str.startsWith("'")) {
      return str.substring(1, str.length()-1);
    }
    return str;
  }
}
