package sapotero.rxtest.utils.transducers.queue;

import java.util.List;

import sapotero.rxtest.utils.queue.models.CommandInfo;
import sapotero.rxtest.utils.transducers.Reducable;
import sapotero.rxtest.utils.transducers.Reducer;

public class QueueReader implements Reducable<CommandInfo> {

  private final List<CommandInfo>  docs;

  QueueReader(List<CommandInfo> docs) {
    this.docs = docs;
  }

  public <R> R reduce(Reducer<CommandInfo, R> reducer) {
    R acc = reducer.init();

    for (CommandInfo doc : docs) {
      acc = reducer.step(acc, doc);
    }
    return acc;
  }
}