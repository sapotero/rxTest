package sapotero.rxtest.jobs.bus;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.CancelReason;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import java.util.ArrayList;

import rx.schedulers.Schedulers;
import sapotero.rxtest.db.requery.models.RPrimaryConsiderationEntity;
import sapotero.rxtest.retrofit.models.Oshs;
import timber.log.Timber;


public class CreatePrimaryConsiderationJob extends BaseJob {

  public static final int PRIORITY = 1;
  private final ArrayList<Oshs> users;

  private String TAG = this.getClass().getSimpleName();

  public CreatePrimaryConsiderationJob(ArrayList<Oshs> users, String login) {
    super( new Params(PRIORITY).requireNetwork().persist() );
    this.users = users;
    this.login = login;
  }

  @Override
  public void onAdded() {
  }

  @Override
  public void onRun() throws Throwable {
    Timber.tag(TAG).i( "users: %s | %s", users.size(), users.get(0).getName() );
    int index = 0;
    for (Oshs user : users){
      if ( !exist( user.getId()) ){
        add(user, index);
      }
      index++;
    }

  }

  private void add(Oshs user, int index) {
    RPrimaryConsiderationEntity data = mappers.getPrimaryConsiderationMapper().withLogin(login).toEntity(user);
    data.setSortIndex(index);

    dataStore
      .insert(data)
      .toObservable()
      .subscribeOn(Schedulers.computation())
      .observeOn(Schedulers.computation())
      .subscribe(u -> {
        Timber.tag(TAG).v("addByOne " + u.getName() );
      }, Timber::e);
  }


  @NonNull
  private Boolean exist(String uid){

    boolean result = false;

    Integer count = dataStore
      .count(RPrimaryConsiderationEntity.UID)
      .where(RPrimaryConsiderationEntity.UID.eq(uid))
      .and(RPrimaryConsiderationEntity.USER.eq(login))
      .get().value();

    if( count != 0 ){
      result = true;
    }

    Timber.tag(TAG).v("exist " + result );

    return result;
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
