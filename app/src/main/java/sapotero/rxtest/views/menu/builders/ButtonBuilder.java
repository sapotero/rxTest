package sapotero.rxtest.views.menu.builders;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.StateListDrawable;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.inject.Inject;

import io.requery.Persistable;
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
import sapotero.rxtest.utils.memory.MemoryStore;
import sapotero.rxtest.utils.memory.models.Counter;
import sapotero.rxtest.utils.memory.models.InMemoryDocument;
import sapotero.rxtest.utils.memory.utils.Filter;
import timber.log.Timber;

public class ButtonBuilder {

  @Inject SingleEntityStore<Persistable> dataStore;
  @Inject Settings settings;
  //  @Inject Validation validation;
  @Inject MemoryStore store;

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
    Timber.i("recalculate");
    getCount();
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

    Timber.i("getCount");
    // Отображать документы без резолюции
    if ( settings.isShowWithoutProject() ){
      Timber.i("isShowWithoutProject");
      getCountWithoutDecisons();
    } else {
      // для некоторых журналов показываем всё независимо от настроек
      if (showDecisionForse){
        Timber.i("showDecisionForse");
        getCountWithoutDecisons();
      } else {
        Timber.i("getCountWithDecisons");
        getCountWithDecisons();
      }
    }

  }

  private void getCountWithDecisons() {

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

    ArrayList<ConditionBuilder> _conditions = new ArrayList<>();

    if ( item_conditions.length > 0 ){
      Collections.addAll(_conditions, item_conditions);
    }
    if ( conditions.length > 0 ){
      Collections.addAll(_conditions, conditions);
    }


    Filter filter = new Filter(_conditions);

    ArrayList<String> types    = filter.getTypes();
    ArrayList<String> statuses = filter.getStatuses();

    Timber.i( "type: %s, statuses: %s , processed: %s", new Gson().toJson(types),  new Gson().toJson(statuses), filter.getProcessed() );

    Counter counter = store.getCounter();

    Counter.Status status = null;
    if (statuses.size() > 0){
      status = Counter.Status.getStatus(filter.getStatuses().get(0));
    }

    Counter.Document type = null;

    if (types.size() > 0){
      type = Counter.Document.getType(types.get(0));
    }
    if ( filter.getProcessed() ){
      type = Counter.Document.PROCESSED;
    }


    Timber.w("COUNTER: %s %s", status, type );

    try {
      if (type != null && status != null) {

        Map<Counter.Document, Integer> docs = counter.getData().get(status);
        if (docs != null && docs.containsKey(type)){
          Timber.w("FROM COUNTER: %s", docs.get(type) );
        }
      }
    } catch (Exception e) {
      Timber.e(e);
    }


    Observable
      .from( store.getDocuments().values() )

      .filter( filter::byType)
      .filter( filter::byStatus)
      .filter( filter::isProcessed )
      .filter( filter::isFavorites )
      .filter( filter::isControl )

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
//    view.setForeground( ContextCompat.getDrawable(context, R.drawable.card_foreground) );

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
