/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.spark.sql.hive.execution.command

import org.apache.spark.sql.AnalysisException
import org.apache.spark.sql.execution.command.v1
import org.apache.spark.sql.hive.test.TestHiveSingleton

class AlterTableDropPartitionSuite
  extends v1.AlterTableDropPartitionSuiteBase
  with TestHiveSingleton {

  override def version: String = "Hive V1"
  override def defaultUsing: String = "USING HIVE"

  override protected val notFullPartitionSpecErr = "No partition is dropped"

  test("partition not exists") {
    withNsTable("ns", "tbl") { t =>
      sql(s"CREATE TABLE $t (id bigint, data string) $defaultUsing PARTITIONED BY (id)")
      sql(s"ALTER TABLE $t ADD PARTITION (id=1) LOCATION 'loc'")

      val errMsg = intercept[AnalysisException] {
        sql(s"ALTER TABLE $t DROP PARTITION (id=1), PARTITION (id=2)")
      }.getMessage
      assert(errMsg.contains("No partition is dropped"))

      checkPartitions(t, Map("id" -> "1"))
      sql(s"ALTER TABLE $t DROP IF EXISTS PARTITION (id=1), PARTITION (id=2)")
      checkPartitions(t)
    }
  }
}
