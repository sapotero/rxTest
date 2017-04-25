package sapotero.rxtest.views.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import sapotero.rxtest.R;
import sapotero.rxtest.db.requery.models.decisions.RBlock;
import sapotero.rxtest.db.requery.models.decisions.RBlockEntity;
import sapotero.rxtest.db.requery.models.decisions.RDecisionEntity;
import sapotero.rxtest.db.requery.models.decisions.RPerformer;
import sapotero.rxtest.db.requery.models.decisions.RPerformerEntity;
import sapotero.rxtest.db.requery.utils.DecisionConverter;
import sapotero.rxtest.views.adapters.models.DecisionSpinnerItem;
import timber.log.Timber;

public class DecisionMagniferFragment extends DialogFragment implements View.OnClickListener {

  private String TAG = this.getClass().getSimpleName();

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

    // resolved https://tasks.n-core.ru/browse/MVDESD-13131
    seekbar.setProgress(12);

    return view;
  }

  public void updateTextSize(Integer size){

    for (TextView view : textLabels){
      view.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
    }

  }

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
    private String reg_number;

    Preview(Context context) {
      this.context = context;
    }

    private void clear(){
      preview_head.removeAllViews();
      preview_body.removeAllViews();
      preview_bottom.removeAllViews();
    };

    private void show( RDecisionEntity decision ){
      clear();

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
            printBlockText( block.getText() );
            if ( block.isHidePerformers() != null && !block.isHidePerformers())
              printBlockPerformers( block, isOnlyOneBlock );

          } else {
            if ( block.isHidePerformers() != null && !block.isHidePerformers())
              printBlockPerformers( block, isOnlyOneBlock );
            printBlockText( block.getText() );
          }

        }
      }

      printSigner( decision, regNumber );
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
        textLabels.add(signerPositionView);
      }

      TextView signerBlankTextView = new TextView(context);
      signerBlankTextView.setText( DecisionConverter.formatName( decision.getSignerBlankText() ) );
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

      textLabels.add(numberView);
      textLabels.add(dateView);
      textLabels.add(signerBlankTextView);



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

      textLabels.add(urgencyView);
      preview_head.addView( urgencyView );
    }

    private void printLetterHead(String letterhead) {
      TextView letterHead = new TextView(context);
      letterHead.setGravity(Gravity.CENTER);
      letterHead.setText( letterhead );
      letterHead.setTextColor( Color.BLACK );
      letterHead.setTypeface( Typeface.create("sans-serif-medium", Typeface.NORMAL) );
      textLabels.add(letterHead);
      preview_head.addView( letterHead );
    }

    private void printBlockText(String text) {
      TextView block_view = new TextView(context);
      block_view.setText( "\u00A0     " + text );
      block_view.setTextColor( Color.BLACK );
      block_view.setTypeface( Typeface.create("sans-serif-light", Typeface.NORMAL) );

      LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
      params.setMargins(0, 10, 0, 10);
      block_view.setLayoutParams(params);

      textLabels.add(block_view);

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

      textLabels.add(blockAppealView);

      preview_body.addView( blockAppealView );
    }

    private void printBlockPerformers(RBlock _block, Boolean isOnlyOneBlock) {

      RBlockEntity block = (RBlockEntity) _block;

      boolean numberPrinted = false;
      LinearLayout users_view = new LinearLayout(context);
      users_view.setOrientation(LinearLayout.VERTICAL);
      users_view.setPadding(40,5,5,5);

      if( block.getPerformers().size() > 0 ){
        for (RPerformer _user: block.getPerformers()){

          RPerformerEntity user = (RPerformerEntity) _user;
          String performerName = "";

          String tempPerformerName =
                  DecisionConverter.getPerformerNameForDecisionPreview(user.getPerformerText(), user.getPerformerGender(), block.getAppealText());

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

          textLabels.add(performer_view);

          users_view.addView(performer_view);
        }
      }


      preview_body.addView( users_view );
    }


    public String getRegNumber() {
      return reg_number;
    }
  }

}
