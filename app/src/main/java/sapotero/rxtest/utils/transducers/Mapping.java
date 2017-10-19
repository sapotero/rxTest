package sapotero.rxtest.utils.transducers;

public class Mapping<A,B> implements Transducer<B,A> {
  final Function<A,B> f;
  public Mapping(Function<A,B> f) { this.f = f; }

  public <R> Reducer<A,R> transduce(final Reducer<B,R> reducer) {
    return new Reducer<A,R>() {
      public R init() { return reducer.init(); }
      public R step(R acc, A item) { return reducer.step(acc, f.apply(item)); }
    };
  }
}