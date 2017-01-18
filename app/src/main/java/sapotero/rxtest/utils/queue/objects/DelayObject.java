package sapotero.rxtest.utils.queue.objects;

import android.support.annotation.NonNull;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class DelayObject implements Delayed {
  private String data;
  private long startTime;

  public DelayObject(String data, long delay) {
    this.data = data;
    this.startTime = System.currentTimeMillis() + delay;
  }

  @Override
  public long getDelay(@NonNull TimeUnit unit) {
    long diff = startTime - System.currentTimeMillis();
    return unit.convert(diff, TimeUnit.MILLISECONDS);
  }

  @Override
  public int compareTo(@NonNull Delayed o) {
    if (this.startTime < ((DelayObject) o).startTime) {
      return -1;
    }
    if (this.startTime > ((DelayObject) o).startTime) {
      return 1;
    }
    return 0;
  }

  @Override
  public String toString() {
    return "{" +
      "data='" + data + '\'' +
      ", startTime=" + startTime +
      '}';
  }
}