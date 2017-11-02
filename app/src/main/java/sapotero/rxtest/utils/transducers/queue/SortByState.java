package sapotero.rxtest.utils.transducers.queue;

import java.util.ArrayList;
import java.util.List;

import sapotero.rxtest.utils.queue.models.CommandInfo;
import sapotero.rxtest.utils.transducers.Reducer;

public class SortByState implements Reducer<CommandInfo, List<CommandInfo>> {

  private final CommandInfo.STATE state;

  SortByState(CommandInfo.STATE state) {
    this.state    = state;
  }

  public List<CommandInfo> init() {
    return new ArrayList<CommandInfo>() {};
  }

  public List<CommandInfo> step ( List<CommandInfo> acc, CommandInfo item ) {

    if ( item.getState() == state ){
      acc.add(item);
    }

    return acc;
  }
}