package sapotero.rxtest.views.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.widget.Toast;
import java.util.regex.Pattern;
import javax.inject.Inject;
import rx.subscriptions.CompositeSubscription;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.utils.ISettings;
import sapotero.rxtest.utils.Settings;
import timber.log.Timber;

public class SettingsViewFragment extends PreferenceFragmentCompat {
  private CompositeSubscription subscriptions;

  @Inject Context context;
  @Inject ISettings settings;

  Preference updateTimePreference;
  Preference maxImageSizePreference;
  Preference zoomTextSizePreference;
  Preference hostPreference;

  @Override
  public void onCreatePreferences(Bundle bundle, String s) {
    addPreferencesFromResource(R.xml.settings_view);
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    EsdApplication.getDataComponent().inject(this);

    Timber.tag("SETTINGS").e("settings_view_journals %s", settings.getJournals() );

    updateTimePreference = findPreference(context.getResources().getString(R.string.update_time_key));
    maxImageSizePreference = findPreference(context.getResources().getString(R.string.max_image_size_key));
    zoomTextSizePreference = findPreference(context.getResources().getString(R.string.zoomTextSize_key));
    hostPreference = findPreference(context.getResources().getString(R.string.host_key));


    subscriptions = new CompositeSubscription();

    subscriptions.add(
      settings.getShowUrgencyPreference().asObservable().subscribe(active -> {
        if(!active){settings.setOnlyUrgent(active);}
//        findPreference( context.getResources().getString(R.string.only_urgent_key) ).setEnabled(active);
//        settings.setOnlyUrgent(active);
      },Timber::e)
    );

//    *** НЕ АКТУАЛЬНО ***
//    findPreference( context.getResources().getString(R.string.show_comment_post_key) )
//            .setDependency( context.getResources().getString(R.string.actions_confirm_key) );
//    // resolved https://tasks.n-core.ru/browse/MVDESD-13341
//    // При отклонении проекта не отображается окно ввода комментария
//    subscriptions.add(
//      settings.getActionsConfirmPreference()
//        .asObservable()
//        .subscribe(
//          active -> {
//            if (active){
//              settings.setShowCommentPost(true);
//              findPreference( context.getResources().getString(R.string.show_comment_post_key) ).setEnabled(true);
//            } else {
//              settings.setShowCommentPost(false);
//              findPreference( context.getResources().getString(R.string.show_comment_post_key) ).setEnabled(false);
//            }
//          },
//          Timber::e
//        )
//    );
  }

  @Override
  public void onResume() {
    super.onResume();
    Timber.tag("SETTINGS").e("onResume()" );
    subscriptions = new CompositeSubscription();

    // Enable First run flag preference only if not first run and not substitute mode
    boolean enable = !settings.isFirstRun() && !settings.isSubstituteMode();
    Preference firstFlagPreference = findPreference(Settings.FIRST_RUN_KEY);
    if (firstFlagPreference != null) {
      firstFlagPreference.setEnabled( enable );
    }

    updateTimePreference.setOnPreferenceChangeListener((preference, newValue) -> {
      String inputString = String.valueOf(newValue);
      return isIntegerType(preference, inputString);
    });

    maxImageSizePreference.setOnPreferenceChangeListener((preference, newValue) -> {
      String inputString = String.valueOf(newValue);
      return isIntegerType(preference, inputString);
    });

    zoomTextSizePreference.setOnPreferenceChangeListener((preference, newValue) -> {
     String inputString = String.valueOf(newValue);
      return isIntegerType(preference, inputString);
    });

    hostPreference.setOnPreferenceChangeListener((preference, newValue) -> {
      String inputString = String.valueOf(newValue);
      String regexp = "^(https?)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]/";
      Pattern pattern = Pattern.compile(regexp);

      boolean matches = pattern.matcher(inputString).matches();
      if(matches){
        return true;
      }else {
        Toast.makeText(context, "Некорректный URL!",Toast.LENGTH_LONG ).show();
        return false;
      }
    });
  }

  private boolean isIntegerType(Preference preference, String inputString){
    String regexp = "\\d+";
    Pattern pattern = Pattern.compile(regexp);
    boolean isValidInput = pattern.matcher(inputString).matches();
    if(isValidInput){
      return true;
    } else {
      Toast.makeText(context, preference.toString() + "\nдолжны содержать только цифры !",Toast.LENGTH_LONG ).show();
      return false;
    }
  }

}
