package sapotero.rxtest.views.dialogs;

import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import sapotero.rxtest.R;
import sapotero.rxtest.retrofit.models.document.Block;
import sapotero.rxtest.retrofit.models.document.Decision;
import sapotero.rxtest.retrofit.models.document.Performer;
import sapotero.rxtest.views.adapters.models.DecisionSpinnerItem;
import timber.log.Timber;

public class DecisionMagniferFragment extends DialogFragment implements View.OnClickListener {

  private String TAG = this.getClass().getSimpleName();
  private int font_size = 12;

//  @BindView(R.id.dialog_magnifer_decision_button_cancel)  Button button_cancel;
  @BindView(R.id.dialog_magnifer_decision_seekbar_font_size) SeekBar seekbar;

  @BindView(R.id.dialog_magniger_preview_head)   LinearLayout preview_head;
  @BindView(R.id.dialog_magniger_preview_body)   LinearLayout preview_body;
  @BindView(R.id.dialog_magniger_preview_bottom) LinearLayout preview_bottom;


  private ArrayList<TextView> textLabels = new ArrayList<>();

  private DecisionSpinnerItem decision;
  private String regNumber;
  private Preview viewer
    ;

  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.dialog_magnifer_decision, null);
    ButterKnife.bind(this, view);


    seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        updateTextSize(12 + progress);
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {

      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {

      }
    });

    if (decision != null && decision.getDecision() != null){
      viewer = new Preview( getActivity() );
      viewer.show( decision.getDecision() );
    }

    return view;
  }

  public void updateTextSize(Integer size){

    for (TextView view : textLabels){
      view.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
    }

  }

