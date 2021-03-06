/*
 * Copyright (C) 2013 Brett Wooldridge
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zaxxer.hikari.pool;

import java.sql.Connection;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Wrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.util.ClockSource;

/**
 * This is the proxy class for java.sql.Statement.
 *
 * @author Brett Wooldridge
 */
public abstract class ProxyStatement implements Statement
{
   private static final Logger LOGGER = LoggerFactory.getLogger(ProxyStatement.class);
   protected final ProxyConnection connection;
   protected final Statement delegate;
   protected final ClockSource source = ClockSource.INSTANCE;

   private boolean isClosed;
   private ResultSet proxyResultSet;
   private boolean isLogTracingEnabled;
   private long longSqlTimeInMillis;
   
   protected ProxyStatement(ProxyConnection connection, Statement statement, HikariConfig hikariConfig)
   {
      this.connection = connection;
      this.delegate = statement;
      this.isLogTracingEnabled = hikariConfig.isLongSqlTrackingEnabled();
      this.longSqlTimeInMillis = hikariConfig.getLongSqlTimeoutInMillis();
   }

   final SQLException checkException(SQLException e)
   {
      return connection.checkException(e);
   }

   /** {@inheritDoc} */
   @Override
   public final String toString()
   {
      final String delegateToString = delegate.toString();
      return new StringBuilder(64 + delegateToString.length())
         .append(this.getClass().getSimpleName()).append('@').append(System.identityHashCode(this))
         .append(" wrapping ")
         .append(delegateToString).toString();
   }

   // **********************************************************************
   //                 Overridden java.sql.Statement Methods
   // **********************************************************************

   /** {@inheritDoc} */
   @Override
   public final void close() throws SQLException
   {
      if (isClosed) {
         return;
      }

      isClosed = true;
      connection.untrackStatement(delegate);

      try {
         delegate.close();
      }
      catch (SQLException e) {
         throw connection.checkException(e);
      }
   }

   /** {@inheritDoc} */
   @Override
   public Connection getConnection() throws SQLException
   {
      return connection;
   }

   /** {@inheritDoc} */
   @Override
   public boolean execute(String sql) throws SQLException
   {
	  final long start = source.currentTime();
	  try {
	      connection.markCommitStateDirty();
	      return delegate.execute(sql);
	  } finally {
		  log (source.elapsedMillis(start), sql);
	  }
   }

/** {@inheritDoc} */
   @Override
   public boolean execute(String sql, int autoGeneratedKeys) throws SQLException
   {
	  final long start = source.currentTime();
	  try {
	      connection.markCommitStateDirty();
	      return delegate.execute(sql, autoGeneratedKeys);
	  } finally {
		  log (source.elapsedMillis(start), sql);
	  }
   }

   /** {@inheritDoc} */
   @Override
   public ResultSet executeQuery(String sql) throws SQLException
   {
	  final long start = source.currentTime();
	  try {
	      connection.markCommitStateDirty();
	      ResultSet resultSet = delegate.executeQuery(sql);
	      return ProxyFactory.getProxyResultSet(connection, this, resultSet); 
   	  } finally {
		  log (source.elapsedMillis(start), sql);
	  }
   }

   /** {@inheritDoc} */
   @Override
   public int executeUpdate(String sql) throws SQLException
   {
	  final long start = source.currentTime();
	  try {
	      connection.markCommitStateDirty();
	      return delegate.executeUpdate(sql);
	  } finally {
		  log (source.elapsedMillis(start), sql);
	  }
   }

   /** {@inheritDoc} */
   @Override
   public int[] executeBatch() throws SQLException
   {
      connection.markCommitStateDirty();
      return delegate.executeBatch();
   }

   /** {@inheritDoc} */
   @Override
   public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException
   {
	  final long start = source.currentTime();
	  try {
	      connection.markCommitStateDirty();
	      return delegate.executeUpdate(sql, autoGeneratedKeys);
   	  } finally {
		  log (source.elapsedMillis(start), sql);
	  }
   }

   /** {@inheritDoc} */
   @Override
   public int executeUpdate(String sql, int[] columnIndexes) throws SQLException
   {
	  final long start = source.currentTime();
	  try {
	      connection.markCommitStateDirty();
	      return delegate.executeUpdate(sql, columnIndexes);
	  } finally {
		  log (source.elapsedMillis(start), sql);
	  }
   }

