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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.ObjectStreamUtils;
import opennlp.tools.util.TrainingParameters;

/**
 * An indexer for maxent model data which handles cutoffs for uncommon
 * contextual predicates and provides a unique integer index for each of the
 * predicates.
 */
public class Chi2DataIndexer extends AbstractDataIndexer {

  public Chi2DataIndexer() {
  }

  @Override
  public void index(ObjectStream<Event> eventStream) throws IOException {
    int cutoff = trainingParameters.getIntParameter(CUTOFF_PARAM, CUTOFF_DEFAULT);
    boolean sort = trainingParameters.getBooleanParameter(SORT_PARAM, SORT_DEFAULT);

    Map<String, Integer> aMap = new HashMap<>();
    Map<String, Integer> outcomeCount = new HashMap<>();

    Map<String, Integer> featureCount = new HashMap<>();

    List<Event> events1 = new ArrayList<>();
    Event ev;
    while ((ev = eventStream.read()) != null) {
      events1.add(ev);

      // this can occur multiple times per event (we just count once)
      Set<String> contextSet =  new HashSet<>(Arrays.asList(ev.getContext()));
      for (String feature: contextSet) {
        aMap.merge(feature + "_" + ev.getOutcome().trim(), 1, (x1, x2) -> x1 + x2);
        featureCount.merge(feature, 1, (x1, x2) -> x1 + x2);
      }

      outcomeCount.merge(ev.getOutcome().trim(), 1, (x1, x2) -> x1 + x2);
    }

    TwoPassDataIndexer twoPassDataIndexer = new TwoPassDataIndexer();
    TrainingParameters params = new TrainingParameters();
    params.put(CUTOFF_PARAM, "0");
    twoPassDataIndexer.init(params, null);
    twoPassDataIndexer.index(ObjectStreamUtils.createObjectStream(events1));

    Map<String, Double> chiMap = new HashMap<>();

    System.out.println("PMAP BUILD");

    for (Map.Entry<String, Integer> tAndC : aMap.entrySet()) {
      double a = tAndC.getValue();

      String feature = tAndC.getKey().substring(0, tAndC.getKey().lastIndexOf('_'));
      String outcome = tAndC.getKey().substring(tAndC.getKey().lastIndexOf('_') + 1);

      double b = featureCount.get(feature) - a;


      double c = outcomeCount.get(outcome) - a;

      double n = twoPassDataIndexer.getNumEvents();
      double d = n - a - b - c;


      if (a < 0) {
        throw new RuntimeException("a");
      }

      if (b < 0) {
        throw new RuntimeException("b");
      }

      if (c < 0) {
        System.out.println("feature: " + feature + "  " + outcome + "   " + tAndC.getKey());

        throw new RuntimeException("c a " + a + "  b " + b + "   c " + c + "   d" + d + "  n "
            + n + " outcomeCount " + outcomeCount.get(outcome));
      }

      if (d < 0) {
        throw new RuntimeException("d");
      }

      double chi = (n * Math.pow((a * d) - (c * b) ,2)) / ((a + c) * (b + d) * (a + b) * (c + d));

      if (Double.isNaN(chi)) {
        chi = 10000d;
      }

      if (chi < 0) {
        throw new RuntimeException("chi a " + a + "  b " + b + "   c " + c + "   d" + d + "  n " + n);
      }

      chiMap.put(tAndC.getKey(), chi);
    }

    List<Event> events2 = new ArrayList<>();

    int eliminated = 0;
    int active = 0;

    for (Event event : events1) {

      List<String> context = new ArrayList<>();

      for (String feature : event.getContext()) {
        double chi = chiMap.get(feature + "_" + event.getOutcome());

        if (chi > 0.0005d) {
          context.add(feature);
          active++;
        }
        else {
          eliminated++;
        }
      }
      if (context.size() > 0) {
        events2.add(new Event(event.getOutcome(),
            context.toArray(new String[context.size()]), event.getValues()));
      }
    }

    System.out.println("####### Eliminated " + eliminated + " #### " + active);

    Map<String, Integer> predicateIndex = new HashMap<>();
    List<Event> events;
    List<ComparableEvent> eventsToCompare;

    display("Indexing events using cutoff of " + cutoff + "\n\n");

    display("\tComputing event counts...  ");
    events = computeEventCounts(ObjectStreamUtils.createObjectStream(events2), predicateIndex, cutoff);
    display("done. " + events.size() + " events\n");

    display("\tIndexing...  ");
    eventsToCompare = index(events, predicateIndex);
    // done with event list
    events = null;
    // done with predicates
    predicateIndex = null;

    display("done.\n");

    display("Sorting and merging events... ");
    sortAndMerge(eventsToCompare, sort);
    display("Done indexing.\n");
  }

