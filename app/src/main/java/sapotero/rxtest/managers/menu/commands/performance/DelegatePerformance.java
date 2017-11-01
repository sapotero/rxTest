package sapotero.rxtest.managers.menu.commands.performance;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Retrofit;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.events.view.ShowNextDocumentEvent;
import sapotero.rxtest.managers.menu.commands.AbstractCommand;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import sapotero.rxtest.retrofit.OperationService;
import sapotero.rxtest.retrofit.models.OperationResult;
import timber.log.Timber;

public class DelegatePerformance extends AbstractCommand {

  public DelegatePerformance(CommandParams params) {
    super(params);
  }

  public String getInfo(){
    return null;
  }

  @Override
  public String getType() {
    return "delegate_performance";
  }

  @Override
  public void executeLocal() {
    EventBus.getDefault().post( new ShowNextDocumentEvent( getParams().getDocument() ));

    Timber.tag(TAG).i( "type: %s", this.getClass().getName() );

    Retrofit retrofit = getOperationsRetrofit();

    OperationService operationService = retrofit.create( OperationService.class );

    ArrayList<String> uids = new ArrayList<>();
    uids.add( getParams().getDocument() );

    Observable<OperationResult> info = operationService.performance(
      getType(),
      getParams().getLogin(),
      settings.getToken(),
      uids,
      getParams().getDocument(),
      getParams().getStatusCode(),
      getParams().getPerson()
    );

    info.subscribeOn( Schedulers.computation() )
      .observeOn( AndroidSchedulers.mainThread() )
      .subscribe(
        data -> {
          Timber.tag(TAG).i("ok: %s", data.getOk());
          Timber.tag(TAG).i("error: %s", data.getMessage());
          Timber.tag(TAG).i("type: %s", data.getType());
        },
        error -> {
        }
      );

    setAsProcessed();
  }

  @Override
  public void executeRemote() {

  }

  @Override
  public void finishOnOperationError(List<String> errors) {
  }
}
