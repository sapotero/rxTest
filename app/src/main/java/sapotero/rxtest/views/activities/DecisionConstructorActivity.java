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
import io.requery.Persistable;
import io.requery.rx.SingleEntityStore;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.models.decisions.RBlock;
import sapotero.rxtest.db.requery.models.decisions.RBlockEntity;
import sapotero.rxtest.db.requery.models.decisions.RDecisionEntity;
import sapotero.rxtest.db.requery.models.decisions.RPerformer;
import sapotero.rxtest.db.requery.models.decisions.RPerformerEntity;
import sapotero.rxtest.retrofit.models.Oshs;
import sapotero.rxtest.retrofit.models.document.Block;
import sapotero.rxtest.retrofit.models.document.Decision;
import sapotero.rxtest.retrofit.models.document.Performer;
import sapotero.rxtest.views.adapters.OshsAutoCompleteAdapter;
import sapotero.rxtest.views.adapters.models.FontItem;
import sapotero.rxtest.views.adapters.models.UrgencyItem;
import sapotero.rxtest.views.custom.DelayAutoCompleteTextView;
import sapotero.rxtest.views.custom.SpinnerWithLabel;
import sapotero.rxtest.views.fragments.DecisionFragment;
import sapotero.rxtest.views.fragments.DecisionPreviewFragment;
import sapotero.rxtest.views.managers.menu.OperationManager;
import sapotero.rxtest.views.managers.menu.factories.CommandFactory;
import sapotero.rxtest.views.managers.menu.utils.CommandParams;
import sapotero.rxtest.views.managers.view.DecisionManager;
import timber.log.Timber;

public class DecisionConstructorActivity extends AppCompatActivity implements DecisionFragment.OnFragmentInteractionListener, DecisionPreviewFragment.OnFragmentInteractionListener, OperationManager.Callback {

  @Inject RxSharedPreferences settings;
  @Inject OperationManager operationManager;
  @Inject SingleEntityStore<Persistable> dataStore;

  @BindView(R.id.toolbar) Toolbar toolbar;

  @BindView(R.id.fragment_decision_autocomplete_field) DelayAutoCompleteTextView user_autocomplete;
  @BindView(R.id.fragment_decision_autocomplete_field_loading_indicator) ProgressBar indicator;

  @BindView(R.id.urgency_selector) SpinnerWithLabel<UrgencyItem> urgency_selector;
  @BindView(R.id.head_font_selector) SpinnerWithLabel<FontItem> font_selector;


  private String TAG = this.getClass().getSimpleName();
  private DecisionManager manager;
  private Decision raw_decision;

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

//              CommandParams params = new CommandParams();
//              params.setSign( new_decision.getId() );
//              params.setDecision(json);
//              operationManager.execute( CommandFactory.Operation.SAVE_DECISION, params );

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

        case R.id.action_constructor_next:
          // настройка
          // Показывать подтверждения о действиях с документом
          if ( settings.getBoolean("settings_view_show_actions_confirm").get() ){
            showNextDialog();
          } else {
            // operation = CommandFactory.Operation.APPROVAL_PREV_PERSON;
          }

          break;
        case R.id.action_constructor_prev:
          Decision reject = new Decision();
          reject.setApproved(false);
          reject.setCanceled(true);
          reject.setBlocks(null);

          Timber.tag("DECISION").e("reject: %s", new Gson().toJson(reject) );

          // настройка
          // Показывать подтверждения о действиях с документом
          if ( settings.getBoolean("settings_view_show_actions_confirm").get() ){
            showPrevDialog();
          } else {
            // operation = CommandFactory.Operation.APPROVAL_PREV_PERSON;
          }

          break;



//        case R.id.action_constructor_save:
//          Timber.e("CHANGED: %s", manager.isChanged() );
//
//          manager.update();
//          break;
//        case R.id.action_constructor_close:
//
//          if ( manager.isChanged() ){
////            new RejectDecisionFragment().show( getFragmentManager(), "SaveDialog");
//
//            new MaterialDialog.Builder(this)
//              .title("Имеются несохранненые данные")
//              .content("Резолюция была изменена")
//              .positiveText("сохранить")
//              .onPositive(
//                (dialog, which) -> {
//                  Decision new_decision = manager.getDecision();
//
//                  Timber.tag(TAG).w("positive %s", new_decision.getId() );
//                }
//              )
//              .neutralText("выход")
//              .onNeutral(
//                (dialog, which) -> {
//                  Timber.tag(TAG).w("nothing");
//                  finish();
//                }
//              )
//              .negativeText("возврат")
//              .onNegative(
//                (dialog, which) -> {
//                  Timber.tag(TAG).w("negative");
//                }
//              )
//              .show();
//
//
//          } else {
//            finish();
//          }
//
//        break;
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



