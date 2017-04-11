package sapotero.rxtest.jobs.bus;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.CancelReason;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import java.util.ArrayList;

import rx.schedulers.Schedulers;
import sapotero.rxtest.db.requery.models.RTemplateEntity;
import sapotero.rxtest.retrofit.models.Template;
import timber.log.Timber;


public class CreateTemplatesJob extends BaseJob {

  public static final int PRIORITY = 1;
  private final ArrayList<Template> templates;

  private String TAG = this.getClass().getSimpleName();

  public CreateTemplatesJob(ArrayList<Template> templates) {
    super( new Params(PRIORITY).requireNetwork().persist() );
    this.templates = templates;
  }

  @Override
  public void onAdded() {
  }

  @Override
  public void onRun() throws Throwable {
//    Timber.tag(TAG).i( "templates: %s | %s", templates.size(), templates.get(0).getText() );
    for (Template template : templates){
      if ( !exist( template.getId()) ){
        add(template);
      }
    }

  }

  private void add(Template template) {
    RTemplateEntity data = new RTemplateEntity();
    data.setUid( template.getId() );
    data.setTitle( template.getText() );
    data.setType( template.getType() );
    data.setUser( settings.getString("current_user").get() );


    dataStore
      .insert(data)
      .toObservable()
      .subscribeOn(Schedulers.computation())
      .observeOn(Schedulers.io())
      .subscribe(u -> {
        Timber.tag(TAG).v("addByOne " + u.getTitle() );
      });
  }


  @NonNull
  private Boolean exist(String uid){

    boolean result = false;

    Integer count = dataStore
      .count(RTemplateEntity.UID)
      .where(RTemplateEntity.UID.eq(uid))
      .get().value();

    if( count != 0 ){
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
