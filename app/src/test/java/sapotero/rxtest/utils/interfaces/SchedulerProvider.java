package sapotero.rxtest.utils.interfaces;

import rx.Scheduler;

public interface SchedulerProvider {
  Scheduler ui();
  Scheduler computation();
  Scheduler trampoline();
  Scheduler newThread();
  Scheduler io();
}
