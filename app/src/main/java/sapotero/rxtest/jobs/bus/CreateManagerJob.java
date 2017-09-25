package sapotero.rxtest.jobs.bus;

import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.CancelReason;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import java.util.ArrayList;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.db.mapper.ManagerMapper;
import sapotero.rxtest.db.requery.models.RManagerEntity;
import sapotero.rxtest.retrofit.models.Oshs;
import timber.log.Timber;


public class CreateManagerJob extends BaseJob {

  public static final int PRIORITY = 1;
  private final ArrayList<Oshs> users;

  private String TAG = this.getClass().getSimpleName();


  public CreateManagerJob(ArrayList<Oshs> users, String login) {
    super( new Params(PRIORITY).requireNetwork().persist() );
    this.users = users;
    this.login = login;
  }

  @Override
  public void onAdded() {
  }

  @Override
  public void onRun() throws Throwable {
    dataStore
      .delete(RManagerEntity.class)
      .where(RManagerEntity.USER.eq(login))
      .get().value();


    List<RManagerEntity> managerEntityList = new ArrayList<>();
    ManagerMapper mapper = new ManagerMapper().withLogin(login);

    for (Oshs user : users) {
      RManagerEntity managerEntity = mapper.toEntity(user);
      managerEntityList.add(managerEntity);
    }

    dataStore
      .insert(managerEntityList)
      .toObservable()
      .subscribeOn(Schedulers.computation())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(
        u -> Timber.tag(TAG).v("Added managers"),
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