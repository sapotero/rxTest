package sapotero.rxtest.utils.transducers;

public interface Reducable<A> {
  <R> R reduce(Reducer<A,R> reducer);
}