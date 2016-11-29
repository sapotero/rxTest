package sapotero.rxtest.views.managers.menu;

import android.content.Context;

import sapotero.rxtest.application.EsdApplication;

public class OperationManager {

  private final String TAG = this.getClass().getSimpleName();

  private Context context;
  private OperationManager instance;

  private String uid;

  Callback callback;

  public interface Callback {
    void onExecuteSuccess();
    void onExecuteError();
  }

  public void registerCallBack(Callback callback){
    this.callback = callback;
  }

  public OperationManager(Context context) {
    EsdApplication.getComponent(context).inject(this);

    this.context = context;
  }


  public void execute(String operation) {

  }
}
