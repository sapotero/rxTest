package sapotero.rxtest.utils.transducers;

public class Sum implements Reducer<Long,Long> {
  public Long init() { return 0L; }
  public Long step(Long acc, Long item) { return acc + item; }
}