//  @OnClick(R.id.dialog_magnifer_decision_button_cancel)
//  public void close(){
//    dismiss();
//  }

  public void onDismiss(DialogInterface dialog) {
    super.onDismiss(dialog);
    Timber.tag(TAG).i( "onDismiss");
  }

  public void onCancel(DialogInterface dialog) {
    super.onCancel(dialog);
    Timber.tag(TAG).i( "onCancel");
  }

  @Override
  public void onClick(View v) {

  }

  public void setDecision(DecisionSpinnerItem item) {
    this.decision = item;
  }

  public void setRegNumber(String regNumber) {
    this.regNumber = regNumber;
  }


  public class Preview{

    private final Context context;
    private String TAG = this.getClass().getSimpleName();

    public Preview(Context context) {
      this.context = context;
    }

    private void clear(){
      preview_head.removeAllViews();
      preview_body.removeAllViews();
      preview_bottom.removeAllViews();
    };

    private void show( Decision decision ){
      clear();

      if( decision.getLetterhead() != null ) {
        printLetterHead(decision.getLetterhead());
      }
      if( decision.getUrgencyText() != null ){
        printUrgency( decision.getUrgencyText().toString() );
      }

      if( decision.getBlocks().size() > 0 ){
        List<Block> blocks = decision.getBlocks();
        for (Block block: blocks){
          Timber.tag("block").v( block.getText() );
          printAppealText( block );

          Boolean f = block.getToFamiliarization();
          if (f == null)
            f = false;

          if ( block.getTextBefore() ){
            printBlockText( block.getText() );
            if (!block.getHidePerformers())
              printBlockPerformers( block.getPerformers(), f, block.getNumber() );

          } else {
            if (!block.getHidePerformers())
              printBlockPerformers( block.getPerformers(), f, block.getNumber() );
            printBlockText( block.getText() );
          }
        }
      }


      printSigner( decision.getShowPosition(), decision.getSignerBlankText(), decision.getSignerPositionS(), decision.getDate(), regNumber  );
    }

    private void showEmpty(){
      Timber.tag(TAG).d( "showEmpty" );

      clear();
      printLetterHead( getString(R.string.decision_blank) );
    }

    private void printSigner(Boolean showPosition, String signerBlankText, String signerPositionS, String date, String registrationNumber) {

      LinearLayout relativeSigner = new LinearLayout(context);
      relativeSigner.setOrientation(LinearLayout.VERTICAL);
      relativeSigner.setVerticalGravity( Gravity.BOTTOM );
      relativeSigner.setPadding(0,0,0,0);
//      relativeSigner.setMinimumHeight(350);
      LinearLayout.LayoutParams relativeSigner_params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
      relativeSigner_params.height = LinearLayout.LayoutParams.WRAP_CONTENT;
      relativeSigner.setLayoutParams( relativeSigner_params );




      LinearLayout signer_view = new LinearLayout(context);
      signer_view.setOrientation(LinearLayout.VERTICAL);
//      signer_view.setPadding(0,0,0,0);

      if ( showPosition ){
        TextView signerPositionView = new TextView(context);
        signerPositionView.setText( signerPositionS );
        signerPositionView.setTextColor( Color.BLACK );
        signerPositionView.setGravity( Gravity.END );
        signer_view.addView( signerPositionView );
        textLabels.add( signerPositionView );
      }
      TextView signerBlankTextView = new TextView(context);
      signerBlankTextView.setText( signerBlankText );
      signerBlankTextView.setTextColor( Color.BLACK );
      signerBlankTextView.setGravity( Gravity.END);
      signer_view.addView( signerBlankTextView );
      textLabels.add( signerBlankTextView );




      LinearLayout date_and_number_view = new LinearLayout(context);
      date_and_number_view.setOrientation(LinearLayout.HORIZONTAL);

      TextView numberView = new TextView(context);
      numberView.setText( "№ " + registrationNumber );
      numberView.setTextColor( Color.BLACK );
      LinearLayout.LayoutParams numberViewParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
      numberView.setLayoutParams(numberViewParams);
      date_and_number_view.addView(numberView);

      textLabels.add( numberView );

      TextView dateView = new TextView(context);
      dateView.setText( date );
      dateView.setGravity( Gravity.END );
      dateView.setTextColor( Color.BLACK );
      RelativeLayout.LayoutParams dateView_params = new RelativeLayout.LayoutParams(
        RelativeLayout.LayoutParams.MATCH_PARENT,
        RelativeLayout.LayoutParams.WRAP_CONTENT);
      dateView_params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
      dateView.setLayoutParams(dateView_params);
      LinearLayout.LayoutParams dateView_params1 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
      dateView.setLayoutParams(dateView_params1);
      date_and_number_view.addView(dateView);
      textLabels.add( dateView );

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
      params.setMargins(0,0,0,10);
      urgencyView.setLayoutParams(params);

      preview_head.addView( urgencyView );
    }

    private void printLetterHead(String letterhead) {
      TextView letterHead = new TextView(context);
      letterHead.setGravity(Gravity.CENTER);
      letterHead.setText( letterhead );
      letterHead.setTextColor( Color.BLACK );
      preview_head.addView( letterHead );

      TextView delimiter = new TextView(context);
      delimiter.setGravity(Gravity.CENTER);
      delimiter.setHeight(1);
      delimiter.setWidth(400);
      delimiter.setBackgroundColor( ContextCompat.getColor(context, R.color.md_blue_grey_200) );

      LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
      params.setMargins(50, 10, 50, 10);
      delimiter.setLayoutParams(params);
    }

    private void printBlockText(String text) {
      TextView block_view = new TextView(context);
      block_view.setText( text );
      block_view.setTextColor( Color.BLACK );

      LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
      params.setMargins(0, 10, 0, 10);
      block_view.setLayoutParams(params);

      textLabels.add( block_view );
      preview_body.addView( block_view );
    }

    private void printAppealText( Block block ) {

      String text = "";
      String appealText;
      String number;
      boolean toFamiliarization = block.getToFamiliarization() == null ? false : block.getToFamiliarization();

      if ( block.getAppealText() != null ){
        appealText = block.getAppealText().toString();
      } else {
        appealText = "";
      }

      if ( block.getNumber() != null ){
        number = block.getNumber().toString();
      } else {
        number = "1";
      }



      if (toFamiliarization){
        text += number + ". ";
        block.setToFamiliarization(false);
      }
      text += appealText;

      TextView blockAppealView = new TextView(context);
      blockAppealView.setGravity(Gravity.CENTER);
      blockAppealView.setText( text );
      blockAppealView.setTextColor( Color.BLACK );
      blockAppealView.setTextSize( TypedValue.COMPLEX_UNIT_SP, 12 );

      textLabels.add( blockAppealView );
      preview_body.addView( blockAppealView );
    }

    private void printBlockPerformers(List<Performer> performers, Boolean toFamiliarization, Integer number) {

      boolean numberPrinted = false;
      LinearLayout users_view = new LinearLayout(context);
      users_view.setOrientation(LinearLayout.VERTICAL);
      users_view.setPadding(40,5,5,5);

      if( performers.size() > 0 ){
        List<Performer> users = performers;
        for (Performer user: users){

          String performerName = "";

          if (toFamiliarization && !numberPrinted){
            performerName += number.toString() + ". ";
            numberPrinted = true;
          } else {
            performerName += user.getPerformerText();
          }

          TextView performer_view = new TextView(context);
          performer_view.setText( performerName );
          performer_view.setTextColor( Color.BLACK );
          users_view.addView(performer_view);
          textLabels.add( performer_view );
        }
      }


      preview_body.addView( users_view );
    }


  }
}