  // 1. compute all A for each feature_outcome
  // 2. eliminate features in contexts
  // 3. update counts


  /**
   * Reads events from <tt>eventStream</tt> into a linked list. The predicates
   * associated with each event are counted and any which occur at least
   * <tt>cutoff</tt> times are added to the <tt>predicatesInOut</tt> map along
   * with a unique integer index.
   *
   * @param eventStream
   *          an <code>EventStream</code> value
   * @param predicatesInOut
   *          a <code>TObjectIntHashMap</code> value
   * @param cutoff
   *          an <code>int</code> value
   * @return a <code>TLinkedList</code> value
   */
  private List<Event> computeEventCounts(ObjectStream<Event> eventStream,
                                         Map<String, Integer> predicatesInOut,
                                         int cutoff) throws IOException {
    Set<String> predicateSet = new HashSet<>();
    Map<String, Integer> counter = new HashMap<>();
    List<Event> events = new LinkedList<>();
    Event ev;
    while ((ev = eventStream.read()) != null) {
      events.add(ev);
      update(ev.getContext(), predicateSet, counter, cutoff);
    }
    predCounts = new int[predicateSet.size()];
    int index = 0;
    for (Iterator<String> pi = predicateSet.iterator(); pi.hasNext(); index++) {
      String predicate = pi.next();
      predCounts[index] = counter.get(predicate);
      predicatesInOut.put(predicate, index);
    }
    return events;
  }

  protected List<ComparableEvent> index(List<Event> events,
                                        Map<String, Integer> predicateIndex) {
    Map<String, Integer> omap = new HashMap<>();

    int numEvents = events.size();
    int outcomeCount = 0;
    List<ComparableEvent> eventsToCompare = new ArrayList<>(numEvents);
    List<Integer> indexedContext = new ArrayList<>();

    for (Event ev:events) {
      String[] econtext = ev.getContext();
      ComparableEvent ce;

      int ocID;
      String oc = ev.getOutcome();

      if (omap.containsKey(oc)) {
        ocID = omap.get(oc);
      } else {
        ocID = outcomeCount++;
        omap.put(oc, ocID);
      }

      for (String pred : econtext) {
        if (predicateIndex.containsKey(pred)) {
          indexedContext.add(predicateIndex.get(pred));
        }
      }

      // drop events with no active features
      if (indexedContext.size() > 0) {
        int[] cons = new int[indexedContext.size()];
        for (int ci = 0; ci < cons.length; ci++) {
          cons[ci] = indexedContext.get(ci);
        }
        ce = new ComparableEvent(ocID, cons);
        eventsToCompare.add(ce);
      } else {
        display("Dropped event " + ev.getOutcome() + ":"
            + Arrays.asList(ev.getContext()) + "\n");
      }
      // recycle the TIntArrayList
      indexedContext.clear();
    }
    outcomeLabels = toIndexedStringArray(omap);
    predLabels = toIndexedStringArray(predicateIndex);
    return eventsToCompare;
  }

  public static void main(String[] args) {

    double a = 80;
    double b = 0;
    double c = 2323405;
    double d = 76906;
    double n  = 2400391;

    double chi = (n * Math.pow((a * d) - (c * b) ,2)) / ((a + c) * (b + d) * (a + b) * (c + d));

    System.out.println("");
  }
}
