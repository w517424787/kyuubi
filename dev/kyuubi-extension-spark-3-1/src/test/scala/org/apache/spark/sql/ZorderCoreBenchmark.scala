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

package org.apache.spark.sql

import org.apache.spark.benchmark.Benchmark
import org.apache.spark.sql.benchmark.KyuubiBenchmarkBase

import org.apache.kyuubi.sql.zorder.ZorderBytesUtils

/**
 * Benchmark to measure performance with zorder core.
 *
 * To run this benchmark, temporarily change `ignore` to `test`, then run
 * {{{
 *   RUN_BENCHMARK=1 ./build/mvn clean test \
 *   -pl dev/kyuubi-extension-spark-3-1 -am \
 *   -Pspark-3.1,kyuubi-extension-spark-3-1 \
 *   -Dtest=none -DwildcardSuites=org.apache.spark.sql.ZorderCoreBenchmark
 * }}}
 */
class ZorderCoreBenchmark extends KyuubiSparkSQLExtensionTest with KyuubiBenchmarkBase {
  private val numRows = 1 * 1000 * 1000

  private def randomIntByteArray(numColumns: Int): Seq[Array[Array[Byte]]] = {
    (1 to numRows).map { i =>
      val arr = new Array[Array[Byte]](numColumns)
      (0 until numColumns).foreach(col => arr(col) = ZorderBytesUtils.toByte(i))
      arr
    }
  }

  private def randomLongByteArray(numColumns: Int): Seq[Array[Array[Byte]]] = {
    (1 to numRows).map { i =>
      val l = i.toLong
      val arr = new Array[Array[Byte]](numColumns)
      (0 until numColumns).foreach(col => arr(col) = ZorderBytesUtils.toByte(l))
      arr
    }
  }

  test("zorder core benchmark") {
    if (sys.env.contains("RUN_BENCHMARK")) {
      withHeader {
        val benchmark = new Benchmark(
          s"$numRows rows zorder core benchmark", numRows, output = output)
        benchmark.addCase("2 int columns benchmark", 3) { _ =>
          randomIntByteArray(2).foreach(ZorderBytesUtils.interleaveMultiByteArray)
        }

        benchmark.addCase("3 int columns benchmark", 3) { _ =>
          randomIntByteArray(3).foreach(ZorderBytesUtils.interleaveMultiByteArray)
        }

        benchmark.addCase("4 int columns benchmark", 3) { _ =>
          randomIntByteArray(4).foreach(ZorderBytesUtils.interleaveMultiByteArray)
        }


        benchmark.addCase("2 long columns benchmark", 3) { _ =>
          randomLongByteArray(2).foreach(ZorderBytesUtils.interleaveMultiByteArray)
        }

        benchmark.addCase("3 long columns benchmark", 3) { _ =>
          randomLongByteArray(3).foreach(ZorderBytesUtils.interleaveMultiByteArray)
        }

        benchmark.addCase("4 long columns benchmark", 3) { _ =>
          randomLongByteArray(4).foreach(ZorderBytesUtils.interleaveMultiByteArray)
        }

        benchmark.run()
      }
    }
  }
}