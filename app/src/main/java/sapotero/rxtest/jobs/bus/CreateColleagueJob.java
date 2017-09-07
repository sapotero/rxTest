package sapotero.rxtest.jobs.bus;

import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.CancelReason;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.db.mapper.ColleagueMapper;
import sapotero.rxtest.db.requery.models.RColleagueEntity;
import sapotero.rxtest.events.view.UpdateDrawerEvent;
import sapotero.rxtest.retrofit.models.Colleague;
import timber.log.Timber;


public class CreateColleagueJob extends BaseJob {

  public static final int PRIORITY = 1;
  private final ArrayList<Colleague> users;

  private String TAG = this.getClass().getSimpleName();

  public CreateColleagueJob(ArrayList<Colleague> users, String login) {
    super( new Params(PRIORITY).requireNetwork().persist() );
    this.users = users;
    this.login = login;
  }

  @Override
  public void onAdded() {
  }

  @Override
  public void onRun() throws Throwable {
    // resolved https://tasks.n-core.ru/browse/MPSED-2134
    // 2.Списки группы избр. моб клиент, первичн рассмотр, врио, по поручен, Коллеги, шаблоны, папки сбрасываются в базе при смене пользователя
    // Удаляем старых коллег непосредственно перед записью новых
    dataStore
      .delete(RColleagueEntity.class)
      .where(RColleagueEntity.USER.eq(login))
      .get().value();

    int index = 0;

    List<RColleagueEntity> colleagueEntityList = new ArrayList<>();
    ColleagueMapper mapper = mappers.getColleagueMapper().withLogin(login);

    for (Colleague user : users) {
      RColleagueEntity colleagueEntity = mapper.toEntity(user);
      colleagueEntity.setSortIndex(index);
      colleagueEntityList.add(colleagueEntity);
      index++;
    }

    dataStore
      .insert(colleagueEntityList)
      .toObservable()
      .subscribeOn(Schedulers.computation())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(
        u -> {
          Timber.tag(TAG).v("Added colleagues");
          // Update drawer only once after all colleagues created
          EventBus.getDefault().post( new UpdateDrawerEvent() );
        },
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