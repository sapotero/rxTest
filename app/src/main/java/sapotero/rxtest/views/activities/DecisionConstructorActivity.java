package sapotero.rxtest.views.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;

import com.afollestad.materialdialogs.MaterialDialog;
import com.f2prateek.rx.preferences.RxSharedPreferences;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.retrofit.models.Oshs;
import sapotero.rxtest.retrofit.models.document.Decision;
import sapotero.rxtest.views.adapters.OshsAutoCompleteAdapter;
import sapotero.rxtest.views.adapters.models.FontItem;
import sapotero.rxtest.views.adapters.models.UrgencyItem;
import sapotero.rxtest.views.fragments.DecisionFragment;
import sapotero.rxtest.views.fragments.DecisionPreviewFragment;
import sapotero.rxtest.views.managers.menu.OperationManager;
import sapotero.rxtest.views.managers.menu.factories.CommandFactory;
import sapotero.rxtest.views.managers.menu.utils.CommandParams;
import sapotero.rxtest.views.managers.view.DecisionManager;
import sapotero.rxtest.views.views.DelayAutoCompleteTextView;
import sapotero.rxtest.views.views.SpinnerWithLabel;
import timber.log.Timber;

public class DecisionConstructorActivity extends AppCompatActivity implements DecisionFragment.OnFragmentInteractionListener, DecisionPreviewFragment.OnFragmentInteractionListener, OperationManager.Callback {

  @Inject RxSharedPreferences settings;
  @Inject OperationManager operationManager;

  @BindView(R.id.toolbar) Toolbar toolbar;

  @BindView(R.id.fragment_decision_autocomplete_field) DelayAutoCompleteTextView user_autocomplete;
  @BindView(R.id.fragment_decision_autocomplete_field_loading_indicator) ProgressBar indicator;

  @BindView(R.id.urgency_selector) SpinnerWithLabel<UrgencyItem> urgency_selector;
  @BindView(R.id.head_font_selector) SpinnerWithLabel<FontItem> font_selector;


  private String TAG = this.getClass().getSimpleName();
  private DecisionManager manager;

  @Override
  protected void onCreate(Bundle savedInstanceState) {

    setTheme(R.style.AppTheme);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_decision_constructor);

    ButterKnife.bind(this);
    EsdApplication.getComponent(this).inject(this);

    operationManager.registerCallBack(this);


    toolbar.setTitleTextColor( getResources().getColor( R.color.md_grey_100 ) );
    toolbar.setSubtitleTextColor( getResources().getColor( R.color.md_grey_400 ) );

    toolbar.setContentInsetStartWithNavigation(250);

    toolbar.setTitle("Редактор резолюции ");
    toolbar.inflateMenu(R.menu.info_decision_constructor);
    toolbar.setNavigationOnClickListener( v -> {

      if ( manager.isChanged() ){

        new MaterialDialog.Builder(this)
          .title("Имеются несохранненые данные")
          .content("Резолюция была изменена")
          .positiveText("сохранить")
          .onPositive(
            (dialog, which) -> {
              Decision new_decision = manager.getDecision();

              Timber.tag(TAG).w("positive %s", new_decision.getId() );

              String json = new Gson().toJson(new_decision);

              CommandParams params = new CommandParams();
              params.setSign( new_decision.getId() );
              params.setDecision(json);
              operationManager.execute( CommandFactory.Operation.SAVE_DECISION, params );

            }
          )
          .neutralText("выход")
          .onNeutral(
            (dialog, which) -> {
              Timber.tag(TAG).w("nothing");
              finish();
            }
          )
          .negativeText("возврат")
          .onNegative(
            (dialog, which) -> {
              Timber.tag(TAG).w("negative");
            }
          )
          .show();


      } else {
        finish();
      }

    } );
    toolbar.setOnMenuItemClickListener(item -> {

      switch (item.getItemId()){
        case R.id.action_constructor_add_block:
          manager.getDecisionBuilder().addBlock();
          break;
        case R.id.action_constructor_save:
          Timber.e("CHANGED: %s", manager.isChanged() );

          manager.saveDecision();


          break;
        case R.id.action_constructor_close:

          if ( manager.isChanged() ){
//            new RejectDecisionFragment().show( getFragmentManager(), "SaveDialog");

            new MaterialDialog.Builder(this)
              .title("Имеются несохранненые данные")
              .content("Резолюция была изменена")
              .positiveText("сохранить")
              .onPositive(
                (dialog, which) -> {
                  Decision new_decision = manager.getDecision();

                  Timber.tag(TAG).w("positive %s", new_decision.getId() );
                }
              )
              .neutralText("выход")
              .onNeutral(
                (dialog, which) -> {
                  Timber.tag(TAG).w("nothing");
                  finish();
                }
              )
              .negativeText("возврат")
              .onNegative(
                (dialog, which) -> {
                  Timber.tag(TAG).w("negative");
                }
              )
              .show();


          } else {
            finish();
          }

        break;
        default:
          break;
      }

      return false;
    });

