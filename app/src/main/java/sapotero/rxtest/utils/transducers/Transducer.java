package sapotero.rxtest.utils.transducers;

interface Transducer<A,B> {
  <R> Reducer<B,R> transduce(Reducer<A,R> reducer);
}