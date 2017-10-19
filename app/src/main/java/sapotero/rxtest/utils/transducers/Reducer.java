package sapotero.rxtest.utils.transducers;

public interface Reducer<A,R> {
  R init();
  R step(R acc, A item);
}