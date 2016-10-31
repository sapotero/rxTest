package sapotero.rxtest.jobs.bus;

import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.CancelReason;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import org.greenrobot.eventbus.EventBus;

import retrofit2.Retrofit;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.application.config.Constant;
import sapotero.rxtest.events.bus.UpdateAuthTokenEvent;
import sapotero.rxtest.retrofit.AuthTokenService;
import sapotero.rxtest.retrofit.models.AuthToken;
import sapotero.rxtest.retrofit.utils.RetrofitManager;
import timber.log.Timber;


public class UpdateAuthTokenJob extends BaseJob{
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
    Retrofit retrofit = new RetrofitManager( getApplicationContext(), Constant.HOST, okHttpClient).process();
    AuthTokenService authTokenService = retrofit.create( AuthTokenService.class );

    Observable<AuthToken> user = authTokenService.getAuth( settings.getString("login").get(), settings.getString("password").get() );

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


