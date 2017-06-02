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

package opennlp.tools.ml.perceptron;

import opennlp.tools.ml.model.AbstractModel;
import opennlp.tools.ml.model.Context;
import opennlp.tools.ml.model.EvalParameters;

public class PerceptronModel extends AbstractModel {


  public PerceptronModel(Context[] params, long[] predLabels, String[] outcomeNames) {
    super(params,predLabels,outcomeNames);
    modelType = ModelType.Perceptron;
  }

  public double[] eval(long[] context) {
    return eval(context,new double[evalParams.getNumOutcomes()]);
  }

  public double[] eval(long[] context, float[] values) {
    return eval(context,values,new double[evalParams.getNumOutcomes()]);
  }

  public double[] eval(long[] context, double[] probs) {
    return eval(context,null,probs);
  }

  public double[] eval(long[] context, float[] values,double[] outsums) {
    Context[] scontexts = new Context[context.length];
    java.util.Arrays.fill(outsums, 0);
    for (int i = 0; i < context.length; i++) {
      scontexts[i] = pmap.get(context[i]);
    }
    return eval(scontexts,values,outsums,evalParams,true);
  }

  public static double[] eval(int[] context, double[] prior, EvalParameters model) {
    return eval(context,null,prior,model,true);
  }

  static double[] eval(int[] context, float[] values, double[] prior, EvalParameters model,
                              boolean normalize) {
    Context[] scontexts = new Context[context.length];
    for (int i = 0; i < context.length; i++) {
      scontexts[i] = model.getParams()[context[i]];
    }

    return eval(scontexts, values, prior, model, normalize);
  }

  static double[] eval(Context[] context, float[] values, double[] prior, EvalParameters model,
                       boolean normalize) {
    Context[] params = model.getParams();
    double[] activeParameters;
    int[] activeOutcomes;
    double value = 1;
    for (int ci = 0; ci < context.length; ci++) {
      if (context[ci] != null) {
        Context predParams = context[ci];
        activeOutcomes = predParams.getOutcomes();
        activeParameters = predParams.getParameters();
        if (values != null) {
          value = values[ci];
        }
        for (int ai = 0; ai < activeOutcomes.length; ai++) {
          int oid = activeOutcomes[ai];
          prior[oid] += activeParameters[ai] * value;
        }
      }
    }

    if (normalize) {
      int numOutcomes = model.getNumOutcomes();

      double maxPrior = 1;

      for (int oid = 0; oid < numOutcomes; oid++) {
        if (maxPrior < Math.abs(prior[oid]))
          maxPrior = Math.abs(prior[oid]);
      }

      double normal = 0.0;
      for (int oid = 0; oid < numOutcomes; oid++) {
        prior[oid] = Math.exp(prior[oid] / maxPrior);
        normal += prior[oid];
      }

      for (int oid = 0; oid < numOutcomes; oid++) {
        prior[oid] /= normal;
      }
    }
    return prior;
  }
}
