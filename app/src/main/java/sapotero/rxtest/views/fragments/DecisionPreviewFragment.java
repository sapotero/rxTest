package sapotero.rxtest.views.fragments;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.f2prateek.rx.preferences.RxSharedPreferences;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.requery.Persistable;
import io.requery.rx.SingleEntityStore;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.retrofit.models.document.Block;
import sapotero.rxtest.retrofit.models.document.Decision;
import sapotero.rxtest.retrofit.models.document.Performer;
import sapotero.rxtest.views.managers.toolbar.ToolbarManager;
import sapotero.rxtest.views.managers.view.interfaces.DecisionInterface;
import timber.log.Timber;

public class DecisionPreviewFragment extends Fragment implements DecisionInterface {

  @Inject RxSharedPreferences settings;
  @Inject SingleEntityStore<Persistable> dataStore;

  private OnFragmentInteractionListener mListener;
  private Context mContext;

  @BindView(R.id.decision_preview_sign_text) FrameLayout decision_preview_sign_text;
  @BindView(R.id.decision_preview_head) TextView decision_preview_head;

  @BindView(R.id.decision_preview_body) LinearLayout decision_preview_body;


  private View view;
  private String TAG = this.getClass().getSimpleName();
  public Decision decision;
  private ToolbarManager toolbarManager;

  public DecisionPreviewFragment() {
  }

  public static DecisionPreviewFragment newInstance(String param1, String param2) {
    DecisionPreviewFragment fragment = new DecisionPreviewFragment();


    return fragment;
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
    EsdApplication.getComponent( mContext ).inject( this );

    if (decision != null){
      updateView();
    }

    return view;
  }

  private void updateView() {
    decision_preview_head.setText("");
    decision_preview_body.removeAllViews();
    decision_preview_sign_text.removeAllViews();

    updateSignLetterhead();
    updateUrgency();
    updateSignText();
    updateData();
  }

  private void updateData() {
    if( decision.getBlocks().size() > 0 ){
      List<Block> blocks = decision.getBlocks();
      for (Block block: blocks){
        Timber.tag("block").v( block.getText() );
        Timber.tag("block").v(String.valueOf(block.getNumber()));
        Timber.tag("block").v(String.valueOf(block.getHidePerformers()));
        setAppealText( block );

        Boolean toFamiliarization = block.getToFamiliarization();
        if (toFamiliarization == null){
          toFamiliarization = false;
        }

        if ( block.getTextBefore() ){
          setBlockText( block.getText() );
          if (!block.getHidePerformers())
            setBlockPerformers( block.getPerformers(), toFamiliarization, block.getNumber() );
        } else {
          if (!block.getHidePerformers())
            setBlockPerformers( block.getPerformers(), toFamiliarization, block.getNumber() );
          setBlockText( block.getText() );
        }
      }
    }
  }

  private void setBlockPerformers(List<Performer> performers, Boolean toFamiliarization, Integer number) {

    boolean numberPrinted = false;
    LinearLayout users_view = new LinearLayout( getActivity() );
    users_view.setOrientation(LinearLayout.VERTICAL);
    users_view.setPadding(40,5,5,5);

    if( performers.size() > 0 ){
      for (Performer user: performers){

        String performerName = "";

        if (user.getIsOriginal() != null && user.getIsOriginal()){
          performerName += "* ";
        }

        if (toFamiliarization && !numberPrinted){
          performerName += number.toString() + ". ";
          numberPrinted = true;
        } else {
          performerName += user.getPerformerText();
        }


        TextView performer_view = new TextView( getActivity() );
        performer_view.setText( performerName );
        performer_view.setTextColor( Color.BLACK );
        users_view.addView(performer_view);
      }
    }


    decision_preview_body.addView( users_view );
  }

  private void setBlockText(String text) {
    TextView block_view = new TextView( getActivity() );
    block_view.setText( text );
    block_view.setTextColor( Color.BLACK );

    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    params.setMargins(0, 10, 0, 10);
    block_view.setLayoutParams(params);

    decision_preview_body.addView( block_view );
  }

