package sapotero.rxtest.jobs.bus;

import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.CancelReason;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.f2prateek.rx.preferences.Preference;

import org.greenrobot.eventbus.EventBus;

import retrofit2.Retrofit;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.events.bus.UpdateAuthTokenEvent;
import sapotero.rxtest.retrofit.Api.AuthService;
import sapotero.rxtest.retrofit.models.AuthSignToken;
import sapotero.rxtest.retrofit.utils.RetrofitManager;
import timber.log.Timber;


public class UpdateAuthTokenJob extends BaseJob {
  private static final int PRIORITY = 1;
  private String TOKEN;

  private String TAG = "UpdateAuthTokenJob";

  public UpdateAuthTokenJob() {
    super( new Params(PRIORITY).requireNetwork().persist() );

    TOKEN = "";
  }

  @Override
  public void onAdded() {
  }

  private void submitData( String TOKEN){
    EventBus.getDefault().post( new UpdateAuthTokenEvent( TOKEN ) );
  }

  @Override
  public void onRun() throws Throwable {

    Retrofit retrofit = new RetrofitManager( getApplicationContext(), settings2.getHost(), okHttpClient).process();
    AuthService auth = retrofit.create( AuthService.class );

    Observable<AuthSignToken> user = auth.getAuth( settings2.getLogin(), settings.getString("password").get() );

    user.subscribeOn( Schedulers.newThread() )
      .observeOn( AndroidSchedulers.mainThread() )
      .subscribe(
        data -> {
          submitData( data.getAuthToken() );
        },
        error -> {
          Timber.tag(TAG).d( "onRun " + error );
        }
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


