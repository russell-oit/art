/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sf.calcitecsvart;

import org.apache.calcite.DataContext;
import org.apache.calcite.linq4j.AbstractEnumerable;
import org.apache.calcite.linq4j.Enumerable;
import org.apache.calcite.linq4j.Enumerator;
import org.apache.calcite.rel.type.RelProtoDataType;
import org.apache.calcite.schema.ScannableTable;
import org.apache.calcite.schema.StreamableTable;
import org.apache.calcite.schema.Table;
import org.apache.calcite.util.Source;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Table based on a CSV file.
 *
 * <p>It implements the {@link ScannableTable} interface, so Calcite gets
 * data by calling the {@link #scan(DataContext)} method.
 */
public class CsvStreamScannableTable extends CsvScannableTable
    implements StreamableTable {
  /** Creates a CsvScannableTable. */
  CsvStreamScannableTable(Source source, RelProtoDataType protoRowType) {
    super(source, protoRowType);
  }

  @Override protected boolean isStream() {
    return true;
  }

  public String toString() {
    return "CsvStreamScannableTable";
  }

  public Enumerable<Object[]> scan(DataContext root) {
    final List<CsvFieldType> fieldTypes = getFieldTypes(root.getTypeFactory());
    final int[] fields = CsvEnumerator.identityList(fieldTypes.size());
    final AtomicBoolean cancelFlag = DataContext.Variable.CANCEL_FLAG.get(root);
    return new AbstractEnumerable<Object[]>() {
      public Enumerator<Object[]> enumerator() {
        return new CsvEnumerator<>(source, cancelFlag, true, null,
            new CsvEnumerator.ArrayRowConverter(fieldTypes, fields, true));
      }
    };
  }

  @Override public Table stream() {
    return this;
  }
}
