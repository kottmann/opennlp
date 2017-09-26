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

module opennlp.tools {
  requires java.xml;

  // TODO: Missing is currently:
  // cmdline
  // format
  // Should those be exported as well?

  exports opennlp.tools.chunker;
  exports opennlp.tools.dictionary;
  exports opennlp.tools.doccat;
  exports opennlp.tools.entitylinker;
  exports opennlp.tools.langdetect;
  exports opennlp.tools.languagemodel;
  exports opennlp.tools.lemmatizer;
  exports opennlp.tools.ml;
  exports opennlp.tools.ml.maxent;
  exports opennlp.tools.ml.maxent.io;
  exports opennlp.tools.ml.maxent.quasinewton;
  exports opennlp.tools.ml.model;
  exports opennlp.tools.ml.naivebayes;
  exports opennlp.tools.ml.perceptron;
  exports opennlp.tools.namefind;
  exports opennlp.tools.ngram;
  exports opennlp.tools.parser;
  exports opennlp.tools.parser.chunking;
  exports opennlp.tools.parser.lang.en;
  exports opennlp.tools.parser.lang.es;
  exports opennlp.tools.parser.treeinsert;
  exports opennlp.tools.postag;
  exports opennlp.tools.sentdetect;
  exports opennlp.tools.sentdetect.lang;
  exports opennlp.tools.sentdetect.lang.th;
  exports opennlp.tools.stemmer;
  exports opennlp.tools.stemmer.snowball;
  exports opennlp.tools.tokenize;
  exports opennlp.tools.tokenize.lang;
  exports opennlp.tools.tokenize.lang.en;
  exports opennlp.tools.util;
  exports opennlp.tools.util.eval;
  exports opennlp.tools.util.ext;
  exports opennlp.tools.util.featuregen;
  exports opennlp.tools.util.model;
  exports opennlp.tools.util.normalizer;
}
