package sapotero.rxtest.views.menu.builders;

import android.content.Context;
import android.graphics.drawable.StateListDrawable;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.f2prateek.rx.preferences.RxSharedPreferences;

import javax.inject.Inject;

import io.requery.Persistable;
import io.requery.query.Expression;
import io.requery.query.LogicalCondition;
import io.requery.query.Scalar;
import io.requery.query.WhereAndOr;
import io.requery.rx.SingleEntityStore;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import timber.log.Timber;

public class ButtonBuilder {

  @Inject SingleEntityStore<Persistable> dataStore;
  @Inject RxSharedPreferences settings;

  private ConditionBuilder[] conditions;
  private ConditionBuilder item_conditions;
  private String label;
  private boolean active;
  private Corner corner;

  private Callback callback;
  private RadioButton view;


  private String TAG = this.getClass().getSimpleName();

  public void recalculate() {
    Timber.tag("recalculate");
    if (view != null) {
      view.setText( getLabel() );
    }
  }

  public interface Callback {
    void onButtonBuilderUpdate();
  }
  public void registerCallBack(Callback callback){
    this.callback = callback;
  }


  enum Corner{
    LEFT,
    RIGHT,
    NONE
  }

  public ButtonBuilder(String label, ConditionBuilder[] conditions, ConditionBuilder item_conditions ) {
    this.label = label;
    this.conditions = conditions;
    this.item_conditions = item_conditions;
    this.corner = Corner.NONE;
    this.active = false;

    EsdApplication.getComponent( EsdApplication.getContext() ).inject(this);
  }


  private Integer getCount() {
    int count = 0;

    LogicalCondition<? extends Expression<?>, ?> query_condition;

    if ( item_conditions == null ){
      query_condition = RDocumentEntity.UID.ne("");
    } else {
      query_condition = item_conditions.getField();
    }

    WhereAndOr<Scalar<Integer>> query = dataStore
      .count(RDocumentEntity.class)
      .where( query_condition )
      .and(RDocumentEntity.USER.eq( settings.getString("login").get() ));

    if ( conditions.length > 0 ){

      for (ConditionBuilder condition : conditions ){
        switch ( condition.getCondition() ){
          case AND:
            query = query.and( condition.getField() );
            break;
          case OR:
            query = query.or( condition.getField() );
            break;
          default:
            break;
        }
      }
    }

    count = query.get().value();
    return count;
  }

  public String getLabel() {
    return String.format( label, getCount() );
  }

  public RadioButton getView(Context context){

    Timber.tag(TAG).e("create new");
    view = new RadioButton(context);

    view.setPadding( 32,4,32,4 );
    view.setGravity(Gravity.CENTER);
    view.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12 );


    RadioGroup.LayoutParams params = new RadioGroup.LayoutParams( LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT,10.0f );

    switch (corner){
      case LEFT:
        view.setBackgroundResource( R.drawable.button_corner_left );
        params.setMargins(4,4,0,4);
        break;
      case RIGHT:
        view.setBackgroundResource( R.drawable.button_corner_right );
        params.setMargins(0,4,4,4);
        break;
      case NONE:
        view.setBackgroundResource( R.drawable.button_corner_none );
        params.setMargins(0,4,0,4);
        break;
    }

    view.setButtonDrawable(new StateListDrawable());
    view.setTextColor( ContextCompat.getColorStateList( context, R.color.text_selector ) );
    view.setLayoutParams( params );
    view.setText( getLabel() );

    view.setOnCheckedChangeListener((buttonView, isChecked) -> {
      setActive(isChecked);

      view.setText( getLabel() );

      if (isChecked){
        Timber.tag("setOnCheckedChangeListener").i("change");
        callback.onButtonBuilderUpdate();
      }
    });

//    view.setForeground(context.getDrawable(R.drawable.ripple));

    return view;
  }

  public ConditionBuilder[] getConditions() {
    return conditions;
  }

  public RadioButton getButton(){
    return view;
  }

  public void setLeftCorner() {
    corner = Corner.LEFT;
  }
  public void setRightCorner() {
    corner = Corner.RIGHT;
  }
  public void setNoneCorner() {
    corner = Corner.NONE;
  }

  public boolean isActive() {
    return active;
  }

  private void setActive(boolean active) {
    this.active = active;
  }
}
