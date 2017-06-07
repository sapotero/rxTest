package sapotero.rxtest.views.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Base64;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.requery.Persistable;
import io.requery.rx.SingleEntityStore;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.mapper.utils.Mappers;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.models.actions.RAction;
import sapotero.rxtest.db.requery.models.actions.RActionEntity;
import sapotero.rxtest.db.requery.models.decisions.RBlock;
import sapotero.rxtest.db.requery.models.decisions.RBlockEntity;
import sapotero.rxtest.db.requery.models.decisions.RDecision;
import sapotero.rxtest.db.requery.models.decisions.RDecisionEntity;
import sapotero.rxtest.db.requery.models.decisions.RPerformer;
import sapotero.rxtest.db.requery.models.decisions.RPerformerEntity;
import sapotero.rxtest.events.decision.ApproveDecisionEvent;
import sapotero.rxtest.events.decision.HasNoActiveDecisionConstructor;
import sapotero.rxtest.events.decision.RejectDecisionEvent;
import sapotero.rxtest.events.view.InvalidateDecisionSpinnerEvent;
import sapotero.rxtest.events.view.ShowNextDocumentEvent;
import sapotero.rxtest.events.view.UpdateCurrentDocumentEvent;
import sapotero.rxtest.managers.menu.OperationManager;
import sapotero.rxtest.managers.menu.factories.CommandFactory;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import sapotero.rxtest.managers.toolbar.ToolbarManager;
import sapotero.rxtest.retrofit.models.document.Decision;
import sapotero.rxtest.utils.Settings;
import sapotero.rxtest.utils.padeg.Declension;
import sapotero.rxtest.utils.queue.QueueManager;
import sapotero.rxtest.views.activities.DecisionConstructorActivity;
import sapotero.rxtest.views.adapters.DecisionSpinnerAdapter;
import sapotero.rxtest.views.adapters.models.DecisionSpinnerItem;
import sapotero.rxtest.views.dialogs.DecisionMagniferFragment;
import sapotero.rxtest.views.dialogs.SelectTemplateDialogFragment;
import timber.log.Timber;


@SuppressLint("ValidFragment")
public class InfoActivityDecisionPreviewFragment extends Fragment implements SelectTemplateDialogFragment.Callback{

  @Inject Settings settings;
  @Inject Mappers mappers;
  @Inject SingleEntityStore<Persistable> dataStore;
  @Inject OperationManager operationManager;
  @Inject QueueManager queue;

  private ToolbarManager toolbarManager;
  private OnFragmentInteractionListener mListener;

  @BindView(R.id.activity_info_decision_preview_head) LinearLayout preview_head;
  @BindView(R.id.activity_info_decision_preview_acition_wrapper) LinearLayout action_wrapper;

  @BindView(R.id.activity_info_decision_preview_body) LinearLayout preview_body;
  @BindView(R.id.activity_info_decision_preview_bottom) LinearLayout preview_bottom;
  @BindView(R.id.desigion_view_root) LinearLayout desigion_view_root;

  @BindView(R.id.activity_info_button_magnifer) ImageButton magnifer_button;
  @BindView(R.id.activity_info_decision_preview_comment) ImageButton comment_button;
  @BindView(R.id.activity_info_button_edit) ImageButton edit;

  @BindView(R.id.activity_info_decision_spinner) Spinner decision_spinner;
  @BindView(R.id.activity_info_decision_preview_toolbar) Toolbar decision_toolbar;
  @BindView(R.id.activity_info_decision_preview_magnifer) ImageButton magnifer;

  @BindView(R.id.activity_info_decision_preview_next_person) Button next_person_button;
  @BindView(R.id.activity_info_decision_preview_prev_person) Button prev_person_button;
  @BindView(R.id.activity_info_decision_preview_approved_text) TextView approved_text;
  @BindView(R.id.activity_info_decision_preview_acition_text)  TextView action_text;
  @BindView(R.id.activity_info_decision_preview_temporary) TextView temporary;
  @BindView(R.id.activity_info_decision_preview_count) TextView decision_count;


  ;

  private DecisionSpinnerAdapter decision_spinner_adapter;
  private Preview preview;

  private Unbinder binder;
  private String uid;
  private RDecisionEntity current_decision;
  private String TAG = this.getClass().getSimpleName();
  private GestureDetector gestureDetector;
  private RDocumentEntity doc;
  private InfoActivityDecisionPreviewFragment fragment;
  private SelectTemplateDialogFragment templates;
  private MaterialDialog.Builder prev_dialog;

  public InfoActivityDecisionPreviewFragment() {
  }


  public InfoActivityDecisionPreviewFragment(ToolbarManager toolbarManager) {
    this.toolbarManager = toolbarManager;
  }

  @OnClick(R.id.activity_info_decision_preview_magnifer)
  public void expandView(){
    magnifer();
  }


