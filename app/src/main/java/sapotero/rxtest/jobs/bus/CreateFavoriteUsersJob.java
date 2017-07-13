package sapotero.rxtest.jobs.bus;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.CancelReason;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import java.util.ArrayList;

import rx.schedulers.Schedulers;
import sapotero.rxtest.db.requery.models.RFavoriteUserEntity;
import sapotero.rxtest.retrofit.models.Oshs;
import timber.log.Timber;


public class CreateFavoriteUsersJob extends BaseJob {

  public static final int PRIORITY = 1;

  // Needed to show favorite users after assistants in SelectOshsDialogFragment
  public static final int INDEX_INIT_VALUE = 10000;

  private final ArrayList<Oshs> users;

  private String TAG = this.getClass().getSimpleName();

  public CreateFavoriteUsersJob(ArrayList<Oshs> users) {
    super( new Params(PRIORITY).requireNetwork().persist() );
    this.users = users;
  }

  @Override
  public void onAdded() {
  }

  @Override
  public void onRun() throws Throwable {
    try {
      Timber.tag(TAG).i( "users: %s | %s", users.size(), users.get(0).getName() );
      int index = INDEX_INIT_VALUE;
      for (Oshs user : users){
        if ( !exist( user.getId()) ){
          add(user, index);
        }
        index++;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  private void add(Oshs user, int index) {
    RFavoriteUserEntity data = mappers.getFavoriteUserMapper().toEntity(user);
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
      .count(RFavoriteUserEntity.UID)
      .where(RFavoriteUserEntity.UID.eq(uid))
      .and(RFavoriteUserEntity.USER.eq(settings.getLogin()))
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