    List<UrgencyItem> urgency = new ArrayList<>();


    // настройка
    if (!settings.getBoolean("settings_view_show_urgency").get()){
      urgency_selector.setVisibility(View.GONE);
    }
    // настройка
    if (settings.getBoolean("settings_view_only_urgent").get()){
      urgency.add(new UrgencyItem("Нет", ""));
      urgency.add(new UrgencyItem("Срочно", "Срочно"));
      urgency_selector.setVisibility(View.VISIBLE);
    } else {
      urgency.add(new UrgencyItem("Весьма срочно", "Весьма срочно"));
      urgency.add(new UrgencyItem("Крайне срочно", "Крайне срочно"));
      urgency.add(new UrgencyItem("Няшная срочность", "Няшная срочность"));
      urgency.add(new UrgencyItem("Очень срочно", "Очень срочно"));
      urgency.add(new UrgencyItem("Срочно", "Срочно"));
    }



    urgency_selector.setItems(urgency);
    urgency_selector.setOnItemSelectedListener((item, selectedIndex) -> {
//      manager.setUrgency( item.getLabel() );
    });



    // настройка
    if (settings.getBoolean("settings_view_show_decision_change_font").get()){
      List<FontItem> fonts = new ArrayList<>();
      fonts.add(new FontItem("12", "12"));
      fonts.add(new FontItem("13", "13"));
      fonts.add(new FontItem("14", "14"));
      fonts.add(new FontItem("15", "15"));
      fonts.add(new FontItem("16", "16"));

      font_selector.setItems(fonts);
      font_selector.setOnItemSelectedListener((item, selectedIndex) -> {
        Timber.e("%s - %s", item.getLabel(), item.getValue());
      });
    } else {
      font_selector.setVisibility(View.GONE);
    }



    Decision raw_decision = null;
    Gson gson = new Gson();

    Intent intent = getIntent();

    if (null != intent) {
      String data = intent.getStringExtra("decision");
      raw_decision = gson.fromJson(data, Decision.class);

      Timber.tag(TAG).v( "getIntent ++" + raw_decision);
      if (raw_decision == null) {
        raw_decision = new Decision();
        raw_decision.setLetterhead("TEST");
        raw_decision.setShowPosition(true);
        raw_decision.setSignerPositionS("--");
        raw_decision.setSignerBlankText("---");
        raw_decision.setUrgencyText("URGENCY");
        raw_decision.setId("---");
        raw_decision.setDate("---");
        raw_decision.setBlocks(new ArrayList<>());
        Timber.tag(TAG).v( "raw_decision" + gson.toJson( raw_decision, Decision.class ) );
      }
    }

    manager = new DecisionManager(this, getSupportFragmentManager(), raw_decision);
    manager.build();

    user_autocomplete.setThreshold(2);
    user_autocomplete.setAdapter( new OshsAutoCompleteAdapter(this) );
    user_autocomplete.setLoadingIndicator( indicator );
    user_autocomplete.setOnItemClickListener(
      (adapterView, view1, position, id) -> {
        Oshs user = (Oshs) adapterView.getItemAtPosition(position);
        user_autocomplete.setText( String.format("%s - %s", user.getName(), user.getOrganization() ) );
//        manager.setSigner( user );
      }
    );




  }


  @Override
  public void onFragmentInteraction(Uri uri) {
  }

  @Override
  public void onStart() {
    super.onStart();
  }
  @Override protected void onPause() {
    super.onPause();
  }

  @Override
  public void onStop() {
    super.onStop();
  }


  @Override
  public void onExecuteSuccess(String command) {

  }

  @Override
  public void onExecuteError() {

  }
}
