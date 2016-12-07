package sapotero.rxtest.views.menu.factories;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.utils.Fields;
import sapotero.rxtest.views.adapters.models.DocumentTypeItem;
import sapotero.rxtest.views.adapters.utils.DocumentTypeAdapter;
import sapotero.rxtest.views.menu.builders.ButtonBuilder;
import sapotero.rxtest.views.menu.builders.ConditionBuilder;
import timber.log.Timber;

public class ItemsFactory {

  private final Context context;
  private ArrayList<Item> items;

  private Callback callback;
  private FrameLayout view;
  private Spinner journalSpinner;
  private String TAG = this.getClass().getSimpleName();
  private DocumentTypeAdapter journalSpinnerAdapter;

  public interface Callback {
    void onMenuUpdate();
  }
  public void registerCallBack(Callback callback){
    this.callback = callback;
  }


  private enum Button {

    PROJECTS ( 1,
      new ButtonBuilder(
        "Проекты %s" ,
        new ConditionBuilder[]{
          new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FILTER.eq("approval")  ),
          new ConditionBuilder( ConditionBuilder.Condition.OR,  RDocumentEntity.FILTER.eq("signing")  ),
        }
      )
    ),
    PERFORMANCE ( 2,
      new ButtonBuilder(
        "На рассмотрение %s" ,
        new ConditionBuilder[]{
          new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FILTER.eq("primary_consideration")  ),
        }
      )
    ),
    PRIMARY_CONSIDERATION ( 3,
      new ButtonBuilder(
        "Первичное рассмотрение %s" ,
        new ConditionBuilder[]{
          new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FILTER.eq("primary_consideration")  ),
        }
      )
    ),
    VIEWED ( 4,
      new ButtonBuilder(
        "Рассмотренные %s" ,
        new ConditionBuilder[]{
          new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FILTER.eq("viewed")  ),
        }
      )
    ),
    ASSIGN ( 5,
      new ButtonBuilder(
        "На подпись %s" ,
        new ConditionBuilder[]{
          new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FILTER.eq("signing")  ),
        }
      )
    ),
    APPROVAL ( 6,
      new ButtonBuilder(
        "На согласование %s" ,
        new ConditionBuilder[]{
          new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FILTER.eq("approval")  ),
        }
      )
    ),
    PROCESSED ( 7,
      new ButtonBuilder(
        "Обработанные %s" ,
        new ConditionBuilder[]{
          new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FILTER.eq("success")  ),
        }
      )
    );

    private final Integer index;
    private final ButtonBuilder button;

    Button( final Integer index, final ButtonBuilder button ) {
      this.index = index;
      this.button = button;
    }

    @Override
    public String toString() {
      return button.getLabel();
    }

    public View getView(Context context) {
      return button.getView(context);
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
      RadioGroup view = new RadioGroup(context);
      view.setOrientation(LinearLayout.HORIZONTAL);
      LinearLayout.LayoutParams params = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT,1.0f );
      view.setLayoutParams(params);

      ArrayList<View> buttonsList = new ArrayList<View>();

      if ( buttons.length > 0 ){

        for (int i = 0, length = buttons.length-1; i <= length; i++) {

          Button item = buttons[i];
          Timber.tag("getButtons").w( "i: %s", i );

          if (i == 0){
            Timber.tag("getButtons").w( "left" );
            item.button.setLeftCorner();
          } else if ( i == length ){
            Timber.tag("getButtons").w( "right" );
            item.button.setRightCorner();
          } else {
            Timber.tag("getButtons").w( "none" );
            item.button.setNoneCorner();
          }


          view.addView( item.getView(context) );

        }

      }

      return view;
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



  public void setSpinner(Spinner selector) {
    journalSpinner = selector;

    journalSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
        Timber.tag(TAG).w( journalSpinnerAdapter.getItem(position).getName() );

        updateView();

        callback.onMenuUpdate();
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


  public void prev() {
    journalSpinner.setSelection( journalSpinnerAdapter.prev() );
  }
  public void next() {
    journalSpinner.setSelection( journalSpinnerAdapter.next() );
  }


  private void buildItems() {
    callback.onMenuUpdate();
  }

  public void updateView() {
    if( view == null ){
      view = new FrameLayout(context);
    }

    view.removeAllViews();
    view.addView( journalSpinnerAdapter.getItem( journalSpinner.getSelectedItemPosition() ).getItem().getButtons(context) );
  }

  public View getView() {
    return view;
  }
}
