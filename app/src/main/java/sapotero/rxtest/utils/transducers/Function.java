package sapotero.rxtest.utils.transducers;

public interface Function<A,B> {
  B apply(A a);
}