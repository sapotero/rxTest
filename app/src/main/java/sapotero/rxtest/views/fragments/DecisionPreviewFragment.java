package sapotero.rxtest.views.fragments;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.managers.menu.OperationManager;
import sapotero.rxtest.managers.menu.factories.CommandFactory;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import sapotero.rxtest.managers.view.interfaces.DecisionInterface;
import sapotero.rxtest.retrofit.models.document.Block;
import sapotero.rxtest.retrofit.models.document.Decision;
import sapotero.rxtest.retrofit.models.document.Performer;
import sapotero.rxtest.utils.ISettings;
import sapotero.rxtest.utils.padeg.Declension;
import timber.log.Timber;

public class DecisionPreviewFragment extends Fragment implements DecisionInterface {

  @Inject ISettings settings;
  @Inject OperationManager operationManager;

  private Context mContext;

  @BindView(R.id.decision_preview_bottom) LinearLayout decision_preview_bottom;
  @BindView(R.id.decision_preview_head)   LinearLayout decision_preview_head;
  @BindView(R.id.decision_preview_body)   LinearLayout decision_preview_body;
  @BindView(R.id.fragment_decision_preview_button_wrapper)   LinearLayout wrapper;

  @BindView(R.id.fragment_decision_preview_next_person) Button next_person_button;
  @BindView(R.id.fragment_decision_preview_prev_person) Button prev_person_button;

  private View view;
  private String TAG = this.getClass().getSimpleName();
  public Decision decision;

