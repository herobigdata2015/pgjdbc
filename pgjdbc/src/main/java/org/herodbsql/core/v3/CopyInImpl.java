/*
 * Copyright (c) 2009, PostgreSQL Global Development Group
 * See the LICENSE file in the project root for more information.
 */

package org.herodbsql.core.v3;

import org.herodbsql.copy.CopyIn;
import org.herodbsql.util.ByteStreamWriter;
import org.herodbsql.util.GT;
import org.herodbsql.util.PSQLException;
import org.herodbsql.util.PSQLState;

import java.sql.SQLException;

/**
 * <p>COPY FROM STDIN operation.</p>
 *
 * <p>Anticipated flow:
 *
 * CopyManager.copyIn() -&gt;QueryExecutor.startCopy() - sends given query to server
 * -&gt;processCopyResults(): - receives CopyInResponse from Server - creates new CopyInImpl
 * -&gt;initCopy(): - receives copy metadata from server -&gt;CopyInImpl.init() -&gt;lock()
 * connection for this operation - if query fails an exception is thrown - if query returns wrong
 * CopyOperation, copyIn() cancels it before throwing exception &lt;-return: new CopyInImpl holding
 * lock on connection repeat CopyIn.writeToCopy() for all data -&gt;CopyInImpl.writeToCopy()
 * -&gt;QueryExecutorImpl.writeToCopy() - sends given data -&gt;processCopyResults() - parameterized
 * not to block, just peek for new messages from server - on ErrorResponse, waits until protocol is
 * restored and unlocks connection CopyIn.endCopy() -&gt;CopyInImpl.endCopy()
 * -&gt;QueryExecutorImpl.endCopy() - sends CopyDone - processCopyResults() - on CommandComplete
 * -&gt;CopyOperationImpl.handleCommandComplete() - sets updatedRowCount when applicable - on
 * ReadyForQuery unlock() connection for use by other operations &lt;-return:
 * CopyInImpl.getUpdatedRowCount()</p>
 */
public class CopyInImpl extends CopyOperationImpl implements CopyIn {

  public void writeToCopy(byte[] data, int off, int siz) throws SQLException {
    queryExecutor.writeToCopy(this, data, off, siz);
  }

  public void writeToCopy(ByteStreamWriter from) throws SQLException {
    queryExecutor.writeToCopy(this, from);
  }

  public void flushCopy() throws SQLException {
    queryExecutor.flushCopy(this);
  }

  public long endCopy() throws SQLException {
    return queryExecutor.endCopy(this);
  }

  protected void handleCopydata(byte[] data) throws PSQLException {
    throw new PSQLException(GT.tr("CopyIn copy direction can't receive data"),
        PSQLState.PROTOCOL_VIOLATION);
  }
}
