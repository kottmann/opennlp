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

package opennlp.tools.ml.model;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

/**
 * The context of a decision point during training.  This includes
 * contextual predicates and an outcome.
 */
public class Event {
  private final String outcome;
  private final String[] context;
  private final float[] values;

  public Event(String outcome, String[] context) {
    this(outcome,context,null);
  }

  public Event(String outcome, String[] context, float[] values) {
    this.outcome = Objects.requireNonNull(outcome, "outcome must not be null");
    this.context = Objects.requireNonNull(context, "context must not be null");
    this.values = values;
  }

  public String getOutcome() {
    return outcome;
  }

  public String[] getContext() {
    return context;
  }

  public float[] getValues() {
    return values;
  }

  // this could be moved to the Two Pass ...
  void asBinary(OutputStream out) throws IOException {

    DataOutputStream dataOut = new DataOutputStream(out);

    dataOut.writeUTF(outcome);

    dataOut.writeInt(context.length);

    for (String feature : context) {
      dataOut.writeUTF(feature);
    }
  }

  // this could be moved to Two Pass
  static Event readEvent(InputStream in) throws IOException {

    DataInputStream dataIn = new DataInputStream(in);

    try {
      String outcome = dataIn.readUTF();

      String[] context = new String[dataIn.readInt()];

      for (int i = 0; i < context.length; i++) {
        context[i] = dataIn.readUTF();
      }

      return new Event(outcome, context, null);
    }
    catch (EOFException e) {
      return null;
    }
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(outcome).append(" [");
    if (context.length > 0) {
      sb.append(context[0]);
      if (values != null) {
        sb.append("=").append(values[0]);
      }
    }
    for (int ci = 1; ci < context.length; ci++) {
      sb.append(" ").append(context[ci]);
      if (values != null) {
        sb.append("=").append(values[ci]);
      }
    }
    sb.append("]");
    return sb.toString();
  }
}
