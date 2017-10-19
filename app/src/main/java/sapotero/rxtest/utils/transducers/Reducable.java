package sapotero.rxtest.utils.transducers;

interface Reducable<A> {
  <R> R reduce(Reducer<A,R> reducer);
}