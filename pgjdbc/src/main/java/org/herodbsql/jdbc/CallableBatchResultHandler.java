/*
 * Copyright (c) 2016, PostgreSQL Global Development Group
 * See the LICENSE file in the project root for more information.
 */

package org.herodbsql.jdbc;

import org.herodbsql.core.Field;
import org.herodbsql.core.ParameterList;
import org.herodbsql.core.Query;
import org.herodbsql.core.ResultCursor;
import org.herodbsql.core.Tuple;

import java.util.List;

class CallableBatchResultHandler extends BatchResultHandler {
  CallableBatchResultHandler(PgStatement statement, Query[] queries, ParameterList[] parameterLists) {
    super(statement, queries, parameterLists, false);
  }

  public void handleResultRows(Query fromQuery, Field[] fields, List<Tuple> tuples, ResultCursor cursor) {
    /* ignore */
  }
}
