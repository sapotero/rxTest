package sapotero.rxtest.jobs.bus;

import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.CancelReason;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import java.util.ArrayList;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.db.mapper.AssistantMapper;
import sapotero.rxtest.db.requery.models.RAssistantEntity;
import sapotero.rxtest.retrofit.models.Assistant;
import timber.log.Timber;


public class CreateAssistantJob extends BaseJob {

  public static final int PRIORITY = 1;
  private final ArrayList<Assistant> users;

  private String TAG = this.getClass().getSimpleName();

  public CreateAssistantJob(ArrayList<Assistant> users, String login) {
    super( new Params(PRIORITY).requireNetwork().persist() );
    this.users = users;
    this.login = login;
  }

  @Override
  public void onAdded() {
  }

  @Override
  public void onRun() throws Throwable {
    int index = 0;

    List<RAssistantEntity> assistantEntityList = new ArrayList<>();
    AssistantMapper mapper = mappers.getAssistantMapper().withLogin(login);

    for (Assistant user : users) {
      RAssistantEntity assistantEntity = mapper.toEntity(user);
      assistantEntity.setSortIndex(index);
      assistantEntityList.add(assistantEntity);
      index++;
    }

    dataStore
      .insert(assistantEntityList)
      .toObservable()
      .subscribeOn(Schedulers.computation())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(
        u -> Timber.tag(TAG).v("Added assistants"),
        Timber::e
      );
  }

  @Override
  protected RetryConstraint shouldReRunOnThrowable(Throwable throwable, int runCount, int maxRunCount) {
    return RetryConstraint.createExponentialBackoff(runCount, 1000);
  }

  @Override
  protected void onCancel(@CancelReason int cancelReason, @Nullable Throwable throwable) {
    // Job has exceeded retry attempts or shouldReRunOnThrowable() has decided to cancel.
  }
}