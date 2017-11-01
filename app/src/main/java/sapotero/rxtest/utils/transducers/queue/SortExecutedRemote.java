package sapotero.rxtest.utils.transducers.queue;

import java.util.ArrayList;
import java.util.List;

import sapotero.rxtest.utils.queue.models.CommandInfo;
import sapotero.rxtest.utils.transducers.Reducer;

public class SortExecutedRemote implements Reducer<CommandInfo, List<CommandInfo>> {

  private final Boolean isExecutedRemote;

  SortExecutedRemote(Boolean remote) {
    this.isExecutedRemote = remote;
  }

  public List<CommandInfo> init() {return new ArrayList<CommandInfo>() {
    };
  }

  public List<CommandInfo> step ( List<CommandInfo> acc, CommandInfo item ) {

    if ( item.isExecutedRemote() == isExecutedRemote ){
      acc.add(item);
    }

    return acc;
  }
}