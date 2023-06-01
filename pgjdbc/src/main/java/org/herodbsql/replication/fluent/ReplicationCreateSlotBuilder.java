/*
 * Copyright (c) 2016, PostgreSQL Global Development Group
 * See the LICENSE file in the project root for more information.
 */

package org.herodbsql.replication.fluent;

import org.herodbsql.core.BaseConnection;
import org.herodbsql.replication.fluent.logical.ChainedLogicalCreateSlotBuilder;
import org.herodbsql.replication.fluent.logical.LogicalCreateSlotBuilder;
import org.herodbsql.replication.fluent.physical.ChainedPhysicalCreateSlotBuilder;
import org.herodbsql.replication.fluent.physical.PhysicalCreateSlotBuilder;

public class ReplicationCreateSlotBuilder implements ChainedCreateReplicationSlotBuilder {
  private final BaseConnection baseConnection;

  public ReplicationCreateSlotBuilder(BaseConnection baseConnection) {
    this.baseConnection = baseConnection;
  }

  @Override
  public ChainedLogicalCreateSlotBuilder logical() {
    return new LogicalCreateSlotBuilder(baseConnection);
  }

  @Override
  public ChainedPhysicalCreateSlotBuilder physical() {
    return new PhysicalCreateSlotBuilder(baseConnection);
  }
}
