package sapotero.rxtest.jobs.bus;

import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.CancelReason;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.db.mapper.TemplateMapper;
import sapotero.rxtest.db.requery.models.RTemplateEntity;
import sapotero.rxtest.events.decision.AddDecisionTemplateEvent;
import sapotero.rxtest.retrofit.models.Template;
import timber.log.Timber;


public class CreateTemplatesJob extends BaseJob {

  public static final int PRIORITY = 1;
  private final ArrayList<Template> templates;
  private final String type;

  private String TAG = this.getClass().getSimpleName();

  public CreateTemplatesJob(ArrayList<Template> templates, String type, String login) {
    super( new Params(PRIORITY).requireNetwork().persist() );
    this.templates = templates;
    this.type = type;
    this.login = login;
  }

  @Override
  public void onAdded() {
  }

  @Override
  public void onRun() throws Throwable {
    String templateType = type != null && !Objects.equals(type, "") ? type : "decision";

    // resolved https://tasks.n-core.ru/browse/MPSED-2134
    // 2.Списки группы избр. моб клиент, первичн рассмотр, врио, по поручен, Коллеги, шаблоны, папки сбрасываются в базе при смене пользователя
    // Удаляем старые шаблоны непосредственно перед записью новых
    dataStore
      .delete(RTemplateEntity.class)
      .where(RTemplateEntity.USER.eq(login))
      .and(RTemplateEntity.TYPE.eq(templateType))
      .get().value();

    List<RTemplateEntity> templateEntityList = new ArrayList<>();
    TemplateMapper mapper = new TemplateMapper().withLogin(login);

    for (Template template : templates) {
      RTemplateEntity templateEntity = mapper.toEntity(template);
      templateEntity.setType( templateType );
      templateEntityList.add(templateEntity);
    }

    dataStore
      .insert(templateEntityList)
      .toObservable()
      .subscribeOn(Schedulers.computation())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(
        u -> {
          Timber.tag(TAG).v("Added templates");
          EventBus.getDefault().post( new AddDecisionTemplateEvent() );
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
