// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim.test;

import static util.ListUtility.list;

import java.util.List;

public class TestTable {
  private String param = "";

  public TestTable(String x) {
    param = x;
  }

  public TestTable() {
  }

  public List<?> doTable(List<?> l) {
    return list(
      list("pass", "error:huh", param),
      list("bill", "no change", "pass:jake")
    );
  }
}
