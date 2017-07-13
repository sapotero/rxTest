package sapotero.rxtest.jobs.bus;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.CancelReason;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import java.util.ArrayList;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.db.requery.models.RColleagueEntity;
import sapotero.rxtest.retrofit.models.Colleague;
import timber.log.Timber;


public class CreateColleagueJob extends BaseJob {

  public static final int PRIORITY = 1;
  private final ArrayList<Colleague> users;

  private String TAG = this.getClass().getSimpleName();

  public CreateColleagueJob(ArrayList<Colleague> users) {
    super( new Params(PRIORITY).requireNetwork().persist() );
    this.users = users;
  }

  @Override
  public void onAdded() {
  }

  @Override
  public void onRun() throws Throwable {
    int index = 0;
    for (Colleague user : users){
      if ( !exist( user.getColleagueId()) ){
        add(user, index);
      }
      index++;
    }

  }

  private void add(Colleague user, int index) {
    RColleagueEntity data = mappers.getColleagueMapper().toEntity(user);
    data.setSortIndex(index);

    dataStore
      .insert(data)
      .toObservable()
      .subscribeOn(Schedulers.computation())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(u -> {
        Timber.tag(TAG).v("addByOne " + u.getOfficialName() );
      }, Timber::e);
  }


  @NonNull
  private Boolean exist(String user){

    boolean result = false;

    Integer count = dataStore
      .count(RColleagueEntity.COLLEAGUE_ID)
      .where(RColleagueEntity.COLLEAGUE_ID.eq(user))
      .and(RColleagueEntity.USER.eq(settings.getLogin()))
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