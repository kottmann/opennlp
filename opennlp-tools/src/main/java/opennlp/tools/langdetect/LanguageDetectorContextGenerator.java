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

package opennlp.tools.langdetect;

import opennlp.tools.ml.HashUtil;
import opennlp.tools.util.normalizer.AggregateCharSequenceNormalizer;
import opennlp.tools.util.normalizer.CharSequenceNormalizer;

/**
 * A context generator for language detector.
 */
class LanguageDetectorContextGenerator {

  protected final int minLength;
  protected final int maxLength;
  protected final CharSequenceNormalizer normalizer;

  /**
   * Creates a customizable @{@link LanguageDetectorContextGenerator} that computes ngrams from text
   * @param minLength min ngrams chars
   * @param maxLength max ngrams chars
   * @param normalizers zero or more normalizers to
   *                    be applied in to the text before extracting ngrams
   */
  public LanguageDetectorContextGenerator(int minLength, int maxLength,
                                          CharSequenceNormalizer... normalizers) {
    this.minLength = minLength;
    this.maxLength = maxLength;

    this.normalizer = new AggregateCharSequenceNormalizer(normalizers);
  }

  private static class LowerCasedSequence implements  CharSequence {

    private CharSequence sequence;

    public LowerCasedSequence(CharSequence sequence) {
      this.sequence = sequence;
    }

    @Override
    public int length() {
      return sequence.length();
    }

    @Override
    public char charAt(int index) {
      return Character.toLowerCase(sequence.charAt(index));
    }

    @Override
    public CharSequence subSequence(int start, int end) {
      return new LowerCasedSequence(sequence.subSequence(start, end));
    }
  }

  /**
   * Generates the context for a document using character ngrams.
   * @param document document to extract context from
   * @return the generated context
   */
  public long[] getContext(String document) {

    int contextIndex = 0;
    long[] context = new long[document.length() + document.length() - 1 + document.length() - 2];

    CharSequence lowerCased = new LowerCasedSequence(document);

    for (int textIndex = 0; textIndex < document.length(); textIndex++) {
      for (int lengthIndex = minLength; textIndex + lengthIndex - 1 < document.length()
          && lengthIndex < maxLength + 1; lengthIndex++) {
        context[contextIndex++] = (HashUtil.hash(lowerCased, textIndex, lengthIndex));
      }
    }

    return context;
  }
}