    raw_decision = null;
    Gson gson = new Gson();

    Intent intent = getIntent();

    if (null != intent) {
      String data = intent.getStringExtra("decision");
      raw_decision = gson.fromJson(data, Decision.class);

      Timber.tag(TAG).v( "getIntent ++" + raw_decision);
      if (raw_decision == null) {

        loadDecision();

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

  private void loadDecision() {
    String decision_id = settings.getString("decision.active.id").get();

    RDecisionEntity d = dataStore
      .select(RDecisionEntity.class)
      .where(RDocumentEntity.UID.eq(decision_id))
      .get().firstOrNull();

    if (d != null) {
      raw_decision = new Decision();

      raw_decision.setId( d.getUid() );
      raw_decision.setLetterhead(d.getLetterhead());
      raw_decision.setApproved(d.isApproved());
      raw_decision.setSigner(d.getSigner());
      raw_decision.setSignerId(d.getSignerId());
      raw_decision.setAssistantId(d.getAssistantId());
      raw_decision.setSignerBlankText(d.getSignerBlankText());
      raw_decision.setSignerIsManager(d.isSignerIsManager());
      raw_decision.setComment(d.getComment());
      raw_decision.setDate(d.getDate());
      raw_decision.setUrgencyText(d.getUrgencyText());
      raw_decision.setShowPosition(d.isShowPosition());

      if ( d.getBlocks() != null && d.getBlocks().size() >= 1 ){

        ArrayList<Block> list = new ArrayList<>();

        for (RBlock _block: d.getBlocks() ) {

          RBlockEntity b = (RBlockEntity) _block;
          Block block = new Block();
          block.setNumber(b.getNumber());
          block.setText(b.getText());
          block.setAppealText(b.getAppealText());
          block.setTextBefore(b.isTextBefore());
          block.setHidePerformers(b.isHidePerformers());
          block.setToCopy(b.isToCopy());
          block.setToFamiliarization(b.isToFamiliarization());

          if ( b.getPerformers() != null && b.getPerformers().size() >= 1 ) {

            for (RPerformer _performer : b.getPerformers()) {

              RPerformerEntity p = (RPerformerEntity) _performer;
              Performer performer = new Performer();

              performer.setNumber(p.getNumber());
              performer.setPerformerId(p.getPerformerId());
              performer.setPerformerType(p.getPerformerType());
              performer.setPerformerText(p.getPerformerText());
              performer.setOrganizationText(p.getOrganizationText());
              performer.setIsOriginal(p.isIsOriginal());
              performer.setIsResponsible(p.isIsResponsible());

              block.getPerformers().add(performer);
            }
          }


          list.add(block);
        }
        raw_decision.setBlocks(list);
      }
    } else {
      raw_decision = new Decision();
      raw_decision.setLetterhead("Бланк резолюции");
      raw_decision.setShowPosition(true);
      raw_decision.setSignerPositionS("");
      raw_decision.setSignerBlankText("");
      raw_decision.setUrgencyText("");
      raw_decision.setId("");
      raw_decision.setDate("");
      raw_decision.setBlocks(new ArrayList<>());    }

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


  private void showPrevDialog() {

    MaterialDialog.Builder prev_dialog = new MaterialDialog.Builder(this)
      .content(R.string.decision_reject_body)
      .cancelable(true)
      .positiveText(R.string.yes)
      .negativeText(R.string.no)
      .onPositive((dialog1, which) -> {

//        CommandFactory.Operation operation;
//        operation = isApproval ? CommandFactory.Operation.APPROVAL_PREV_PERSON : CommandFactory.Operation.SIGNING_PREV_PERSON;
//
//        CommandParams params = new CommandParams();
//        params.setUser(LOGIN.get());
//        params.setSign("Sign");

//        operationManager.execute(operation, params);
      })
      .autoDismiss(true);

    prev_dialog.build().show();
  }

  private void showNextDialog() {

    MaterialDialog.Builder prev_dialog = new MaterialDialog.Builder(this)
      .content(R.string.decision_approve_body)
      .cancelable(true)
      .positiveText(R.string.yes)
      .negativeText(R.string.no)
      .onPositive((dialog1, which) -> {

        Decision approve = new Decision();
        approve.setApproved(true);
        approve.setBlocks(null);

        Timber.tag("DECISION").e("approve: %s | %s", new Gson().toJson(approve), settings.getString("decision.active.id").get() );

        CommandFactory.Operation operation;
        operation =CommandFactory.Operation.APPROVE_DECISION;

        CommandParams params = new CommandParams();
        params.setDecisionId( settings.getString("decision.active.id").get() );
        params.setDecision( approve );

        operationManager.execute(operation, params);
      })
      .autoDismiss(true);

    prev_dialog.build().show();
  }

}
