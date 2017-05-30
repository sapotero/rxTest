package sapotero.rxtest.views.menu.builders;

import android.content.Context;
import android.graphics.Typeface;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.regex.Pattern;

import javax.inject.Inject;

import io.requery.Persistable;
import io.requery.query.Expression;
import io.requery.query.LogicalCondition;
import io.requery.query.WhereAndOr;
import io.requery.rx.RxScalar;
import io.requery.rx.SingleEntityStore;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.utils.Settings;
import sapotero.rxtest.utils.memory.InMemoryDocumentStorage;
import sapotero.rxtest.utils.memory.models.InMemoryDocument;
import sapotero.rxtest.utils.memory.utils.IMDFilter;
import timber.log.Timber;

public class ButtonBuilder {

  @Inject SingleEntityStore<Persistable> dataStore;
  @Inject Settings settings;
//  @Inject Validation validation;
  @Inject InMemoryDocumentStorage store;

  private ConditionBuilder[] conditions;
  private ConditionBuilder[] item_conditions;
  private boolean showDecisionForse;
  private Integer index;
  private String label;
  private boolean active;

  private Callback callback;
  private RadioButton view;

  private final HashMap<String, String> mapper = new HashMap<>();


  private String TAG = this.getClass().getSimpleName();
  private final CompositeSubscription subscription = new CompositeSubscription();

  public void recalculate() {
    getCount();
//    Observable
//      .just("")
//      .debounce(100, TimeUnit.MILLISECONDS)
//      .subscribeOn(Schedulers.newThread())
//      .observeOn(AndroidSchedulers.mainThread())
//      .subscribe(
//        data -> {
//          Timber.tag(TAG).e("recalculate");
//          if (view != null) {
//            getCount();
//          }
//        },
//        Timber::e
//      );
  }

  public interface Callback {
    void onButtonBuilderUpdate(Integer index);
  }
  public void registerCallBack(Callback callback){
    this.callback = callback;
  }


  public Integer getIndex() {
    return index;
  }

  public ButtonBuilder(String label, ConditionBuilder[] conditions, ConditionBuilder[] item_conditions, boolean showDecisionForse, Integer index) {
    this.label = label;
    this.conditions = conditions;
    this.item_conditions = item_conditions;
    this.showDecisionForse = showDecisionForse;
    this.index = index;
    this.active = false;

    EsdApplication.getManagerComponent().inject(this);
  }


  private void getCount() {

    // Отображать документы без резолюции
    if ( settings.isShowWithoutProject() ){
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

    WhereAndOr<RxScalar<Integer>> query = dataStore
      .count(RDocumentEntity.class)
      .where(RDocumentEntity.USER.eq(settings.getLogin()))
      .and(RDocumentEntity.WITH_DECISION.eq(true))
//      .and( RDocumentEntity.DOCUMENT_TYPE.in( validation.getSelectedJournals() ) )
      .and(RDocumentEntity.FROM_LINKS.eq(false));

//    if (index == 4 || index == 7){
//      query = query.and(RDocumentEntity.PROCESSED.eq(true));
//    } else {
//      query = query.and(RDocumentEntity.PROCESSED.eq(false));
//    }

    if (index == 0){
      query = query.or(RDocumentEntity.FROM_PROCESSED_FOLDER.eq(true));
    }

    if ( item_conditions.length > 0 ){

      for (ConditionBuilder condition : item_conditions ){
//        Timber.tag(TAG).i( "I %s", condition.toString() );
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
//        Timber.tag(TAG).i( "C %s", condition.toString() );
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

    view.setText( String.format( label, query.get().value() ) );
  }

  private void getCountWithoutDecisons() {

//    Boolean processed = false;
//
//    WhereAndOr<RxScalar<Integer>> query = dataStore
//      .count(RDocumentEntity.class)
//      .where( RDocumentEntity.USER.eq( settings.getLogin() ) );

    // проекты, подпись, согласование
//    if ( !Arrays.asList(1,5,6,4,7).contains(index) ){
//      List<String> journals = validation.getSelectedJournals();
//      if ( journals.size() > 0){
//        query = query.and( RDocumentEntity.DOCUMENT_TYPE.in( validation.getSelectedJournals() ) );
//      }
//    }

//    // обработанные и Рассмотренные
//    if ( Arrays.asList(4,7).contains(index) ){
//      query = query.and(RDocumentEntity.PROCESSED.eq(true));
//
//      processed = true;
//    } else {
//      query = query.and(RDocumentEntity.PROCESSED.eq(false));
//    }


    ArrayList<ConditionBuilder> _conditions = new ArrayList<>();

    if ( item_conditions.length > 0 ){
      Collections.addAll(_conditions, item_conditions);
    }
    if ( conditions.length > 0 ){
      Collections.addAll(_conditions, conditions);
    }


    IMDFilter filter = new IMDFilter(_conditions);

    Observable
      .from( store.getDocuments().values() )

      .filter( filter::isProcessed )
      .filter( filter::isFavorites )
      .filter( filter::isControl )
      .filter( filter::byType)
      .filter( filter::byStatus)

      .map( InMemoryDocument::getUid )
      .toList()
      .subscribeOn( Schedulers.computation() )
      .observeOn( AndroidSchedulers.mainThread() )
      .subscribe(
        list -> {

          Timber.e( label, list.size() );
          view.setText( String.format( label, list.size() ) );

        },
        Timber::e
      );
  }

  @RequiresApi(api = Build.VERSION_CODES.M)
  public RadioButton getView(Context context){

    Timber.tag(TAG).e("create new");
    view = new RadioButton(context);


    view.setPadding( 32,4,32,4 );

    view.setGravity(Gravity.CENTER);
    view.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16 );
    view.setTypeface( Typeface.create("sans-serif-light", Typeface.NORMAL) );
    view.setTextColor( context.getResources().getColor(R.color.md_grey_600) );


    RadioGroup.LayoutParams params = new RadioGroup.LayoutParams( LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT,10.0f );

    view.setButtonDrawable(new StateListDrawable());
    view.setTextColor( ContextCompat.getColorStateList( context, R.color.text_selector ) );
    view.setLayoutParams( params );
    view.setClickable(true);
    view.setButtonDrawable( ContextCompat.getDrawable(context, R.drawable.toggle_selector_button) );

    view.setBackground( ContextCompat.getDrawable(context, R.drawable.toggle_selector_button) );
    view.setForeground( context.getDrawable(R.drawable.card_foreground) );

    view.setText( String.format( label, 0) );



    // настройка показывать первичное рассмотрение
    if ( settings.isHidePrimaryConsideration() ){
      boolean matches = Pattern.matches("Перви.*", label);
      if (matches){
        view.setVisibility(View.GONE);
      }
    }

//    if (!validation.hasSigningAndApproval() && index == 1){
//      view.setVisibility(View.GONE);
//    }


    getCount();

    view.setOnCheckedChangeListener((buttonView, isChecked) -> {
//      Timber.tag("setOnCheckedChangeListener").i("change");
      setActive(isChecked);

//      getCount();

      if (isChecked){
//        Timber.tag("setOnCheckedChangeListener").i("change");
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
