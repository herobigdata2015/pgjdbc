/*
 * Copyright (c) 2004, PostgreSQL Global Development Group
 * See the LICENSE file in the project root for more information.
 */

package org.herodbsql.test.jdbc2;

import org.herodbsql.core.CommandCompleteParserNegativeTest;
import org.herodbsql.core.CommandCompleteParserTest;
import org.herodbsql.core.OidToStringTest;
import org.herodbsql.core.OidValueOfTest;
import org.herodbsql.core.ParserTest;
import org.herodbsql.core.ReturningParserTest;
import org.herodbsql.core.UTF8EncodingTest;
import org.herodbsql.core.v3.V3ParameterListTests;
import org.herodbsql.jdbc.DeepBatchedInsertStatementTest;
import org.herodbsql.jdbc.NoColumnMetadataIssue1613Test;
import org.herodbsql.jdbc.PgSQLXMLTest;
import org.herodbsql.jdbc.PrimitiveArraySupportTest;
import org.herodbsql.test.core.FixedLengthOutputStreamTest;
import org.herodbsql.test.core.JavaVersionTest;
import org.herodbsql.test.core.LogServerMessagePropertyTest;
import org.herodbsql.test.core.NativeQueryBindLengthTest;
import org.herodbsql.test.core.OptionsPropertyTest;
import org.herodbsql.test.util.ByteBufferByteStreamWriterTest;
import org.herodbsql.test.util.ByteStreamWriterTest;
import org.herodbsql.test.util.ExpressionPropertiesTest;
import org.herodbsql.test.util.HostSpecTest;
import org.herodbsql.test.util.LruCacheTest;
import org.herodbsql.test.util.PGPropertyMaxResultBufferParserTest;
import org.herodbsql.test.util.ServerVersionParseTest;
import org.herodbsql.test.util.ServerVersionTest;
import org.herodbsql.util.ReaderInputStreamTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/*
 * Executes all known tests for JDBC2 and includes some utility methods.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    ANTTest.class,
    ArrayTest.class,
    BatchedInsertReWriteEnabledTest.class,
    BatchExecuteTest.class,
    BatchFailureTest.class,
    BlobTest.class,
    BlobTransactionTest.class,
    CallableStmtTest.class,
    ClientEncodingTest.class,
    ColumnSanitiserDisabledTest.class,
    ColumnSanitiserEnabledTest.class,
    CommandCompleteParserNegativeTest.class,
    CommandCompleteParserTest.class,
    ConcurrentStatementFetch.class,
    ConnectionTest.class,
    ConnectTimeoutTest.class,
    CopyLargeFileTest.class,
    CopyTest.class,
    CursorFetchTest.class,
    DatabaseEncodingTest.class,
    DatabaseMetaDataCacheTest.class,
    DatabaseMetaDataPropertiesTest.class,
    DatabaseMetaDataTest.class,
    DateStyleTest.class,
    DateTest.class,
    DeepBatchedInsertStatementTest.class,
    DriverTest.class,
    EncodingTest.class,
    ExpressionPropertiesTest.class,
    GeometricTest.class,
    GetXXXTest.class,
    HostSpecTest.class,
    IntervalTest.class,
    JavaVersionTest.class,
    JBuilderTest.class,
    LoginTimeoutTest.class,
    LogServerMessagePropertyTest.class,
    LruCacheTest.class,
    MiscTest.class,
    NativeQueryBindLengthTest.class,
    NoColumnMetadataIssue1613Test.class,
    NotifyTest.class,
    OidToStringTest.class,
    OidValueOfTest.class,
    OptionsPropertyTest.class,
    OuterJoinSyntaxTest.class,
    FixedLengthOutputStreamTest.class,
    ByteStreamWriterTest.class,
    ByteBufferByteStreamWriterTest.class,
    ParameterStatusTest.class,
    ParserTest.class,
    PGPropertyMaxResultBufferParserTest.class,
    PGPropertyTest.class,
    PGTimestampTest.class,
    PGTimeTest.class,
    PgSQLXMLTest.class,
    PreparedStatementTest.class,
    PrimitiveArraySupportTest.class,
    QuotationTest.class,
    ReaderInputStreamTest.class,
    RefCursorTest.class,
    ReplaceProcessingTest.class,
    ResultSetMetaDataTest.class,
    ResultSetTest.class,
    ReturningParserTest.class,
    SearchPathLookupTest.class,
    ServerCursorTest.class,
    ServerErrorTest.class,
    ServerPreparedStmtTest.class,
    ServerVersionParseTest.class,
    ServerVersionTest.class,
    StatementTest.class,
    StringTypeUnspecifiedArrayTest.class,
    TestACL.class,
    TimestampTest.class,
    TimeTest.class,
    TimezoneCachingTest.class,
    TimezoneTest.class,
    TypeCacheDLLStressTest.class,
    UpdateableResultTest.class,
    UpsertTest.class,
    UTF8EncodingTest.class,
    V3ParameterListTests.class,
})
public class Jdbc2TestSuite {
}
