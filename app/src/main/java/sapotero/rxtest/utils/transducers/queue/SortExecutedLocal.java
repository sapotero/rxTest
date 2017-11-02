package sapotero.rxtest.utils.transducers.queue;

import java.util.ArrayList;
import java.util.List;

import sapotero.rxtest.utils.queue.models.CommandInfo;
import sapotero.rxtest.utils.transducers.Reducer;

public class SortExecutedLocal implements Reducer<CommandInfo, List<CommandInfo>> {

  private final Boolean isExecutedLocal;

  SortExecutedLocal(Boolean local) {
    this.isExecutedLocal = local;
  }

  public List<CommandInfo> init() {return new ArrayList<CommandInfo>() {
    };
  }

  public List<CommandInfo> step ( List<CommandInfo> acc, CommandInfo item ) {

    if ( item.isExecutedLocal() == isExecutedLocal){
      acc.add(item);
    }

    return acc;
  }
}