   /** {@inheritDoc} */
   @Override
   public int executeUpdate(String sql, String[] columnNames) throws SQLException
   { 
	  final long start = source.currentTime();
      try {
	      connection.markCommitStateDirty();
	      return delegate.executeUpdate(sql, columnNames);
      } finally {
		  log (source.elapsedMillis(start), sql);
	  }
   }

   /** {@inheritDoc} */
   @Override
   public boolean execute(String sql, int[] columnIndexes) throws SQLException
   {
	  final long start = source.currentTime();
      try {
	      connection.markCommitStateDirty();
	      return delegate.execute(sql, columnIndexes);
      } finally {
		  log (source.elapsedMillis(start), sql);
	  }   
   }

   /** {@inheritDoc} */
   @Override
   public boolean execute(String sql, String[] columnNames) throws SQLException
   {
	  final long start = source.currentTime();
	  try {
	      connection.markCommitStateDirty();
	      return delegate.execute(sql, columnNames);
	  } finally {
		  log (source.elapsedMillis(start), sql);
	  }
   }

   /** {@inheritDoc} */
   @Override
   public long[] executeLargeBatch() throws SQLException
   {
      connection.markCommitStateDirty();
      return delegate.executeLargeBatch();
   }

   /** {@inheritDoc} */
   @Override
   public long executeLargeUpdate(String sql) throws SQLException
   {
	  final long start = source.currentTime();
	  try {
	      connection.markCommitStateDirty();
	      return delegate.executeLargeUpdate(sql);
	  } finally {
		  log (source.elapsedMillis(start), sql);
	  }
   }

   /** {@inheritDoc} */
   @Override
   public long executeLargeUpdate(String sql, int autoGeneratedKeys) throws SQLException
   {
	  final long start = source.currentTime();
      try {
	      connection.markCommitStateDirty();
	      return delegate.executeLargeUpdate(sql, autoGeneratedKeys);
      } finally {
		  log (source.elapsedMillis(start), sql);
	  }
   }

   /** {@inheritDoc} */
   @Override
   public long executeLargeUpdate(String sql, int[] columnIndexes) throws SQLException
   {
	  final long start = source.currentTime();
	  try {
	      connection.markCommitStateDirty();
	      return delegate.executeLargeUpdate(sql, columnIndexes);
	  } finally {
		  log (source.elapsedMillis(start), sql);
	  }
   }

   /** {@inheritDoc} */
   @Override
   public long executeLargeUpdate(String sql, String[] columnNames) throws SQLException
   {
	  final long start = source.currentTime();
      try {
	      connection.markCommitStateDirty();
	      return delegate.executeLargeUpdate(sql, columnNames);
      } finally {
		  log (source.elapsedMillis(start), sql);
	  }
   }

   /** {@inheritDoc} */
   @Override
   public ResultSet getResultSet() throws SQLException {
      final ResultSet resultSet = delegate.getResultSet();
      if (resultSet != null) {
         if (proxyResultSet == null || ((ProxyResultSet) proxyResultSet).delegate != resultSet) {
            proxyResultSet = ProxyFactory.getProxyResultSet(connection, this, resultSet);
         }
      }
      else {
         proxyResultSet = null;
      }
      return proxyResultSet;
   }

   /** {@inheritDoc} */
   @Override
   @SuppressWarnings("unchecked")
   public final <T> T unwrap(Class<T> iface) throws SQLException
   {
      if (iface.isInstance(delegate)) {
         return (T) delegate;
      }
      else if (delegate instanceof Wrapper) {
          return (T) delegate.unwrap(iface);
      }

      throw new SQLException("Wrapped statement is not an instance of " + iface);
   }
   
   protected final void log(final long elapsedTimeInMillis, final String sql) {
	   if (isLogTracingEnabled && elapsedTimeInMillis > longSqlTimeInMillis) {
		   String parameters = "";
		   if (delegate instanceof PreparedStatement) {
			  try {
				ParameterMetaData parameterMetaData = ((PreparedStatement)delegate).getParameterMetaData();
				if (parameterMetaData != null) {
					parameters = parameterMetaData.toString();
				}
			  } catch (SQLException e) {
				// Ignore
			  }
		   }
		   LOGGER.warn(String.format("SQL: %s have taken %d millisecond(s) for execution with parameters: %s", sql, elapsedTimeInMillis, parameters));
	   }
   }

}
