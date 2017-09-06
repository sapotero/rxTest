package sapotero.rxtest.jobs.bus;

import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.CancelReason;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import java.util.ArrayList;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.db.mapper.FavoriteUserMapper;
import sapotero.rxtest.db.requery.models.RFavoriteUserEntity;
import sapotero.rxtest.retrofit.models.Oshs;
import timber.log.Timber;


public class CreateFavoriteUsersJob extends BaseJob {

  public static final int PRIORITY = 1;

  // Needed to show favorite users after assistants in SelectOshsDialogFragment
  private static final int INDEX_INIT_VALUE = 10000;

  private final ArrayList<Oshs> users;

  private String TAG = this.getClass().getSimpleName();

  public CreateFavoriteUsersJob(ArrayList<Oshs> users, String login) {
    super( new Params(PRIORITY).requireNetwork().persist() );
    this.users = users;
    this.login = login;
  }

  @Override
  public void onAdded() {
  }

  @Override
  public void onRun() throws Throwable {
    int index = INDEX_INIT_VALUE;

    List<RFavoriteUserEntity> favoriteUserEntityList = new ArrayList<>();
    FavoriteUserMapper mapper = mappers.getFavoriteUserMapper().withLogin(login);

    for (Oshs user : users) {
      RFavoriteUserEntity favoriteUserEntity = mapper.toEntity(user);
      favoriteUserEntity.setSortIndex(index);
      favoriteUserEntityList.add(favoriteUserEntity);
      index++;
    }

    dataStore
      .insert(favoriteUserEntityList)
      .toObservable()
      .subscribeOn(Schedulers.computation())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(
        u -> Timber.tag(TAG).v("Added favorite users"),
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
