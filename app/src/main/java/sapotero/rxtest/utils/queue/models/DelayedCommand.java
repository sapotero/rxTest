package sapotero.rxtest.utils.queue.models;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import sapotero.rxtest.managers.menu.interfaces.Command;

public class DelayedCommand implements Delayed {
  private final Command command;
  private final long startTime;
  private final Context context;

  public DelayedCommand(Command data, Context context, long delay) {
    this.command = data;
    this.context = context;
    this.startTime = System.currentTimeMillis() + delay;
  }

  @Override
  public long getDelay(@NonNull TimeUnit unit) {
    long diff = startTime - System.currentTimeMillis();
    return unit.convert(diff, TimeUnit.MILLISECONDS);
  }

  @Override
  public int compareTo(@NonNull Delayed o) {
    if (this.startTime < ((DelayedCommand) o).startTime) {
      return -1;
    }
    if (this.startTime > ((DelayedCommand) o).startTime) {
      return 1;
    }
    return 0;
  }

  @Override
  public String toString() {
    return "{" +
      "command='" + command + '\'' +
      ", startTime=" + startTime +
      '}';
  }
}