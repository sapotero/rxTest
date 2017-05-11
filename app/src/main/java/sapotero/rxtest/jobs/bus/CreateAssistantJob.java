package sapotero.rxtest.jobs.bus;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.CancelReason;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import java.util.ArrayList;

import rx.schedulers.Schedulers;
import sapotero.rxtest.db.requery.models.RAssistantEntity;
import sapotero.rxtest.retrofit.models.Assistant;
import timber.log.Timber;


public class CreateAssistantJob extends BaseJob {

  public static final int PRIORITY = 1;
  private final ArrayList<Assistant> users;

  private String TAG = this.getClass().getSimpleName();

  public CreateAssistantJob(ArrayList<Assistant> users) {
    super( new Params(PRIORITY).requireNetwork().persist() );
    this.users = users;
  }

  @Override
  public void onAdded() {
  }

  @Override
  public void onRun() throws Throwable {
    for (Assistant user : users){
      if ( !exist( user.getAssistantId()) ){
        add(user);
      }
    }

  }

  private void add(Assistant user) {
    RAssistantEntity data = new RAssistantEntity();
    data.setTitle( user.getToS() );
    data.setAssistantId( user.getAssistantId() );
    data.setAssistantName( user.getAssistantName() );
    data.setForDecision( user.getForDecision() );
    data.setHeadId( user.getHeadId() );
    data.setHeadName( user.getHeadName() );
    data.setUser( settings2.getLogin() );


    dataStore
      .insert(data)
      .toObservable()
      .subscribeOn(Schedulers.computation())
      .observeOn(Schedulers.computation())
      .subscribe(u -> {
        Timber.tag(TAG).v("addByOne " + u.getTitle() );
      });
  }


  @NonNull
  private Boolean exist(String id){

    boolean result = false;

    Integer count = dataStore
      .count(RAssistantEntity.ASSISTANT_ID)
      .where(RAssistantEntity.ASSISTANT_ID.eq(id))
      .and(RAssistantEntity.USER.eq(settings2.getLogin()))
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