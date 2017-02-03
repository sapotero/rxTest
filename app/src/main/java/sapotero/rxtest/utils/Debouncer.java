package sapotero.rxtest.utils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Debouncer {
  private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
  private final ConcurrentHashMap<Object, Future<?>> delayedMap = new ConcurrentHashMap<>();

  public void debounce(final Object key, final Runnable runnable, long delay, TimeUnit unit) {
    final Future<?> prev = delayedMap.put(key, scheduler.schedule(() -> {
      try {
        runnable.run();
      } finally {
        delayedMap.remove(key);
      }
    }, delay, unit));
    if (prev != null) {
      prev.cancel(true);
    }
  }

  public void shutdown() {
    scheduler.shutdownNow();
  }
}