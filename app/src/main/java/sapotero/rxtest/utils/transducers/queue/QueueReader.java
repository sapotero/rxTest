package sapotero.rxtest.utils.transducers.queue;

import java.util.Collection;
import java.util.List;

import sapotero.rxtest.utils.queue.models.CommandInfo;
import sapotero.rxtest.utils.transducers.Reducable;
import sapotero.rxtest.utils.transducers.Reducer;

public class QueueReader implements Reducable<CommandInfo> {

  private final Collection<List<CommandInfo>>  docs;

  QueueReader(Collection<List<CommandInfo>> docs) {
    this.docs = docs;
  }

  public <R> R reduce(Reducer<CommandInfo, R> reducer) {
    R acc = reducer.init();

    for (List<CommandInfo> doc : docs) {
      acc = reducer.step(acc, doc.get(0));
    }
    return acc;
  }
}