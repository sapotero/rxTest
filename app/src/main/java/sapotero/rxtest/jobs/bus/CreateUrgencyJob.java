package sapotero.rxtest.jobs.bus;

import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.CancelReason;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import java.util.ArrayList;
import java.util.List;

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
    // resolved https://tasks.n-core.ru/browse/MPSED-2134
    // 2.Списки группы избр. моб клиент, первичн рассмотр, врио, по поручен, Коллеги, шаблоны, папки сбрасываются в базе при смене пользователя
    // Удаляем старые срочности непосредственно перед записью новых
    dataStore
      .delete(RUrgencyEntity.class)
      .where(RUrgencyEntity.USER.eq(login))
      .get().value();

    List<RUrgencyEntity> urgencyEntityList = new ArrayList<>();

    for (Urgency urgency : urgencies) {
      RUrgencyEntity urgencyEntity = new RUrgencyEntity();
      urgencyEntity.setUid( urgency.getId() );
      urgencyEntity.setCode( urgency.getCode() );
      urgencyEntity.setName( urgency.getName() );
      urgencyEntity.setUser( login );

      urgencyEntityList.add(urgencyEntity);
    }

    dataStore
      .insert(urgencyEntityList)
      .toObservable()
      .subscribeOn(Schedulers.computation())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(
        u -> Timber.tag(TAG).v("Added urgencies"),
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
