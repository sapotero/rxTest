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
import sapotero.rxtest.db.requery.models.RFolderEntity;
import sapotero.rxtest.events.bus.FolderCreatedEvent;
import sapotero.rxtest.retrofit.models.Folder;
import timber.log.Timber;


public class CreateFoldersJob extends BaseJob {

  public static final int PRIORITY = 1;
  private final ArrayList<Folder> templates;

  private String TAG = this.getClass().getSimpleName();

  public CreateFoldersJob(ArrayList<Folder> templates, String login) {
    super( new Params(PRIORITY).requireNetwork().persist() );
    this.templates = templates;
    this.login = login;
  }

  @Override
  public void onAdded() {
  }

  @Override
  public void onRun() throws Throwable {
    List<RFolderEntity> folderEntityList = new ArrayList<>();

    for (Folder template : templates) {
      // resolved https://tasks.n-core.ru/browse/MPSED-2134
      // Не работает добавление/удаление в избранное, если перезайти в режимы замещения.
      // также не работает добавление в избранное в режиме замещения
      // Не удаляем папки, а добавляем те, которых нет в базе
      if ( !exist( template.getId() ) ) {
        RFolderEntity folderEntity = new RFolderEntity();
        folderEntity.setUid( template.getId() );
        folderEntity.setTitle( template.getTitle() );
        folderEntity.setType( template.getType() );
        folderEntity.setUser( login );

        folderEntityList.add(folderEntity);
      }
    }

    dataStore
      .insert(folderEntityList)
      .toObservable()
      .subscribeOn(Schedulers.computation())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(
        u -> {
          Timber.tag(TAG).v("Added folders");
          EventBus.getDefault().post( new FolderCreatedEvent() );
        },
        Timber::e
      );
  }

  private boolean exist(String uid) {
    boolean result = false;

    int count = dataStore
      .count(RFolderEntity.UID)
      .where(RFolderEntity.UID.eq(uid))
      .and(RFolderEntity.USER.eq(login))
      .get().value();

    if ( count != 0 ) {
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
