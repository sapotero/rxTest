package sapotero.rxtest.views.menu.builders;

import android.content.Context;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.f2prateek.rx.preferences.RxSharedPreferences;

import java.util.ArrayList;
import java.util.regex.Pattern;

import javax.inject.Inject;

import io.requery.Persistable;
import io.requery.query.Expression;
import io.requery.query.LogicalCondition;
import io.requery.query.Scalar;
import io.requery.query.Tuple;
import io.requery.query.WhereAndOr;
import io.requery.rx.SingleEntityStore;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.models.decisions.RDecisionEntity;
import timber.log.Timber;

public class ButtonBuilder {

  @Inject SingleEntityStore<Persistable> dataStore;
  @Inject RxSharedPreferences settings;

  private ConditionBuilder[] conditions;
  private ConditionBuilder[] item_conditions;
  private boolean showDecisionForse;
  private Integer index;
  private String label;
  private boolean active;
  private Corner corner;

  private Callback callback;
  private RadioButton view;


  private String TAG = this.getClass().getSimpleName();
  private final CompositeSubscription subscription = new CompositeSubscription();

  public void recalculate() {
    Timber.tag(TAG).e("recalculate");
    if (view != null) {
//      view.setText( getLabel() );
      getCount();
    }
  }

  public interface Callback {
    void onButtonBuilderUpdate(Integer index);
  }
  public void registerCallBack(Callback callback){
    this.callback = callback;
  }


  enum Corner{
    LEFT,
    RIGHT,
    NONE
  }

  public ButtonBuilder(String label, ConditionBuilder[] conditions, ConditionBuilder[] item_conditions, boolean showDecisionForse, Integer index) {
    this.label = label;
    this.conditions = conditions;
    this.item_conditions = item_conditions;
    this.showDecisionForse = showDecisionForse;
    this.index = index;
    this.corner = Corner.NONE;
    this.active = false;

    EsdApplication.getComponent( EsdApplication.getContext() ).inject(this);
  }


  private void getCount() {

    if ( settings.getBoolean("settings_view_type_show_without_project").get() ){
      getCountWithoutDecisons();
    } else {
      // для некоторых журналов показываем всё независимо от настроек
      if (showDecisionForse){
        getCountWithoutDecisons();
      } else {
        getCountWithDecisons();
      }
    }

  }

  private void getCountWithDecisons() {

    LogicalCondition<? extends Expression<?>, ?> query_condition;

    unsubscribe();
    subscription.add(

      dataStore
        .select(RDecisionEntity.DOCUMENT_ID)
        .distinct()
        .get()
        .toObservable()
        .subscribeOn(Schedulers.computation())
        .observeOn(AndroidSchedulers.mainThread())
        .toList()
        .subscribe( data -> {

            // FIX медленно, но работает, оставить если согласуют
            ArrayList<Integer> result = new ArrayList<>();
            for ( Tuple item: data) {
            result.add( item.get(0) );
          }
            Timber.tag(TAG).w("TOTAL: %s", result.size());

            WhereAndOr<Scalar<Integer>> querys = dataStore
              .count(RDocumentEntity.class )
              .where( RDocumentEntity.USER.eq( settings.getString("login").get() ) );

          if ( item_conditions.length > 0 ){

              for (ConditionBuilder condition : item_conditions ){
                switch ( condition.getCondition() ){
                  case AND:
                    querys = querys.and( condition.getField() );
                    break;
                  case OR:
                    querys = querys.or( condition.getField() );
                    break;
                  default:
                    break;
                }
              }
            }
            if ( conditions.length > 0 ){

              for (ConditionBuilder condition : conditions ){
                switch ( condition.getCondition() ){
                  case AND:
                    querys = querys.and( condition.getField() );
                    break;
                  case OR:
                    querys = querys.or( condition.getField() );
                    break;
                  default:
                    break;
                }
              }
            }


            Integer docs_count = querys
              .and(RDocumentEntity.ID.in(result))
              .get()
              .value();

          view.setText( String.format( label, docs_count ) );

        },
          error -> {
            Timber.e(error);
          }
        )

    );
  }

