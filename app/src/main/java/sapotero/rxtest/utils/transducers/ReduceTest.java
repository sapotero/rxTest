package sapotero.rxtest.utils.transducers;

import timber.log.Timber;

public class ReduceTest {
  public static void calculate(String words[]) {

    Function<String,Long> decode = Long::valueOf;

    Reducer<Long,Long> sum = new Sum();

    Reducer<String,Long> process = new Mapping(decode).transduce(sum);

    WordReader inputs = new WordReader(words);

    Long result = inputs.reduce(process);
    Timber.e("sum = %s" , result);
  }
}