package sapotero.rxtest.utils.transducers.doc;

import java.util.ArrayList;
import java.util.List;

import sapotero.rxtest.utils.transducers.Reducer;

public class OrderHash implements Reducer<String, List<String>> {

  @Override
  public List<String> init() {
    return new ArrayList<>();
  }

  @Override
  public List<String> step(List<String> acc, String item) {
    acc.add(item);
    return acc;
  }
}