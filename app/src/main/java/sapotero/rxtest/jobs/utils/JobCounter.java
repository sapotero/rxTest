package sapotero.rxtest.jobs.utils;

import com.f2prateek.rx.preferences.Preference;
import com.f2prateek.rx.preferences.RxSharedPreferences;

public class JobCounter {
  RxSharedPreferences settings;
  private Preference<Integer> count;
  private Preference<Integer> downloadFileJobCount;

  public JobCounter(RxSharedPreferences rxSharedPreferences) {
    settings = rxSharedPreferences;
    count = settings.getInteger("documents.count");
    downloadFileJobCount = settings.getInteger("images.count");
  }

  public int getJobCount() {
    return getIntegerFromSettings(count);
  }

  private int getIntegerFromSettings(Preference<Integer> integerPreference) {
    Integer value = integerPreference.get();

    if (value != null) {
      return value;
    } else {
      return 0;
    }
  }

  public void setJobCount(int value) {
    count.set(value);
  }

  public void addJobCount(int value) {
    setJobCount(getJobCount() + value);
  }

  public void incJobCount() {
    addJobCount(1);
  }

  public void decJobCount() {
    addJobCount(-1);
  }

  public int getDownloadFileJobCount() {
    return getIntegerFromSettings(downloadFileJobCount);
  }

  public void setDownloadFileJobCount(int value) {
    downloadFileJobCount.set(value);
  }

  public boolean isDownoadFileAlmostComplete() {
    return getDownloadFileJobCount() <= 3;
  }

  public void addDownloadFileJobCount(int value) {
    setDownloadFileJobCount(getDownloadFileJobCount() + value);
  }

  public void incDownloadFileJobCount() {
    addDownloadFileJobCount(1);
  }

  public void decDownloadFileJobCount() {
    addDownloadFileJobCount(-1);
  }
}
