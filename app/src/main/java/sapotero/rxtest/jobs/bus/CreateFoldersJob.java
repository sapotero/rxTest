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
    // resolved https://tasks.n-core.ru/browse/MPSED-2134
    // 2.Списки группы избр. моб клиент, первичн рассмотр, врио, по поручен, Коллеги, шаблоны, папки сбрасываются в базе при смене пользователя
    // Удаляем старые папки непосредственно перед записью новых
    dataStore
      .delete(RFolderEntity.class)
      .where(RFolderEntity.USER.eq(login))
      .get().value();

    List<RFolderEntity> folderEntityList = new ArrayList<>();

    for (Folder template : templates) {
      RFolderEntity folderEntity = new RFolderEntity();
      folderEntity.setUid( template.getId() );
      folderEntity.setTitle( template.getTitle() );
      folderEntity.setType( template.getType() );
      folderEntity.setUser( login );

      folderEntityList.add(folderEntity);
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

  @Override
  protected RetryConstraint shouldReRunOnThrowable(Throwable throwable, int runCount, int maxRunCount) {
    return RetryConstraint.createExponentialBackoff(runCount, 1000);
  }

  @Override
  protected void onCancel(@CancelReason int cancelReason, @Nullable Throwable throwable) {
    // Job has exceeded retry attempts or shouldReRunOnThrowable() has decided to cancel.
  }
}