  private void setAppealText(Block block) {


    String text = "";

    if ( block.getToFamiliarization() != null && block.getToFamiliarization() ){
      block.setToFamiliarization(false);
    }

    if ( decision.getShowPosition() != null && decision.getShowPosition() ){
      text += block.getNumber().toString() + ". ";
    }

    if (block.getAppealText() != null) {
      text += block.getAppealText();
    }

    TextView blockAppealView = new TextView( getActivity() );
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

  private void updateSignLetterhead() {
    decision_preview_head.setText( decision.getLetterhead() );
  }

  public void onButtonPressed(Uri uri) {
    if (mListener != null) {
      mListener.onFragmentInteraction(uri);
    }
  }

  public void updateSignText() {

    Timber.tag(TAG).i( "++++++++++" + decision.toString() );

    LinearLayout relativeSigner = new LinearLayout( getActivity() );
    relativeSigner.setOrientation(LinearLayout.VERTICAL);
    relativeSigner.setVerticalGravity( Gravity.BOTTOM );
//    relativeSigner.setMinimumHeight(350);
    LinearLayout.LayoutParams relativeSigner_params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    relativeSigner_params.height = LinearLayout.LayoutParams.WRAP_CONTENT;
    relativeSigner.setLayoutParams( relativeSigner_params );




    LinearLayout signer_view = new LinearLayout(getActivity());
    signer_view.setOrientation(LinearLayout.VERTICAL);
//    signer_view.setPadding(0,40,0,0);

    if ( decision.getShowPosition() ){
      TextView signerPositionView = new TextView(getActivity());
      signerPositionView.setText( decision.getSignerPositionS() );
      signerPositionView.setTextColor( Color.BLACK );
      signerPositionView.setGravity( Gravity.END );
      signer_view.addView( signerPositionView );
    }
    TextView signerBlankTextView = new TextView(getActivity());
    signerBlankTextView.setText( decision.getSignerBlankText() );
    signerBlankTextView.setTextColor( Color.BLACK );
    signerBlankTextView.setGravity( Gravity.END);
    signer_view.addView( signerBlankTextView );





    LinearLayout date_and_number_view = new LinearLayout(getActivity());
    date_and_number_view.setOrientation(LinearLayout.HORIZONTAL);

    TextView numberView = new TextView(getActivity());
    numberView.setText( "№ " + settings.getString("document.number").get() );
    numberView.setTextColor( Color.BLACK );
    LinearLayout.LayoutParams numberViewParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
    numberView.setLayoutParams(numberViewParams);
    date_and_number_view.addView(numberView);

    TextView dateView = new TextView(getActivity());
    dateView.setText( decision.getDate() );
    dateView.setGravity( Gravity.END );
    dateView.setTextColor( Color.BLACK );

    RelativeLayout.LayoutParams dateView_params = new RelativeLayout.LayoutParams(
      RelativeLayout.LayoutParams.MATCH_PARENT,
      RelativeLayout.LayoutParams.WRAP_CONTENT
    );

    dateView_params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
    dateView.setLayoutParams(dateView_params);
    LinearLayout.LayoutParams dateView_params1 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
    dateView.setLayoutParams(dateView_params1);
    date_and_number_view.addView(dateView);

    relativeSigner.addView( signer_view );
    relativeSigner.addView( date_and_number_view );

    decision_preview_sign_text.addView( relativeSigner );


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
      Timber.tag(TAG).w( "UPDATE: %s", decision.getBlocks().get(0).getText() );
      updateView();
    }
    catch (Error e){
      Timber.tag(TAG).w( String.valueOf(e.getStackTrace()) );
    }

  }

  public interface OnFragmentInteractionListener {
    void onFragmentInteraction(Uri uri);
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    mContext = context;
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

  @Override
  public Decision getDecision() {
    return null;
  }

  @Override
  public void setDecision(Decision _decision_) {
    decision = _decision_;
  }
}