  public DecisionPreviewFragment() {
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (getArguments() != null) {
      Gson gson = new Gson();
      decision = gson.fromJson(getArguments().getString("decision"), Decision.class);
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    view = inflater.inflate(R.layout.fragment_decision_preview, container, false);

    ButterKnife.bind(this, view);
    EsdApplication.getManagerComponent().inject( this );

    if (decision != null){
      updateView();
    }

    updateVisibility();

    return view;
  }

  private void updateVisibility() {
    if (decision == null || decision.getId() == null){
      wrapper.setVisibility(View.GONE);
    } else {
      next_person_button.setVisibility(View.VISIBLE);
      prev_person_button.setVisibility(View.VISIBLE);
    }


    if ( settings.isDecisionWithAssignment() ){
      wrapper.setVisibility(View.GONE);
    }
//
//    showDecisionCardTollbarMenuItems(true);
//
//    // FIX для ссылок
//    if (current_decision == null) {
//      next_person_button.setVisibility( !approved ? View.INVISIBLE : View.GONE);
//      prev_person_button.setVisibility( !approved ? View.INVISIBLE : View.GONE);
//    }
  }

  // Approve current decision
  @OnClick(R.id.fragment_decision_preview_next_person)
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

          //CommandFactory.Operation operation = CommandFactory.Operation.APPROVE_DECISION;
          CommandFactory.Operation operation = CommandFactory.Operation.SAVE_AND_APPROVE_DECISION;

          CommandParams params = new CommandParams();

          params.setDecisionId( decision.getId() );
          params.setDecisionModel( decision );

          operationManager.execute(operation, params);

        })
        .autoDismiss(true);

      prev_dialog.build().show();

    } else {
      CommandFactory.Operation operation;
      //operation =CommandFactory.Operation.APPROVE_DECISION;
      operation =CommandFactory.Operation.SAVE_AND_APPROVE_DECISION;

      CommandParams params = new CommandParams();

      params.setDecisionId( decision.getId() );
      params.setDecisionModel( decision );

      operationManager.execute(operation, params);
    }


    Timber.tag(TAG).v("decision_preview_next end");
  }

  // Reject current decision
  @OnClick(R.id.fragment_decision_preview_prev_person)
  public void decision_preview_prev(){
    Timber.tag(TAG).v("decision_preview_prev");

    // resolved https://tasks.n-core.ru/browse/MVDESD-12765
    // Добавить ввод комментариев на "Отклонить резолюцию" и "без ответа"

    if ( settings.isShowCommentPost() ){

      MaterialDialog.Builder prev_dialog = new MaterialDialog.Builder( getContext() )
        .content(R.string.decision_reject_body)
        .cancelable(true)
        .positiveText(R.string.yes)
        .negativeText(R.string.no)
        .onPositive((dialog1, which) -> {

          CommandFactory.Operation operation =CommandFactory.Operation.REJECT_DECISION;

          CommandParams commandParams = new CommandParams();
          commandParams.setDecisionId( decision.getId() );
          commandParams.setDecisionModel( decision );
          commandParams.setComment( dialog1.getInputEditText().getText().toString() );

          operationManager.execute(operation, commandParams);
        })
        .autoDismiss(true);

      // настройка
      // Показывать комментарий при отклонении
      if ( settings.isShowCommentPost() ){
        prev_dialog.inputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES )
          .input(R.string.comment_hint, R.string.dialog_empty_value, (dialog12, input) -> {});
      }


      prev_dialog.build().show();

    } else {

      CommandFactory.Operation operation;
      operation =CommandFactory.Operation.REJECT_DECISION;

      CommandParams params = new CommandParams();
      params.setDecisionId( decision.getId() );
      params.setDecisionModel( decision );

      operationManager.execute(operation, params);
    }

  }

  private void clear(){
    decision_preview_head.removeAllViews();
    decision_preview_body.removeAllViews();
    decision_preview_bottom.removeAllViews();
  };

  private void showEmpty(){
    Timber.tag(TAG).d( "showEmpty" );

    clear();
    updateSignLetterhead( getString(R.string.decision_blank) );
  }

  private void updateView() {
    clear();

    updateSignLetterhead(null);
    updateUrgency();
    updateSignText();
    updateData();
  }

  private void updateData() {
    Timber.tag("ERROR").w("size: %s", decision.getBlocks().size() );
    if( decision.getBlocks() != null && decision.getBlocks().size() > 0 ){
      List<Block> blocks = decision.getBlocks();

      Collections.sort(blocks, (o1, o2) -> o1.getNumber() != null && o2.getNumber() != null ? o1.getNumber().compareTo( o2.getNumber() ) : 0 );

      for (Block block: blocks){
        Timber.tag("block").v( block.getText() );
        Timber.tag("block").v(String.valueOf(block.getNumber()));
        Timber.tag("block").v(String.valueOf(block.getHidePerformers()));

        Boolean isOnlyOneBlock = false;

        if (blocks.size() == 1){
          isOnlyOneBlock = true;
        }

        setAppealText( block, isOnlyOneBlock );

        if ( block.getTextBefore() != null && block.getTextBefore() ){
          setBlockText( block, isOnlyOneBlock );

          if ( block.getHidePerformers() != null && !block.getHidePerformers() ) {
            setBlockPerformers( block, isOnlyOneBlock );
          }
        } else {
          if ( block.getHidePerformers() != null && !block.getHidePerformers() ) {
            setBlockPerformers( block, isOnlyOneBlock );
          }
          setBlockText( block, isOnlyOneBlock );
        }
      }
    }
  }

  private void setBlockPerformers(Block block, Boolean isOnlyOneBlock) {

    boolean numberPrinted = false;

    LinearLayout users_view = new LinearLayout( getActivity() );
    users_view.setOrientation(LinearLayout.VERTICAL);
    users_view.setPadding(40,5,5,5);

    if( block.getPerformers().size() > 0 ){
      for (Performer user: block.getPerformers()){

        String performerName = "";

        String tempPerformerName =
                Declension.getPerformerNameForDecisionPreview(user.getPerformerText(), user.getPerformerGender(), block.getAppealText());

        Timber.tag("TEST").w("null? - %s | %s", block.getAppealText() == null, block.getAppealText() );
        Timber.tag("TEST").w("user %s", new Gson().toJson( user ) );

        if (Objects.equals(block.getAppealText(), "") && !numberPrinted && !isOnlyOneBlock  ){
          performerName += block.getNumber().toString() + ". ";
          numberPrinted = true;
        }

        performerName += tempPerformerName;

        if (user.getIsResponsible() != null && user.getIsResponsible()){
          performerName += " *";
        }

        TextView performer_view = new TextView( getActivity() );
        performer_view.setText( performerName );
        performer_view.setTextColor( Color.BLACK );
        performer_view.setPaintFlags( Paint.ANTI_ALIAS_FLAG );
        performer_view.setGravity(Gravity.CENTER);
        performer_view.setTypeface( Typeface.create("sans-serif-medium", Typeface.NORMAL) );
        users_view.addView(performer_view);
      }
    }

    decision_preview_body.addView( users_view );
  }

  private void setBlockText(Block block, Boolean isOnlyOneBlock) {
    TextView block_view = new TextView( getActivity() );
    block_view.setTextColor( Color.BLACK );
    block_view.setTypeface( Typeface.create("sans-serif-light", Typeface.NORMAL) );


    if ( !isOnlyOneBlock && block.getHidePerformers() != null && block.getHidePerformers() && ( block.getAppealText() == null || Objects.equals(block.getAppealText(), "") ) ) {
      block_view.setText( String.format( "%s. %s", block.getNumber(), block.getText() ) );
    } else {
      block_view.setText( block.getText() );
    }

    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    params.setMargins(0, 10, 0, 10);
    block_view.setLayoutParams(params);


    decision_preview_body.addView( block_view );
  }

  private void setAppealText(Block block, Boolean isOnlyOneBlock) {

    String text = "";

    if (block.getAppealText() != null && !Objects.equals(block.getAppealText(), "")) {

      if (!isOnlyOneBlock ){
        text += block.getNumber().toString() + ". ";
      }

      text += block.getAppealText();
    }

    TextView blockAppealView = new TextView( getContext() );
    blockAppealView.setGravity(Gravity.CENTER);
    blockAppealView.setText( text );
    blockAppealView.setTextColor( Color.BLACK );
    blockAppealView.setTextSize( TypedValue.COMPLEX_UNIT_SP, 12 );

    decision_preview_body.addView( blockAppealView );
  }

  private void updateUrgency() {
    String urgency = "";
    if ( decision.getUrgencyText() != null ){
      urgency = decision.getUrgencyText();
    }

    if (!Objects.equals(urgency, "")){
      TextView urgencyView = new TextView( getActivity() );
      urgencyView.setGravity(Gravity.END);
      urgencyView.setAllCaps(true);
      urgencyView.setPaintFlags( Paint.UNDERLINE_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG | Paint.FAKE_BOLD_TEXT_FLAG );
      urgencyView.setText( urgency );
      urgencyView.setTextColor( ContextCompat.getColor(mContext, R.color.md_black_1000) );

      decision_preview_body.addView( urgencyView );
    }
  }

  private void updateSignLetterhead(String text) {
    Context context = getContext();

    TextView letterHead = new TextView(context);
    letterHead.setGravity(Gravity.CENTER);
    letterHead.setText( text == null ? decision.getLetterhead() : text );
    letterHead.setTextColor( Color.BLACK );
    letterHead.setTypeface( Typeface.create("sans-serif-medium", Typeface.NORMAL) );
    decision_preview_head.addView( letterHead );

    TextView delimiter = new TextView(context);
    delimiter.setGravity(Gravity.CENTER);
    delimiter.setHeight(1);
    delimiter.setWidth(400);
    delimiter.setBackgroundColor( ContextCompat.getColor(context, R.color.md_blue_grey_200) );

    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    params.setMargins(50, 10, 50, 10);
    delimiter.setLayoutParams(params);

    decision_preview_head.addView( delimiter );
  }

  public void updateSignText() {

    Timber.tag(TAG).i( "DecisionPreviewUpdate\n%s", new Gson().toJson(decision) );

    LinearLayout relativeSigner = new LinearLayout( getActivity() );
    relativeSigner.setOrientation(LinearLayout.VERTICAL);
    relativeSigner.setVerticalGravity( Gravity.BOTTOM );
//    relativeSigner.setMinimumHeight(350);
    LinearLayout.LayoutParams relativeSigner_params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
    relativeSigner_params.height = LinearLayout.LayoutParams.WRAP_CONTENT;
    relativeSigner.setLayoutParams( relativeSigner_params );




    LinearLayout signer_view = new LinearLayout(getActivity());
    signer_view.setOrientation(LinearLayout.VERTICAL);
    signer_view.setWeightSum(2);
//    signer_view.setPadding(0,40,0,0);

    if ( decision.getShowPosition() ){
      TextView signerPositionView = new TextView(getActivity());
      signerPositionView.setText( decision.getSignerPositionS() );
      signerPositionView.setTextColor( Color.BLACK );
      signerPositionView.setGravity( Gravity.END );
      signerPositionView.setTypeface( Typeface.create("sans-serif-medium", Typeface.NORMAL) );

      LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.MATCH_PARENT,
        1.0f
      );
      signerPositionView.setLayoutParams(param);

      signer_view.addView( signerPositionView );
    }

    TextView signerBlankTextView = new TextView(getActivity());
    signerBlankTextView.setText( Declension.formatName(decision.getSignerBlankText()) );
    signerBlankTextView.setTextColor( Color.BLACK );
    signerBlankTextView.setGravity( Gravity.END);
    signerBlankTextView.setTypeface( Typeface.create("sans-serif-medium", Typeface.NORMAL) );

    LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
      LinearLayout.LayoutParams.MATCH_PARENT,
      LinearLayout.LayoutParams.MATCH_PARENT,
      1.0f
    );
    signerBlankTextView.setLayoutParams(param);
    signer_view.addView( signerBlankTextView );





    LinearLayout date_and_number_view = new LinearLayout(getActivity());
    date_and_number_view.setOrientation(LinearLayout.HORIZONTAL);

    TextView numberView = new TextView(getActivity());
    numberView.setText( "№ " + settings.getRegNumber() );
    numberView.setTextColor( Color.BLACK );
    LinearLayout.LayoutParams numberViewParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
    numberView.setLayoutParams(numberViewParams);
    numberView.setGravity( Gravity.END );
    numberView.setTypeface( Typeface.create("sans-serif-medium", Typeface.NORMAL) );

    TextView dateView = new TextView(getActivity());
    dateView.setText( decision.getDate() );
    dateView.setGravity( Gravity.START );
    dateView.setTextColor( Color.BLACK );
    dateView.setTypeface( Typeface.create("sans-serif-medium", Typeface.NORMAL) );

    RelativeLayout.LayoutParams dateView_params = new RelativeLayout.LayoutParams(
      RelativeLayout.LayoutParams.MATCH_PARENT,
      RelativeLayout.LayoutParams.WRAP_CONTENT
    );

    dateView_params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
    dateView.setLayoutParams(dateView_params);
    LinearLayout.LayoutParams dateView_params1 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
    dateView.setLayoutParams(dateView_params1);

    date_and_number_view.addView(dateView);
    date_and_number_view.addView(numberView);

    relativeSigner.addView( signer_view );
    relativeSigner.addView( date_and_number_view );

    decision_preview_bottom.addView( relativeSigner );


  }

  public void setBlocks(ArrayList<Block> blocks) {
    decision.setBlocks(blocks);
    updateView();
  }

  public void getBlocksInfo() {
    for (Block block : decision.getBlocks() ){
      Timber.tag(TAG).i(String.valueOf(block.getNumber()));
      Timber.tag(TAG).i( block.getText() );
    }

  }

  public void update() {
    try{
//      Timber.tag(TAG).w( "UPDATE: %s", decision.getBlocks().startTransactionFor(0).getText() );
      updateView();
    }
    catch (Error e){
      Timber.tag(TAG).w( e );
    }

  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    mContext = context;
  }

  @Override
  public Decision getDecision() {
    return null;
  }

  @Override
  public void setDecision(Decision _decision_) {
    decision = _decision_;
  }
}
