package org.neikius.test.importmatch;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Match {

  private String matchId;
  private Integer marketId;
  private String outcomeId;
  private String specifiers;
  private Instant dateInsert;
}
