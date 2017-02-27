package sapotero.rxtest.views.custom.stepper.build.steps;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.f2prateek.rx.preferences.Preference;
import com.f2prateek.rx.preferences.RxSharedPreferences;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.netopen.hotbitmapgg.library.view.RingProgressBar;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.events.stepper.auth.StepperLoginCheckFailEvent;
import sapotero.rxtest.events.stepper.load.StepperLoadDocumentEvent;
import sapotero.rxtest.events.stepper.shared.StepperNextStepEvent;
import sapotero.rxtest.views.custom.stepper.Step;
import sapotero.rxtest.views.custom.stepper.VerificationError;
import timber.log.Timber;

public class StepperLoadDataFragment extends Fragment implements Step {

  @Inject RxSharedPreferences settings;

  private String TAG = this.getClass().getSimpleName();
  private int loaded = 0;

  private RingProgressBar mRingProgressBar;

  private Preference<Integer> COUNT;
  private Preference<Boolean> IS_CONNECTED;

  private VerificationError error;
  private CompositeSubscription subscription;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.stepper_test_step, container, false);

    if ( EventBus.getDefault().isRegistered(this) ) {
      EventBus.getDefault().unregister(this);
    }
    EventBus.getDefault().register(this);
    EsdApplication.getComponent(getContext()).inject(this);

    loadRxSettings();

    mRingProgressBar = (RingProgressBar) view.findViewById(R.id.stepper_test_ring_progress);
    mRingProgressBar.setProgress(0);
    mRingProgressBar.setOnProgressListener(() -> {
      Timber.tag(TAG).v( "mRingProgressBar value: complete");
    });

    if (subscription == null){
      subscription = new CompositeSubscription();
    }

    if (subscription.hasSubscriptions()){
      subscription.unsubscribe();
    }

    subscription.add(
      Observable
      .interval( 2, TimeUnit.SECONDS)
      .subscribeOn(AndroidSchedulers.mainThread())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe( data-> {
        int value = mRingProgressBar.getProgress();
        mRingProgressBar.setProgress( value + 1 );

        if ( getLoadedDocumentsPercent() > value ){
          subscription.unsubscribe();
          mRingProgressBar.setOnProgressListener(null);
          mRingProgressBar = null;
          EventBus.getDefault().post( new StepperNextStepEvent() );
        }

      })
    );

    error = new VerificationError("Подождите загрузки документов");



    return view;
  }
  @Override
  public void onDestroy(){
    super.onDestroy();

    if ( EventBus.getDefault().isRegistered(this) ) {
      EventBus.getDefault().unregister(this);
    }

  }

  @Override
  @StringRes
  public int getName() {
    //return string resource ID for the tab title used when StepperLayout is in tabs mode
    return R.string.stepper_load_data;
  }

  @Override
  public VerificationError verifyStep() {
    if (error != null) {
      Toast.makeText( getContext(), error.getErrorMessage(), Toast.LENGTH_SHORT ).show();
    }

    if ( !IS_CONNECTED.get() ){
      error = null;
      Toast.makeText( getContext(), "Режим работы: оффлайн", Toast.LENGTH_SHORT ).show();
    }

    if ( settings.getString("is_first_run").get() != null ){
      error = null;
    } else {
      error = new VerificationError("Дождитесь окончания загрузки");

      if ( mRingProgressBar.getProgress() >= 99 ){
        error = null;
      }
    }

    return error;
  }

  @Override
  public void onSelected() {
    loaded = 0;
    mRingProgressBar.setProgress( 0 );
  }

  @Override
  public void onError(@NonNull VerificationError error) {
    //handle error inside of the fragment, e.g. show error on EditText
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(StepperLoginCheckFailEvent event) throws Exception {
    Timber.tag(TAG).d("login fail");

    if (event.error != null) {
      Toast.makeText( getContext(), event.error, Toast.LENGTH_SHORT ).show();
    }
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(StepperLoadDocumentEvent event) throws Exception {
    loaded++;
    Timber.tag(TAG).d("TOTAL: %s/%s | %s", COUNT.get(),loaded, event.message );

    int perc = getLoadedDocumentsPercent();

    if (mRingProgressBar != null) {
      if ( mRingProgressBar.getProgress() < perc ){
        mRingProgressBar.setProgress( perc );
      }
    }

    if ( perc == 100f ){
      error = null;
    }
  }

  private int getLoadedDocumentsPercent() {
    if ( COUNT.get() == null ){
      COUNT.set(1);
    }

    float result = 100f * loaded / COUNT.get();
    if (result > 100 ){
      result = 100f;
    }

    return (int) Math.ceil(result);
  }

  private void loadRxSettings() {
    COUNT = settings.getInteger("documents.count");
    IS_CONNECTED = settings.getBoolean("isConnectedToInternet");
  }


}