  private void getCountWithoutDecisons() {

    WhereAndOr<Scalar<Integer>> query = dataStore
      .count(RDocumentEntity.class)
      .where( RDocumentEntity.USER.eq( settings.getString("login").get() ) );

    ArrayList<ConditionBuilder> temp_conditions = new ArrayList<>();

    if ( item_conditions.length > 0 ){

      for (ConditionBuilder condition : item_conditions ){
        temp_conditions.add(condition);

//        Timber.tag("item_conditions").v("%s %s %s | %s"
//          , condition.getField().getLeftOperand()
//          , condition.getField().getOperator()
//          , condition.getField().getRightOperand()
//          , condition.getCondition());
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
    if ( conditions.length > 0 ){
      for (ConditionBuilder condition : conditions ){
        temp_conditions.add(condition);

//        Timber.tag("conditions").v("%s %s %s | %s"
//          , condition.getField().getLeftOperand()
//          , condition.getField().getOperator()
//          , condition.getField().getRightOperand()
//          , condition.getCondition());
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

    if ( temp_conditions.size() > 0 ) {
      for (ConditionBuilder condition : temp_conditions) {
        Timber.tag("temp_conditions").v("%s %s %s | %s"
          , condition.getField().getLeftOperand()
          , condition.getField().getOperator()
          , condition.getField().getRightOperand()
          , condition.getCondition());
      }
    }

    Integer count = dataStore
      .count(RDocumentEntity.class)
      .where(RDocumentEntity.USER.eq(settings.getString("login").get()))
      .and(RDocumentEntity.UID.like("01%"))
      .and(RDocumentEntity.FILTER.eq("sent_to_the_report"))
      .or( RDocumentEntity.FILTER.eq("sent_to_the_performance"))
      .get().value();

    Timber.w("MANUAL COUNT: %s", count);

    view.setText( String.format( label, query.get().value() ) );
  }

//  public String getLabel() {
//    return String.format( label, getCount() );
//  }

  @RequiresApi(api = Build.VERSION_CODES.M)
  public RadioButton getView(Context context){

    Timber.tag(TAG).e("create new");
    view = new RadioButton(context);


    view.setPadding( 32,4,32,4 );

    view.setGravity(Gravity.CENTER);
    view.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12 );


    RadioGroup.LayoutParams params = new RadioGroup.LayoutParams( LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT,10.0f );

//    switch (corner){
//      case LEFT:
//        view.setBackgroundResource( R.drawable.button_corner_left );
//        params.setMargins(4,4,0,4);
//        break;
//      case RIGHT:
//        view.setBackgroundResource( R.drawable.button_corner_right );
//        params.setMargins(0,4,4,4);
//        break;
//      case NONE:
//        view.setBackgroundResource( R.drawable.button_corner_none );
//        params.setMargins(0,4,0,4);
//        break;
//    }

    view.setButtonDrawable(new StateListDrawable());
    view.setTextColor( ContextCompat.getColorStateList( context, R.color.text_selector ) );
    view.setLayoutParams( params );
    view.setButtonDrawable( ContextCompat.getDrawable(context, R.drawable.toggle_selector_button) );
    view.setTextColor( context.getResources().getColor(R.color.md_grey_600) );

    view.setForeground( context.getDrawable(R.drawable.card_foreground) );
    view.setBackground( ContextCompat.getDrawable(context, R.drawable.toggle_selector_button) );

    view.setText( String.format( label, 0) );

    // настройка показывать первичное рассмотрение
    if ( settings.getBoolean("settings_view_hide_primary_consideration").get() ){
      boolean matches = Pattern.matches("Перви.*", label);
      if (matches){
        view.setVisibility(View.GONE);
      }
    }


    getCount();

    view.setOnCheckedChangeListener((buttonView, isChecked) -> {
      Timber.tag("setOnCheckedChangeListener").i("change");
      setActive(isChecked);

//      getCount();

      if (isChecked){
        Timber.tag("setOnCheckedChangeListener").i("change");
        callback.onButtonBuilderUpdate(index);
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

  private void unsubscribe(){
    if ( subscription != null && subscription.hasSubscriptions() ){
      subscription.clear();
    }
//    subscription = new CompositeSubscription();
  }
}
