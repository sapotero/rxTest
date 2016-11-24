package sapotero.rxtest.jobs.bus;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.CancelReason;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import java.util.ArrayList;

import rx.singles.BlockingSingle;
import sapotero.rxtest.db.requery.models.RPrimaryConsiderationEntity;
import sapotero.rxtest.retrofit.models.Oshs;
import timber.log.Timber;


public class AddPrimaryConsiderationJob  extends BaseJob {

  public static final int PRIORITY = 1;
  private final ArrayList<Oshs> users;

  private String TAG = this.getClass().getSimpleName();

  public AddPrimaryConsiderationJob(ArrayList<Oshs> users) {
    super( new Params(PRIORITY).requireNetwork().persist() );
    this.users = users;
  }

  @Override
  public void onAdded() {
  }

  @Override
  public void onRun() throws Throwable {

    for (Oshs user : users){
      if ( !exist( user.getId()) ){
        add(user);
      }
    }

  }

  private void add(Oshs user) {
    RPrimaryConsiderationEntity data = new RPrimaryConsiderationEntity();
    data.setOrganization( user.getOrganization() );
    data.setFirstName( user.getFirstName() );
    data.setLastName( user.getLastName() );
    data.setMiddleName( user.getMiddleName() );
    data.setGender( user.getGender() );
    data.setPosition( user.getPosition() );
    data.setUid( user.getId() );
    data.setName( user.getName() );
    data.setIsGroup( user.getIsGroup() );
    data.setIsOrganization( user.getIsOrganization() );

    BlockingSingle<RPrimaryConsiderationEntity> u = dataStore.insert(data).toBlocking();
  }


  @NonNull
  private Boolean exist(String uid){

    boolean result = false;

    Integer count = dataStore
      .count(RPrimaryConsiderationEntity.UID)
      .where(RPrimaryConsiderationEntity.UID.eq(uid))
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
