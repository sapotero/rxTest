package sapotero.rxtest.managers.menu.commands.templates;

import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;

import java.util.List;
import java.util.Objects;

import retrofit2.Retrofit;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.db.mapper.TemplateMapper;
import sapotero.rxtest.db.requery.models.RTemplateEntity;
import sapotero.rxtest.events.decision.AddDecisionTemplateEvent;
import sapotero.rxtest.managers.menu.commands.AbstractCommand;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import sapotero.rxtest.retrofit.TemplatesService;
import sapotero.rxtest.retrofit.models.Template;
import timber.log.Timber;

public class CreateTemplate extends AbstractCommand {

  public CreateTemplate(CommandParams params) {
    super(params);
  }

  @Override
  public String getType() {
    return "create_template";
  }

  @Override
  public void executeLocal() {
    addToQueue();
    queueManager.setExecutedLocal(this);
  }

  @Override
  public void executeRemote() {
    Retrofit retrofit = getRetrofit();

    TemplatesService templatesService = retrofit.create( TemplatesService.class );

    String type = null;
    if ( getParams().getLabel() != null && !Objects.equals(getParams().getLabel(), "decision")){
      type = getParams().getLabel();
    }

    Observable<Template> info = templatesService.create(
      getParams().getLogin(),
      settings.getToken(),
      getParams().getComment(),
      type
    );

    info
      .subscribeOn( Schedulers.computation() )
      .observeOn( AndroidSchedulers.mainThread() )
      .subscribe(
        data -> {
          queueManager.setExecutedRemote(this);
          insertTemplate(data);
        },
        this::onOperationError
      );
  }

  private void insertTemplate(Template data) {
    RTemplateEntity template = new TemplateMapper().withLogin(getParams().getLogin()).toEntity(data);
    template.setType(getParams().getLabel());

    dataStore
      .insert(template)
      .toObservable()
      .subscribeOn( Schedulers.computation() )
      .observeOn( AndroidSchedulers.mainThread() )
      .subscribe(
        temp -> {
          Timber.tag(TAG).i("success");
          Timber.tag(TAG).i("%s", new Gson().toJson(temp));

          EventBus.getDefault().post( new AddDecisionTemplateEvent() );
        },
        error -> Timber.tag(TAG).e(error)
      );
  }

  @Override
  public void finishOnOperationError(List<String> errors) {
    queueManager.setExecutedWithError( this, errors );
  }
}
