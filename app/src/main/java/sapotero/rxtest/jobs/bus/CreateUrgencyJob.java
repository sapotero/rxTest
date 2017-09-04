package sapotero.rxtest.jobs.bus;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.CancelReason;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import java.util.ArrayList;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.db.requery.models.RUrgencyEntity;
import sapotero.rxtest.retrofit.models.document.Urgency;
import timber.log.Timber;


public class CreateUrgencyJob extends BaseJob {

  public static final int PRIORITY = 1;
  private final ArrayList<Urgency> urgencies;
  private String TAG = this.getClass().getSimpleName();

  public CreateUrgencyJob(ArrayList<Urgency> urgencies, String login) {
    super( new Params(PRIORITY).requireNetwork().persist() );
    this.urgencies = urgencies;
    this.login = login;
  }

  @Override
  public void onAdded() {
  }

  @Override
  public void onRun() throws Throwable {
    for (Urgency urgency : urgencies){
      if ( !exist( urgency.getId()) ){
        add(urgency);
      }
    }

  }

  private void add(Urgency urgency) {
    RUrgencyEntity data = new RUrgencyEntity();
    data.setUid( urgency.getId() );
    data.setCode( urgency.getCode() );
    data.setName( urgency.getName() );
    data.setUser( login );


    dataStore
      .insert(data)
      .toObservable()
      .subscribeOn(Schedulers.computation())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(u -> {
        Timber.tag(TAG).v("addByOne " + u.getName() );
      }, Timber::e);
  }


  @NonNull
  private Boolean exist(String uid){

    boolean result = false;

    Integer count = dataStore
      .count(RUrgencyEntity.UID)
      .where(RUrgencyEntity.UID.eq(uid))
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