  // Approve current decision
  @OnClick(R.id.activity_info_decision_preview_next_person)
  public void decision_preview_next(){
    Timber.tag(TAG).v("decision_preview_next star");

    if ( settings.isActionsConfirm() ){
      // resolved https://tasks.n-core.ru/browse/MVDESD-12765
      // выводить подтверждение при подписании резолюции

        MaterialDialog.Builder prev_dialog = new MaterialDialog.Builder( getContext() )
          .content(R.string.decision_approve_body)
          .cancelable(true)
          .positiveText(R.string.yes)
          .negativeText(R.string.no)
          .onPositive((dialog1, which) -> {

            CommandFactory.Operation operation = CommandFactory.Operation.APPROVE_DECISION;

            CommandParams params = new CommandParams();

            params.setDecisionId( current_decision.getUid() );
            params.setDecisionModel( mappers.getDecisionMapper().toFormattedModel(current_decision) );
            params.setDocument( settings.getUid() );
            params.setActiveDecision( decision_spinner_adapter.hasActiveDecision() );

            operationManager.execute(operation, params);
            updateAfteButtonPressed();
            EventBus.getDefault().post( new ShowNextDocumentEvent());
          })
          .autoDismiss(true);

        prev_dialog.build().show();

    } else {
      CommandFactory.Operation operation;
      operation =CommandFactory.Operation.APPROVE_DECISION;

      CommandParams params = new CommandParams();

      params.setDecisionId( current_decision.getUid() );
//      params.setDecision( current_decision );
      params.setDecisionModel( mappers.getDecisionMapper().toFormattedModel(current_decision) );
      params.setDocument( settings.getUid() );
      params.setActiveDecision( decision_spinner_adapter.hasActiveDecision() );

      operationManager.execute(operation, params);
      updateAfteButtonPressed();
      EventBus.getDefault().post( new ShowNextDocumentEvent());
    }

    Timber.tag(TAG).v("decision_preview_next end");
  }

  // Reject current decision
  @OnClick(R.id.activity_info_decision_preview_prev_person)
  public void decision_preview_prev(){
    Timber.tag(TAG).v("decision_preview_prev");

    // resolved https://tasks.n-core.ru/browse/MVDESD-12765
    // Добавить ввод комментариев на "Отклонить резолюцию" и "без ответа"

    if ( settings.isShowCommentPost() ){

      showPrevDialog(null);

    } else {

      CommandFactory.Operation operation;
      operation =CommandFactory.Operation.REJECT_DECISION;

      CommandParams params = new CommandParams();
      params.setDecisionId( current_decision.getUid() );
      params.setDocument( settings.getUid() );
      params.setDecisionModel( mappers.getDecisionMapper().toFormattedModel(current_decision) );
      params.setActiveDecision( decision_spinner_adapter.hasActiveDecision() );

      operationManager.execute(operation, params);
      updateAfteButtonPressed();
      EventBus.getDefault().post( new ShowNextDocumentEvent());
    }
  }

