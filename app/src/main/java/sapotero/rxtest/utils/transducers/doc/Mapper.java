package sapotero.rxtest.utils.transducers.doc;

import sapotero.rxtest.utils.transducers.Function;
import sapotero.rxtest.utils.transducers.Reducer;
import sapotero.rxtest.utils.transducers.Transducer;

public class Mapper<A,B> implements Transducer<B,A> {
  final Function<A,B> f;
  public Mapper(Function<A,B> f) { this.f = f; }

  public <R> Reducer<A,R> transduce(final Reducer<B,R> reducer) {
    return new Reducer<A,R>() {
      public R init() { return reducer.init(); }
      public R step(R acc, A item) { return reducer.step(acc, f.apply(item)); }
    };
  }
}