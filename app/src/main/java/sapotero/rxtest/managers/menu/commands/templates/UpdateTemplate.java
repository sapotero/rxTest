package sapotero.rxtest.managers.menu.commands.templates;

import java.util.List;

import retrofit2.Retrofit;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.managers.menu.commands.AbstractCommand;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import sapotero.rxtest.retrofit.TemplatesService;
import sapotero.rxtest.retrofit.models.Template;

public class UpdateTemplate extends AbstractCommand {

  public UpdateTemplate(CommandParams params) {
    super(params);
  }

  @Override
  public String getType() {
    return "update_template";
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

    Observable<Template> info = templatesService.update(
      getParams().getUuid(),
      getParams().getLogin(),
      settings.getToken(),
      getParams().getComment()
    );

    info
      .subscribeOn( Schedulers.computation() )
      .observeOn( AndroidSchedulers.mainThread() )
      .subscribe(
        data -> {
          queueManager.setExecutedRemote(this);
        },
        this::onOperationError
      );
  }

  @Override
  public void finishOnOperationError(List<String> errors) {
    queueManager.setExecutedWithError( this, errors );
  }
}