  private void showPrevDialog(String text) {
    prev_dialog = new MaterialDialog.Builder( getContext() )
      .content(R.string.decision_reject_body)
      .cancelable(true)
      .positiveText(R.string.yes)
      .negativeText(R.string.no)
      .onPositive((dialog1, which) -> {

        CommandFactory.Operation operation =CommandFactory.Operation.REJECT_DECISION;

        CommandParams commandParams = new CommandParams();
        commandParams.setDecisionId( current_decision.getUid() );
        commandParams.setDocument( settings.getUid() );
        commandParams.setDecisionModel( mappers.getDecisionMapper().toFormattedModel(current_decision) );
        commandParams.setActiveDecision( decision_spinner_adapter.hasActiveDecision() );

        if ( settings.isShowCommentPost() ) {
          commandParams.setComment(dialog1.getInputEditText().getText().toString());
        }

        operationManager.execute(operation, commandParams);
        updateAfteButtonPressed();
        EventBus.getDefault().post( new ShowNextDocumentEvent());
      })
      .autoDismiss(true);

    // настройка
    // Показывать комментарий при отклонении
    if ( settings.isShowCommentPost() ){
      prev_dialog
        .inputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES )
        .input(R.string.comment_hint, R.string.dialog_empty_value, (dialog12, input) -> {})
        .neutralText("Шаблон")
        .onNeutral(new MaterialDialog.SingleButtonCallback() {
          @Override
          public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

            templates = new SelectTemplateDialogFragment();
            templates.setType("rejection");
            templates.registerCallBack( fragment );

            templates.show( getActivity().getFragmentManager(), "SelectTemplateDialogFragment");
          }
        });
    }



    if ( text != null ){
      MaterialDialog build = prev_dialog.build();
      build.getInputEditText().setText(text);
      build.show();
    } else {
      MaterialDialog build = prev_dialog.build();
      build.show();
    }

    setAsFakeProcessed();
  }

  private void setAsFakeProcessed() {
    // resolved https://tasks.n-core.ru/browse/MVDESD-13366
    // ставим плашку всегда
    dataStore
      .update(RDocumentEntity.class)
      .set(RDocumentEntity.CHANGED, true)
      .set(RDocumentEntity.MD5, "")
      .where(RDocumentEntity.UID.eq( settings.getUid() ))
      .get()
      .value();
  }

  private void updateAfteButtonPressed() {
    try {
      decision_spinner_adapter.setCurrentAsTemporary( decision_spinner.getSelectedItemPosition() );
      current_decision = decision_spinner_adapter.getItem(decision_spinner.getSelectedItemPosition()).getDecision();
      displayDecision();
    } catch (Exception e) {
      e.printStackTrace();
    }

    setAsFakeProcessed();
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

  }

  @Override
  public void onResume() {
    super.onResume();
    invalidate();
  }

  @Override
  public void onSelectTemplate(String template) {

    showPrevDialog(template);

    templates.dismiss();
  }

  public class GestureListener extends
    GestureDetector.SimpleOnGestureListener {

    @Override
    public boolean onDown(MotionEvent e) {
      return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
      Timber.tag("GestureListener").w("DOUBLE TAP");


      if ( doc != null && Objects.equals(doc.getAddressedToType(), "")){

        if ( doc.isFromLinks() != null && !doc.isFromLinks() && current_decision != null ){

          if ( !queue.getConnected() &&
            current_decision.isTemporary() != null &&
            current_decision.isTemporary() && !doc.isProcessed() ){

            edit();
          }

          if ( queue.getConnected() &&
            current_decision.isTemporary() != null &&
            current_decision.isTemporary() ){
            Toast.makeText( getContext(), "Запрещено редактировать резолюции в онлайне!\nДождитесь синхронизации.", Toast.LENGTH_SHORT ).show();
          }

          if ( current_decision.isApproved() != null &&
            !current_decision.isApproved() &&
            current_decision.isTemporary() != null &&
            !current_decision.isTemporary() && !doc.isProcessed()){
            edit();
          }
        }

        if ( doc.isFromLinks() != null && !doc.isFromLinks() &&
          current_decision == null ){

          settings.setDecisionActiveId(0);
          Context context = getContext();
          Intent create_intent = new Intent(context, DecisionConstructorActivity.class);
          context.startActivity(create_intent);

        }
      }

      return true;
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_info_card_decision_preview, container, false);

    EsdApplication.getManagerComponent().inject(this);
    binder = ButterKnife.bind(this, view);

    fragment = this;

    return view;
  }

  private void invalidate() {
    initEvents();

    initToolBar();

    setAdapter();
    loadDocument();

    gestureDetector = new GestureDetector( getContext(),new GestureListener() );

    desigion_view_root.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));
    preview_body.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));

    preview = new Preview(getContext());
  }

  private void initToolBar() {

    if (decision_toolbar != null) {
      decision_toolbar.inflateMenu(R.menu.fragment_decision_preview_menu);
      decision_toolbar.setOnMenuItemClickListener(item -> {
        switch ( item.getItemId() ){
          case R.id.decision_preview_magnifer:
            magnifer();
            break;
          case R.id.decision_preview_edit:
            edit();
            break;
          default:
            break;
        }
        return false;
      });
    }
  }

  private void setAdapter() {

    ArrayList<DecisionSpinnerItem> decisionSpinnerItems = new ArrayList<>();
    decision_spinner_adapter = new DecisionSpinnerAdapter(getContext(), settings.getCurrentUserId(), decisionSpinnerItems);
    decision_spinner.setAdapter(decision_spinner_adapter);

    decision_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
        if ( decision_spinner_adapter.getCount() > 0 ) {
          Timber.tag(TAG).e("onItemSelected %s %s ", position, id);
          current_decision = decision_spinner_adapter.getItem(position).getDecision();
          displayDecision();
        }
      }

      @Override
      public void onNothingSelected(AdapterView<?> adapterView) {
        if ( decision_spinner_adapter.getCount() > 0 ){
          current_decision = decision_spinner_adapter.getItem(0).getDecision();
          Timber.tag(TAG).e("onNothingSelected");
          displayDecision();
        }
      }
    });
  }

  private void updateVisibility(Boolean approved) {

    next_person_button.setVisibility(approved ? View.GONE : View.VISIBLE);
    prev_person_button.setVisibility(approved ? View.GONE : View.VISIBLE);

    showDecisionCardTollbarMenuItems(true);

    // FIX для ссылок
    if (current_decision == null) {
      next_person_button.setVisibility( !approved ? View.INVISIBLE : View.GONE);
      prev_person_button.setVisibility( !approved ? View.INVISIBLE : View.GONE);
    }


    if (approved){
      decision_toolbar.getMenu().findItem(R.id.decision_preview_edit).setVisible(false);
    }

    approved_text.setVisibility(!approved ? View.GONE : View.VISIBLE);

    if ( !decision_spinner_adapter.hasActiveDecision() ){
      Timber.tag(TAG).e("NO ACTIVE DECISION");
      EventBus.getDefault().post( new HasNoActiveDecisionConstructor() );
    }


    //FIX не даем выполнять операции для связанных документов
    if ( doc!=null && doc.isFromLinks()!=null && doc.isFromLinks() ){
      next_person_button.setVisibility( View.GONE );
      prev_person_button.setVisibility( View.GONE );
    }

    // resolved https://tasks.n-core.ru/browse/MVDESD-13146
    // для статуса "на первичное рассмотрение" вместо "Подписать" должно быть "Согласовать"
    // Если подписывающий в резолюции и оператор в МП совпадают, то кнопка должна быть "Подписать"
    if ( doc.getFilter() != null && doc.getFilter().equals("primary_consideration") ){
      if ( current_decision != null &&
           current_decision.getSignerId() != null &&
           current_decision.getSignerId().equals( settings.getCurrentUserId() ) ){
        next_person_button.setText( getString(R.string.menu_info_next_person));
        setSignEnabled(true);
      } else {
        next_person_button.setText( getString(R.string.menu_info_sign_next_person) );

        // resolved https://tasks.n-core.ru/browse/MVDESD-13438
        // Добавить настройку наличия кнопки Согласовать в Первичном рассмотрении
        if (!settings.isShowApproveOnPrimary()){
          setSignEnabled(false);
        } else {
          setSignEnabled(true);
        }
      }
    }


    if ( doc.isProcessed() != null && doc.isProcessed() && !current_decision.isApproved() ){
      next_person_button.setVisibility( View.INVISIBLE );
      prev_person_button.setVisibility( View.INVISIBLE );
    }

    if ( current_decision.isTemporary() != null && current_decision.isTemporary() ){
      temporary.setVisibility(View.VISIBLE);
      next_person_button.setVisibility( View.GONE );
      prev_person_button.setVisibility( View.GONE );
    } else {
      temporary.setVisibility(View.GONE);
    }

    // resolved https://tasks.n-core.ru/browse/MVDESD-13423
    //  Отображать информацию от кого поступила резолюция
    updateActionText(true);

  }

  // resolved https://tasks.n-core.ru/browse/MVDESD-13423
  // Так нужно отображать, но есть проблемы на стороне СЭДика
  // Поэтому пока не используем честный способ, а просто показываем последнее действие
  private void updateActionText(Boolean showAsFake) {


    dataStore
      .select(RActionEntity.class)
      .where(RActionEntity.DOCUMENT_ID.eq(doc.getId()))
      .orderBy(RActionEntity.UPDATED_AT.desc())
      .limit(1)
      .get()
      .toObservable()
      .subscribeOn(Schedulers.newThread())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(
        data -> {
          Timber.tag(TAG).w(" %s | %s", data.getUpdatedAt(), data.getToS() );
          setActionText( data.getToS() );
        },
        error -> {
          Timber.tag(TAG).e(error);
        }
      );


    if (doc != null && doc.getActions() != null && doc.getActions().size() > 0){
      String action_temporary_text = "";

      // refactor
      // убрать эту дичь
      if( showAsFake ){
//
//        ArrayList<RActionEntity> list = new ArrayList<>();
//
//        for ( RAction act: doc.getActions() ) {
//          RActionEntity _act = (RActionEntity) act;
//          list.add(_act);
//        }
//
//        Timber.tag(TAG).i("list: %s", list);
//
//
//          if ( list.size() == 1 ){
//            setActionText(list.startTransactionFor(0).getToS());
//          } else if ( list.size() >= 2 ){
//            Collections.sort(list, (a1, a2) -> a1.getUpdatedAt().compareTo( a2.getUpdatedAt() ));
//            setActionText( list.startTransactionFor( list.size()-1 ).getToS() );
//          }
//        } catch (Exception e) {
//          Timber.tag(TAG).e(e);
//          action_wrapper.setVisibility(View.GONE);
//        }


      } else {

        ArrayList<RActionEntity> list = new ArrayList<>();

        for ( RAction act: doc.getActions() ) {
          RActionEntity _act = (RActionEntity) act;

          if (Objects.equals(_act.getAddressedToId(), settings.getCurrentUserId())){
            list.add( _act );
          }
        }

        if ( list.size() == 1 ){
          setActionText(list.get(0).getToS());
        } else if ( list.size() >= 2 ){
          Collections.sort(list, (a1, a2) -> a1.getUpdatedAt().compareTo( a2.getUpdatedAt() ));
          setActionText( list.get( list.size()-1 ).getToS() );
        } else {
          action_wrapper.setVisibility(View.GONE);
        }

      }

    } else {
      action_wrapper.setVisibility(View.GONE);
    }


  }

  private void setActionText(String action_temporary_text) {
    String organization = "";
    String user = "";

    Timber.tag(TAG).e("action_temporary_text: %s", action_temporary_text);

    int organization_index = action_temporary_text.indexOf("(");

    if ( organization_index != -1 ){
      organization = action_temporary_text.substring( organization_index, action_temporary_text.length() );

      String pattern = "(\\w+\\s.\\.)";

      Pattern r = Pattern.compile(pattern);
      Matcher m = r.matcher( action_temporary_text.substring( 0, organization_index-1 ) );

      if (m.find()) {
        action_wrapper.setVisibility(View.VISIBLE);
        action_text.setText( String.format("%s %s", m.group(0), organization) );
      } else {
        action_wrapper.setVisibility(View.GONE);
      }
    } else {
      action_wrapper.setVisibility(View.GONE);
    }

  }

  private void setSignEnabled(boolean active) {

    if (active){
      next_person_button.setAlpha(1f);
    } else {
      next_person_button.setAlpha(0.2f);
    }

    next_person_button.setClickable(active);
    next_person_button.setFocusable(active);
    next_person_button.setEnabled(active);
  }


  private void showDecisionCardTollbarMenuItems(boolean visible) {
    try {
      decision_toolbar.getMenu().findItem(R.id.decision_preview_magnifer).setVisible(visible);
      decision_toolbar.getMenu().findItem(R.id.decision_preview_edit).setVisible(visible);

      if (!visible){
        next_person_button.setVisibility( View.GONE );
        prev_person_button.setVisibility( View.GONE );
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override public void onDestroyView() {
    super.onDestroyView();
    binder.unbind();
    initEvents();
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    if (context instanceof OnFragmentInteractionListener) {
      mListener = (OnFragmentInteractionListener) context;
    } else {
      throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
    }
  }

  @Override
  public void onDetach() {
    super.onDetach();
    mListener = null;
  }

  public Fragment withUid(String uid) {
    this.uid = uid;
    return this;
  }

  public interface OnFragmentInteractionListener {
    void onFragmentInteraction(Uri uri);
  }

  @OnClick(R.id.activity_info_button_magnifer)
  public void magnifer(){
    Timber.tag(TAG).v("magnifer");
    DecisionSpinnerItem decision;
    DecisionMagniferFragment magnifer = new DecisionMagniferFragment();

    if ( decision_spinner_adapter.size() > 0 ){
      decision = decision_spinner_adapter.getItem( decision_spinner.getSelectedItemPosition() );
      magnifer.setDecision( decision );
      magnifer.setRegNumber( settings.getRegNumber() );
    }

    magnifer.show( getFragmentManager() , "DecisionMagniferFragment");
  }

  @OnClick(R.id.activity_info_decision_preview_comment)
  public void comment(){
    Timber.tag(TAG).v("comment_button");

    MaterialDialog editDialog = new MaterialDialog.Builder(getContext())
      .title("Комментарий резолюции")
      .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE)
      .input("Комментарий", current_decision.getComment(), (dialog, input) -> {})
      .positiveText(R.string.constructor_save)
      .onPositive((dialog, which) -> {

        if ( dialog.getInputEditText().getText() != null && !Objects.equals(dialog.getInputEditText().getText().toString(), current_decision.getComment()) ) {

          CommandFactory.Operation operation = CommandFactory.Operation.SAVE_DECISION;
          CommandParams params = new CommandParams();

          Decision decision = mappers.getDecisionMapper().toFormattedModel( current_decision );
          decision.setComment( dialog.getInputEditText().getText().toString() );
          params.setDecisionModel( decision );
          params.setDecisionId( current_decision.getUid() );
          params.setDocument( settings.getUid() );

          Timber.e("DECISION %s", new Gson().toJson(decision));

          operationManager.execute( operation, params );
        }
        dialog.dismiss();
      })
      .neutralText(R.string.constructor_close)
      .onNeutral((dialog, which) -> dialog.dismiss())
      .autoDismiss(false)
      .build();

    MaterialDialog materialDialog = new MaterialDialog.Builder(getContext())
      .title("Комментарий резолюции")
      .content( current_decision.getComment() )
      .positiveText(R.string.constructor_close)
      .neutralText(R.string.decision_preview_edit)
      .onPositive((dialog, which) -> { dialog.dismiss(); })
      .onNeutral((dialog, which) -> {
        dialog.dismiss();
        editDialog.show();
      })
      .autoDismiss(false)
      .build();

    materialDialog.show();
  }


  @OnClick(R.id.activity_info_button_edit)
  public void edit(){

    Timber.tag(TAG).v("edit");
    RDecisionEntity data = decision_spinner_adapter.getItem( decision_spinner.getSelectedItemPosition() ).getDecision();

    Timber.tag(TAG).v("DECISION");
    Timber.tag(TAG).v("%s", data);


    Context context = getContext();
    Intent intent = new Intent( context , DecisionConstructorActivity.class);

    context.startActivity(intent);

  }


  private Boolean documentExist(){
    Integer count = dataStore
      .count( RDocumentEntity.class )
      .where( RDocumentEntity.UID.eq( uid == null? settings.getUid() : uid ) )
      .get()
      .value();

    return count > 0;
  }

  private void loadDocument() {
    Timber.tag(TAG).v("loadDocument | exist %s | %s", documentExist(), settings.getUid() );

    if ( documentExist() ){
      loadFromDb();
    } else {
      loadFromJson();
    }
  }

  
  private void loadFromDb() {
    Timber.tag("loadFromDb").v("star");

    dataStore
      .select(RDocumentEntity.class)
      .where(RDocumentEntity.UID.eq( uid == null? settings.getUid() : uid ))
      .get()
      .toObservable()
      .subscribeOn(Schedulers.newThread())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(doc -> {

        this.doc = doc;

//        Timber.tag("loadFromDb").i( "loaded %s", doc.getId() );

        preview.showEmpty();

        if ( doc.getDecisions().size() > 0 ){

          decision_spinner_adapter.clear();

          List<DecisionSpinnerItem> unsorterd_decisions = new ArrayList<DecisionSpinnerItem>();


          for (RDecision rDecision: doc.getDecisions()) {
            RDecisionEntity decision = (RDecisionEntity) rDecision;
            unsorterd_decisions.add( new DecisionSpinnerItem( decision ) );
          }

//          Collections.sort(unsorterd_decisions, (o1, o2) -> o1.getDecision().getUid().compareTo( o2.getDecision().getUid() ));
//          Timber.tag(TAG).e("unsorterd_decisions > %s", unsorterd_decisions.size());

          decision_spinner_adapter.addAll( unsorterd_decisions );


//           если есть резолюции, то отобразить первую
          if ( decision_spinner_adapter.size() > 0 ) {
            current_decision = decision_spinner_adapter.getItem(0).getDecision();
            Timber.tag(TAG).e("decision_spinner_adapter > 0");
            displayDecision();
          }

          if (decision_spinner_adapter.size() == 1){
            invalidateSpinner(false);
            decision_count.setVisibility(View.GONE);
          }

          if (decision_spinner_adapter.size() >= 2){
            decision_count.setText( String.format(" %s ", unsorterd_decisions.size()) );
            decision_count.setVisibility(View.VISIBLE);
            invalidateSpinner(true);
          }
        } else {
          Timber.e("no decisions");

          if (toolbarManager != null) {
            toolbarManager.setEditDecisionMenuItemVisible(false);
          }
          decision_spinner_adapter.clear();

          RDecisionEntity empty = new RDecisionEntity();
          empty.setSignerBlankText("Нет резолюций");
          decision_spinner_adapter.add( new DecisionSpinnerItem( empty ) );

          preview.showEmpty();

          magnifer_button.setVisibility(View.GONE);
          comment_button.setVisibility(View.GONE);
          decision_count.setVisibility(View.GONE);
          invalidateSpinner(false);
          showDecisionCardTollbarMenuItems(false);
          EventBus.getDefault().post( new HasNoActiveDecisionConstructor() );

          updateActionText(true);
        }

      }, error -> {
        Timber.tag(TAG).e(error);
      });
  }

  private void invalidateSpinner(boolean visibility) {
//    decision_spinner.setBackgroundColor( ContextCompat.getColor(getContext(), R.color.md_grey_50) );

    decision_spinner
      .getBackground()
      .setColorFilter(
        ContextCompat.getColor(getContext(), visibility ? R.color.md_grey_800 : R.color.md_white_1000),
        PorterDuff.Mode.SRC_ATOP);

    decision_spinner.setClickable( visibility );
    decision_spinner.setFocusable( visibility );
    decision_spinner.setEnabled( visibility );
  }

  private void displayDecision() {
    Timber.tag(TAG).v("displayDecision");

    if (current_decision != null && current_decision.getUid() != null) {

      if (current_decision.getComment() != null && !Objects.equals(current_decision.getComment(), "")){
        comment_button.setVisibility(View.VISIBLE);
      } else {
        comment_button.setVisibility(View.GONE);
      }

      preview.show( current_decision );

      settings.setDecisionActiveId( current_decision.getId() );

      if (toolbarManager != null) {
        toolbarManager.setEditDecisionMenuItemVisible( !current_decision.isApproved() );
      }
      updateVisibility( current_decision.isApproved() );



    }

//    if ( !decision_spinner_adapter.hasActiveDecision() ){
//      toolbarManager.showAsProcessed();
//    }
  }

  private void loadFromJson(){
    Timber.e("loadFromJson");
//    HOST = settings.getString("settings_username_host");
//
//    Retrofit retrofit = new Retrofit.Builder()
//      .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
//      .addConverterFactory(GsonConverterFactory.create())
//      .baseUrl(HOST.startTransactionFor() + "v3/documents/")
//      .client(okHttpClient)
//      .build();
//
//    DocumentService documentService = retrofit.create( DocumentService.class );
//
//    Observable<DocumentInfo> activity_main_menu = documentService.getInfo(
//      UID.startTransactionFor(),
//      LOGIN.startTransactionFor(),
//      TOKEN.startTransactionFor()
//    );
//
//    activity_main_menu.subscribeOn( Schedulers.newThread() )
//      .observeOn( AndroidSchedulers.mainThread() )
//      .subscribe(
//        data -> {
//          DOCUMENT = data;
//
//          Gson gson = new Gson();
//
//          Preference<String> documentNumber = settings.getString("document.number");
//          documentNumber.set( DOCUMENT.getRegistrationNumber() );
//
//          Preference<String> documentJson = settings.getString("document.json");
//          documentJson.set( gson.toJson(DOCUMENT) );
//
//          Preference<String> documentImages = settings.getString("document.images");
//          documentImages.set( gson.toJson( DOCUMENT.getImages() ) );
//
//          toolbar.setTitle( data.getTitle() );
//
//          if ( data.getDecisions().size() >= 1 ){
//
//            desigions_recycler_view.setLayoutManager(new LinearLayoutManager(this));
//
//            List<Decision> decisions_list = new ArrayList<>();
//            for (Decision decision: data.getDecisions()) {
//              decisions_list.addByOne(decision);
//            }
//
//            decision_adapter = new DecisionAdapter(decisions_list, this, desigions_recycler_view);
//            desigions_recycler_view.setAdapter(decision_adapter);
//
//            // если есть резолюции, то отобразить первую
//            if ( decisions_list.size() > 0 ) {
//              try {
//                jobManager.addJobInBackground( new SetActiveDecisionJob(0) );
//              } catch ( Exception e){
//                Timber.tag(TAG + " massInsert error").v( e );
//              }
//            }
//
//            desigions_recycler_view.setLayoutManager(new LinearLayoutManager(this));
//
//            RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
//            itemAnimator.setAddDuration(10);
//            itemAnimator.setRemoveDuration(10);
//            desigions_recycler_view.setItemAnimator(itemAnimator);
//
//          }
//
//          if ( data.getInfoCard() != null ){
//            CARD = Base64.decode( data.getInfoCard().getBytes(), Base64.DEFAULT );
//          } else {
//            CARD = Base64.decode( "".getBytes(), Base64.DEFAULT );
//          }
//          Preference<String> infocard = settings.getString("document.infoCard");
//          infocard.set( new String(CARD , StandardCharsets.UTF_8) );
//
//        },
//        error -> {
//          Log.d( "++_ERROR", error.getMessage() );
//          Toast.makeText( this, error.getMessage(), Toast.LENGTH_SHORT).show();
//        });
  }

  public class Preview{

    private final Context context;
    private String TAG = this.getClass().getSimpleName();
    private String reg_number;

    Preview(Context context) {
      this.context = context;
      clear();
    }

    private void clear(){
      try {
        preview_head.removeAllViews();
        preview_body.removeAllViews();
        preview_bottom.removeAllViews();
      } catch (Exception e) {
        e.printStackTrace();
      }
    };

    private void show( RDecisionEntity decision ){
      clear();

      showMagnifer();

      Timber.tag("getUrgencyText").v("%s", decision.getUrgencyText() );
      Timber.tag("getLetterhead").v("%s",  decision.getLetterhead() );

      if( decision.getLetterhead() != null ) {
        printLetterHead(decision.getLetterhead());
      }

      if( decision.getUrgencyText() != null ){
        printUrgency(decision.getUrgencyText());
      }

      if( decision.getBlocks().size() > 0 ){

        Boolean isOnlyOneBlock = false;

        if (decision.getBlocks().size() == 1){
          isOnlyOneBlock = true;
        }

        Set<RBlock> _blocks = decision.getBlocks();

        ArrayList<RBlockEntity> blocks = new ArrayList<>();
        for (RBlock b: _blocks){
          blocks.add( (RBlockEntity) b );
        }

        Collections.sort(blocks, (o1, o2) -> o1.getNumber().compareTo( o2.getNumber() ));


        for (RBlock b: blocks){
          RBlockEntity block = (RBlockEntity) b;

          Timber.tag("showPosition").v( "ShowPosition: %s", block.getAppealText() );

          printAppealText( block, isOnlyOneBlock );

          if ( block.isTextBefore() ){

            printBlockText( block, isOnlyOneBlock );
            if ( block.isHidePerformers() != null && !block.isHidePerformers()){
              printBlockPerformers( block, isOnlyOneBlock );
            }

          } else {
            if ( block.isHidePerformers() != null && !block.isHidePerformers())
              printBlockPerformers( block, isOnlyOneBlock );
            printBlockText( block, isOnlyOneBlock);
          }

        }
      }

      printSigner( decision, settings.getRegNumber() );
    }

    private void showEmpty(){
      Timber.tag(TAG).d( "showEmpty" );

      clear();
      printLetterHead( getString(R.string.decision_blank) );
      hideMagnifer();

    }

    private void hideMagnifer() {
      magnifer.setAlpha(0.4f);
      magnifer.setFocusable(false);
      magnifer.setClickable(false);
    }

    private void showMagnifer() {
      magnifer.setAlpha(1.0f);
      magnifer.setFocusable(true);
      magnifer.setClickable(true);
    }

    private void printSigner(RDecisionEntity decision, String registrationNumber) {

//      Timber.tag(TAG).i("DECISION\n%s", new Gson().toJson(decision));

      LinearLayout relativeSigner = new LinearLayout(context);
      relativeSigner.setOrientation(LinearLayout.VERTICAL);
      relativeSigner.setVerticalGravity( Gravity.BOTTOM );
      relativeSigner.setPadding(0,0,0,0);
      LinearLayout.LayoutParams relativeSigner_params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
      relativeSigner_params.height = LinearLayout.LayoutParams.WRAP_CONTENT;
      relativeSigner.setLayoutParams( relativeSigner_params );


      LinearLayout.LayoutParams viewsLayotuParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);


      LinearLayout signer_view = new LinearLayout(context);
      signer_view.setOrientation(LinearLayout.VERTICAL);

      if ( decision.isShowPosition() != null && decision.isShowPosition() ){
        TextView signerPositionView = new TextView(context);
        signerPositionView.setText( decision.getSignerPositionS() );
        signerPositionView.setTextColor( ContextCompat.getColor(context, R.color.md_grey_800) );
        signerPositionView.setTypeface( Typeface.create("sans-serif-light", Typeface.NORMAL) );
        signerPositionView.setGravity( Gravity.END );

        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
          LinearLayout.LayoutParams.MATCH_PARENT,
          LinearLayout.LayoutParams.MATCH_PARENT,
          1.0f
        );
        signerPositionView.setLayoutParams(param);

        signer_view.addView( signerPositionView );
      }

      TextView signerBlankTextView = new TextView(context);
      signerBlankTextView.setText( Declension.formatName( decision.getSignerBlankText() ) );
      signerBlankTextView.setTextColor( Color.BLACK );
      signerBlankTextView.setGravity( Gravity.END);
      signerBlankTextView.setTypeface( Typeface.create("sans-serif-medium", Typeface.NORMAL) );
      signerBlankTextView.setLayoutParams(viewsLayotuParams);

      LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.MATCH_PARENT,
        1.0f
      );
      signerBlankTextView.setLayoutParams(param);

      signer_view.addView( signerBlankTextView );





      LinearLayout date_and_number_view = new LinearLayout(context);
      date_and_number_view.setOrientation(LinearLayout.HORIZONTAL);

      TextView numberView = new TextView(context);
      numberView.setText( "№ " + registrationNumber );
      numberView.setTextColor( Color.BLACK );
      numberView.setTypeface( Typeface.create("sans-serif-medium", Typeface.NORMAL) );
      numberView.setLayoutParams(viewsLayotuParams);
      numberView.setGravity( Gravity.END );

      TextView dateView = new TextView(context);
      dateView.setText( decision.getDate() );
      dateView.setGravity( Gravity.START );
      dateView.setTextColor( Color.BLACK );
      dateView.setTypeface( Typeface.create("sans-serif-medium", Typeface.NORMAL) );
      dateView.setLayoutParams(viewsLayotuParams);

      date_and_number_view.addView(dateView);
      date_and_number_view.addView(numberView);


      if (decision.getSignBase64() != null){
        ImageView image = new ImageView(getContext());


        byte[] decodedString = Base64.decode( decision.getSignBase64() , Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

        image.setImageBitmap( decodedByte );
        relativeSigner.addView( image );
      }

      relativeSigner.addView( signer_view );
      relativeSigner.addView( date_and_number_view );


      preview_bottom.addView( relativeSigner );
    }

    private void printUrgency(String urgency) {
      TextView urgencyView = new TextView(context);
      urgencyView.setGravity(Gravity.RIGHT);
      urgencyView.setAllCaps(true);
      urgencyView.setPaintFlags( Paint.UNDERLINE_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG | Paint.FAKE_BOLD_TEXT_FLAG );
      urgencyView.setText( urgency );
      urgencyView.setTextColor( ContextCompat.getColor(context, R.color.md_black_1000) );

      LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
      params.setMargins(0,2,0,2);
      urgencyView.setLayoutParams(params);

      preview_head.addView( urgencyView );
    }

    private void printLetterHead(String letterhead) {
      TextView letterHead = new TextView(context);
      letterHead.setGravity(Gravity.CENTER);
      letterHead.setText( letterhead );
      letterHead.setTextColor( Color.BLACK );
      letterHead.setTypeface( Typeface.create("sans-serif-medium", Typeface.NORMAL) );
      preview_head.addView( letterHead );

      TextView delimiter = new TextView(context);
      delimiter.setGravity(Gravity.CENTER);
      delimiter.setHeight(1);
      delimiter.setWidth(400);
      delimiter.setBackgroundColor( ContextCompat.getColor(context, R.color.md_blue_grey_200) );

      LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
      params.setMargins(50, 10, 50, 10);
      delimiter.setLayoutParams(params);

      preview_head.addView( delimiter );
    }

    private void printBlockText(RBlockEntity block, Boolean isOnlyOneBlock) {
      TextView block_view = new TextView(context);
      block_view.setTextColor( Color.BLACK );
      block_view.setTypeface( Typeface.create("sans-serif-light", Typeface.NORMAL) );

      if ( !isOnlyOneBlock && block.isHidePerformers() != null && block.isHidePerformers() ){
        block_view.setText( String.format( "%s. %s", block.getNumber(), block.getText() ) );
      } else {
        block_view.setText( block.getText() );
      }

      LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
      params.setMargins(0, 10, 0, 10);
      block_view.setLayoutParams(params);

      preview_body.addView( block_view );
    }

    private void printAppealText(RBlock _block, Boolean isOnlyOneBlock) {

      RBlockEntity block = (RBlockEntity) _block;
      String text = "";

      if (block.getAppealText() != null && !Objects.equals(block.getAppealText(), "")) {

        if (!isOnlyOneBlock ){
          text += block.getNumber().toString() + ". ";
        }

        text += block.getAppealText();
      }

      TextView blockAppealView = new TextView(context);
      blockAppealView.setGravity(Gravity.CENTER);
      blockAppealView.setText( text );
      blockAppealView.setTextColor( Color.BLACK );
      blockAppealView.setTextSize( TypedValue.COMPLEX_UNIT_SP, 12 );

      preview_body.addView( blockAppealView );
    }

    private void printBlockPerformers(RBlock _block, Boolean isOnlyOneBlock) {

      RBlockEntity block = (RBlockEntity) _block;

      boolean numberPrinted = false;

      LinearLayout users_view = new LinearLayout(context);
      users_view.setOrientation(LinearLayout.VERTICAL);
      users_view.setPadding(40,5,5,5);

      if( block.getPerformers().size() > 0 ){

        Set<RPerformer> users = block.getPerformers();
        ArrayList<RPerformerEntity> _users = new ArrayList<>();

        for (RPerformer _user: users){
          RPerformerEntity user = (RPerformerEntity) _user;
          _users.add(user);
        }

        Collections.sort(_users, (o1, o2) -> o1.getNumber().compareTo( o2.getNumber() ));

        for (RPerformerEntity user: _users){

          String performerName = "";

          String tempPerformerName = Declension.getPerformerNameForDecisionPreview(
            user.getPerformerText(),
            user.getPerformerGender(),
            block.getAppealText()
          );

          if ( block.getAppealText() == null && !numberPrinted && !isOnlyOneBlock ){
            performerName += block.getNumber().toString() + ". ";
            numberPrinted = true;
          }

          performerName += tempPerformerName;

          if (user.isIsResponsible() != null && user.isIsResponsible()){
            performerName += " *";
          }

//          performerName = performerName.replaceAll( "\\(.+\\)", "" );


          TextView performer_view = new TextView( getActivity() );
          performer_view.setText( performerName );
          performer_view.setTextColor( Color.BLACK );
          performer_view.setPaintFlags( Paint.ANTI_ALIAS_FLAG );
          performer_view.setGravity(Gravity.CENTER);
          performer_view.setTypeface( Typeface.create("sans-serif-medium", Typeface.NORMAL) );

          users_view.addView(performer_view);
        }
      }


      preview_body.addView( users_view );
    }

  }

  private void initEvents() {
    Timber.tag(TAG).v("initEvents");
    if (EventBus.getDefault().isRegistered(this)) {
      EventBus.getDefault().unregister(this);
    }
    EventBus.getDefault().register(this);
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(ApproveDecisionEvent event) throws Exception {
    Timber.d("ApproveDecisionEvent");
    current_decision.setApproved(true);
    displayDecision();
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(RejectDecisionEvent event) throws Exception {
    Timber.d("RejectDecisionEvent");
    current_decision.setApproved(false);
    displayDecision();
    hideButtons();
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(UpdateCurrentDocumentEvent event) throws Exception {
    Timber.tag(TAG).w("UpdateCurrentDocumentEvent %s", event.uid);
    if (Objects.equals(event.uid, settings.getUid())){
      invalidate();
    }
  }


  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(InvalidateDecisionSpinnerEvent event) throws Exception {
    Timber.tag(TAG).w("InvalidateDecisionSpinnerEvent %s", event.uid);
    decision_spinner_adapter.invalidate(event.uid);
  }


  private void hideButtons() {
    next_person_button.setVisibility( View.INVISIBLE );
    prev_person_button.setVisibility( View.INVISIBLE );
  }

}
