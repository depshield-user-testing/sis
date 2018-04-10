/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.sis.internal.sql.reverse;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import org.apache.sis.sql.dialect.SQLDialect;
import org.apache.sis.storage.DataStoreException;

/**
 * Describe a table primary key.
 *
 * @author Johann Sorel (Geomatys)
 * @version 1.0
 * @since   1.0
 * @module
 */
public class PrimaryKey {

    private final String tableName;
    private final List<ColumnMetaModel> columns;

    public PrimaryKey(String tableName) {
        this(tableName,null);
    }

    public PrimaryKey(String tableName, List<ColumnMetaModel> columns) {
        this.tableName = tableName;
        if (columns == null) columns = Collections.emptyList();
        this.columns = columns;
    }

    public String getTableName() {
        return tableName;
    }

    public List<ColumnMetaModel> getColumns() {
        return columns;
    }

    public boolean isNull(){
        return columns.isEmpty();
    }

    /**
     * Create a feature identifier from primary key column values.
     *
     * @param rs ResultSet on a row
     * @return feature identifier
     * @throws SQLException
     */
    public String buildIdentifier(final ResultSet rs) throws SQLException {

        final int size = columns.size();

        if (size == 0) {
            // no primary key columns, generate a random id
            return UUID.randomUUID().toString();
        } else if (size == 1) {
            // unique column value
            return rs.getString(columns.get(0).getName());
        } else {
            // aggregate column values
            final Object[] values = new Object[size];
            for (int i=0; i<size; i++) {
                values[i] = rs.getString(columns.get(i).getName());
            }
            return buildIdentifier(values);
        }
    }

    public static String buildIdentifier(final Object[] values) {
        final StringBuilder sb = new StringBuilder();
        for (int i=0; i<values.length; i++) {
            if (i > 0) sb.append('.');
            sb.append(values[i]);
        }
        return sb.toString();
    }

    /**
     * Create primary key column field values.
     *
     * @param dialect database dialect
     * @param logger database logger
     * @param cx database connection
     * @return primary key values
     * @throws SQLException
     * @throws DataStoreException
     */
    public Object[] nextValues(final SQLDialect dialect, Logger logger, final Connection cx)
            throws SQLException, DataStoreException {
        final Object[] parts = new Object[columns.size()];
        for (int i=0; i<parts.length; i++) {
            parts[i] = columns.get(i).nextValue(dialect, logger, cx);
        }
        return parts;
    }

}
