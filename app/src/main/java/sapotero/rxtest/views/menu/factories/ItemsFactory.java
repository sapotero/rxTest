package sapotero.rxtest.views.menu.factories;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;

import org.honorato.multistatetogglebutton.MultiStateToggleButton;

import java.util.ArrayList;
import java.util.List;

import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.utils.Fields;
import sapotero.rxtest.views.adapters.models.DocumentTypeItem;
import sapotero.rxtest.views.adapters.utils.DocumentTypeAdapter;
import sapotero.rxtest.views.menu.builders.ConditionBuilder;
import timber.log.Timber;

public class ItemsFactory {

  private final Context context;
  private ArrayList<Item> items;

  private Callback callback;
  private ViewGroup view;
  private Spinner journalSpinner;
  private String TAG = this.getClass().getSimpleName();
  private DocumentTypeAdapter journalSpinnerAdapter;
//  private Condition<V, ?> query;

  public interface Callback {
    void onMenuUpdate();
  }

  public void registerCallBack(Callback callback){
    this.callback = callback;
  }




  public void setSpinner(Spinner selector) {
    journalSpinner = selector;

    journalSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
        Timber.tag(TAG).w( journalSpinnerAdapter.getItem(position).getName() );
      }

      @Override
      public void onNothingSelected(AdapterView<?> adapterView) {
      }
    });
  }

  public void setSpinnerDefaults() {

    List<DocumentTypeItem> document_types = new ArrayList<>();

    for ( Item item : Item.values()) {
      document_types.add( new DocumentTypeItem( context, item ) );
    }

    journalSpinnerAdapter = new DocumentTypeAdapter(context, document_types);
    journalSpinner.setAdapter(journalSpinnerAdapter);
  }

  private enum Button {

    PROJECTS              ( "Проекты %s"                , 0 ),
    PERFORMANCE           ( "На рассмотрение %s"        , 0 ),
    PRIMARY_CONSIDERATION ( "Первичное рассмотрение %s" , 0 ),
    VIEWED                ( "Рассмотренные %s"          , 0 ),
    ASSIGN                ( "На подпись %s"             , 0 ),
    APPROVAL              ( "На согласование %s"        , 0 ),
    PROCESSED             ( "Обпаботанные %s"           , 0 );

    private final String text;
    private final Integer count;

    Button( final String text, final Integer count ) {
      this.text  = text;
      this.count = count;
    }

    @Override
    public String toString() {
      return String.format( text, count );
    }

    public View getView(Context context) {
      return null;
    }
  }

  public enum Item {

    ALL ( 0, "Документы %s / Проекты %s",
      new Button[]{
        Button.PROJECTS,
        Button.PERFORMANCE,
        Button.PRIMARY_CONSIDERATION,
        Button.VIEWED
      },
      true,
      new ConditionBuilder[]{
        new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FILTER.eq("approval")  ),
      }
    ),

    INCOMING_DOCUMENTS ( 1, "Входящие документы %s", new Button[]{
      Button.PERFORMANCE,
      Button.PRIMARY_CONSIDERATION,
      Button.VIEWED
    },
      true,
      new ConditionBuilder[]{}
    ),

    CITIZEN_REQUESTS ( 2, "Обращения граждан %s", new Button[]{
      Button.PERFORMANCE,
      Button.PRIMARY_CONSIDERATION,
      Button.VIEWED
    },
      true,
      new ConditionBuilder[]{}
    ),

    APPROVE_ASSIGN ( 3, "Подписание/Согласование %s",
      new Button[]{
        Button.APPROVAL,
        Button.ASSIGN,
        Button.PROCESSED
      },
      true,
      new ConditionBuilder[]{
        new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FILTER.eq( Fields.Status.SIGNING.getValue()  ) ),
        new ConditionBuilder( ConditionBuilder.Condition.OR,  RDocumentEntity.FILTER.eq( Fields.Status.APPROVAL.getValue() ) )
      }
    ),

    INCOMING_ORDERS ( 4, "НПА %s", new Button[]{
      Button.PERFORMANCE,
      Button.PRIMARY_CONSIDERATION,
      Button.VIEWED
    },true,
      new ConditionBuilder[]{}
    ),

    ORDERS ( 5, "Приказы %s", new Button[]{
      Button.PERFORMANCE,
      Button.PRIMARY_CONSIDERATION,
      Button.VIEWED
    },true,
      new ConditionBuilder[]{}
    ),

    ORDERS_DDO ( 6, "Приказы ДДО %s", new Button[]{
      Button.PERFORMANCE,
      Button.PRIMARY_CONSIDERATION,
      Button.VIEWED
    },true,
      new ConditionBuilder[]{}
    ),

    IN_DOCUMENTS ( 7, "Внутренние документ %s", new Button[]{
      Button.PERFORMANCE,
      Button.PRIMARY_CONSIDERATION,
      Button.VIEWED
    },
      true,
      new ConditionBuilder[]{}
    ),

    ON_CONTROL ( 8, "На контроле %s", new Button[]{},
      false,
      new ConditionBuilder[]{}
    );

    private final Integer index;
    private final Button[] buttons;
    private final String name;
    private final Boolean showOrganization;
    private ConditionBuilder[]  conditions;

    Item(final int index, final String name, final Button[] buttons, Boolean showOrganizations, ConditionBuilder[] conditions) {
      this.index = index;
      this.name  = name;
      this.buttons = buttons;
      this.showOrganization = showOrganizations;
      this.conditions = conditions;
    }

    public Integer getIndex(){
      return index;
    }

    public ConditionBuilder[] getConditions(){
      return conditions;
    }

    public String getName(){
      return name;
    }

    public View getButtons( Context context ){
      MultiStateToggleButton buttons = new MultiStateToggleButton(context);

      if ( this.buttons.length > 0 ){

        for(Button item: this.buttons){
          buttons.addView( item.getView(context) );
        }
      }

      return buttons;
    }

    public Boolean getShowOrganization(){
      return showOrganization;
    }

    @Override
    public String toString() {
      return name;
    }

  }


  public ItemsFactory(Context context) {
    this.context = context;
  }

  private void buildItems() {

    callback.onMenuUpdate();
  }

  public View getView() {
    return view;
  }
}
