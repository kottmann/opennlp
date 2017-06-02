/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package opennlp.tools.ml;

import java.util.Arrays;

public class HashUtil {

  public static long hash(final CharSequence data) {
    return hash(data, 0, data.length());
  }

  // MurmurHash
  public static long hash(final CharSequence data, final int offset, final int length) {
    final int seed = 0xe17a1465;
    final long m = 0xc6a4a7935bd1e995L;
    final int r = 47;

    long h = (seed & 0xffffffffl) ^ (length * m);

    int length4 = length / 4;

    for (int i = 0; i < length4; i++) {
      final int i4 = offset + i * 4;
      long k = ((long) data.charAt(i4 + 0) & 0xffff) + (((long) data.charAt(i4 + 1) & 0xffff) << 16)
          + (((long) data.charAt(i4 + 2) & 0xffff) << 32) + (((long) data.charAt(i4 + 3) & 0xffff) << 48);

      k *= m;
      k ^= k >>> r;
      k *= m;

      h ^= k;
      h *= m;
    }

    switch (length % 4) {
      case 3:
        h ^= ((long) (data.charAt(offset + (length & ~3) + 2) & 0xffff)) << 32;
      case 2:
        h ^= ((long) (data.charAt(offset + (length & ~3) + 1) & 0xffff)) << 16;
      case 1:
        h ^= (long) (data.charAt(offset + (length & ~3)) & 0xffff);
        h *= m;
    }


    h ^= h >>> r;
    h *= m;
    h ^= h >>> r;

    return h;
  }

  public static long[] hash(String[] chars) {
    return Arrays.stream(chars).mapToLong(HashUtil::hash).toArray();
  }
}
