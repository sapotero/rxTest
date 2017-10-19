package sapotero.rxtest.utils.transducers;

interface Function<A,B> {
  B apply(A a);
}