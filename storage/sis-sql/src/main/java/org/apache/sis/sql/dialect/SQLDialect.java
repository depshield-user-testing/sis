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
package org.apache.sis.sql.dialect;

import java.sql.Connection;
import java.sql.SQLException;
import org.apache.sis.internal.sql.reverse.ColumnMetaModel;
import org.apache.sis.storage.DataStoreException;

/**
 * Each database has specific syntax elements.
 *
 * The dialect provide descriptions and methods to process the different
 * needs required by the store to generate the SQL requests.
 *
 * @author Johann Sorel (Geomatys)
 * @version 1.0
 * @since   1.0
 * @module
 */
public interface SQLDialect {

    /**
     * Encode column name.
     *
     * @param sql StringBuilder to write into
     * @param name column name, not null
     */
    void encodeColumnName(StringBuilder sql, String name);

    /**
     * Encode schema and table name portion of an sql query.
     *
     * @param sql StringBuilder to write into
     * @param databaseSchema database schema, can be null
     * @param tableName database table, not null
     */
    void encodeSchemaAndTableName(StringBuilder sql, String databaseSchema, String tableName);

    /**
     * If a column is an Auto-increment or has a sequence, try to extract next value.
     *
     * @param column database column description
     * @param cx database connection
     * @return column value or null
     * @throws SQLException
     * @throws DataStoreException
     */
    Object nextValue(ColumnMetaModel column, Connection cx) throws SQLException, DataStoreException;

}
