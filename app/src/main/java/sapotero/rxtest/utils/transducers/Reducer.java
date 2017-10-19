package sapotero.rxtest.utils.transducers;

interface Reducer<A,R> {
  R init();
  R step(R acc, A item);
}