package sapotero.rxtest.views.menu.fields;

import java.util.ArrayList;

import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.utils.Fields;
import sapotero.rxtest.views.menu.builders.ButtonBuilder;
import sapotero.rxtest.views.menu.builders.ConditionBuilder;

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
    },
    new ConditionBuilder[]{}
  ),

  INCOMING_DOCUMENTS ( 1, "Входящие документы %s", new Button[]{
    Button.PERFORMANCE,
    Button.PRIMARY_CONSIDERATION,
    Button.VIEWED
  },
    true,
    new ConditionBuilder[]{
      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.UID.like( "%" + Fields.Journal.INCOMING_DOCUMENTS.getValue() ) )
    },
    new ConditionBuilder[]{}
  ),

  CITIZEN_REQUESTS ( 2, "Обращения граждан %s", new Button[]{
    Button.PERFORMANCE,
    Button.PRIMARY_CONSIDERATION,
    Button.VIEWED
  },
    true,
    new ConditionBuilder[]{
      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.UID.like( "%" + Fields.Journal.CITIZEN_REQUESTS.getValue() ) )
    },
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
    },
    new ConditionBuilder[]{}
  ),

  INCOMING_ORDERS ( 4, "НПА %s", new Button[]{
    Button.PERFORMANCE,
    Button.PRIMARY_CONSIDERATION,
    Button.VIEWED
  },true,
    new ConditionBuilder[]{
      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.UID.like( "%" + Fields.Journal.INCOMING_ORDERS.getValue() ) )
    },
    new ConditionBuilder[]{}
  ),

  ORDERS ( 5, "Приказы %s", new Button[]{
    Button.PERFORMANCE,
    Button.PRIMARY_CONSIDERATION,
    Button.VIEWED
  },true,
    new ConditionBuilder[]{
      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.UID.like( "%" + Fields.Journal.ORDERS.getValue() ) )
    },
    new ConditionBuilder[]{}
  ),

  ORDERS_DDO ( 6, "Приказы ДДО %s", new Button[]{
    Button.PERFORMANCE,
    Button.PRIMARY_CONSIDERATION,
    Button.VIEWED
  },true,
    new ConditionBuilder[]{
      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.UID.like( "%" + Fields.Journal.ORDERS_DDO.getValue() ) )
    },
    new ConditionBuilder[]{}
  ),

  IN_DOCUMENTS ( 7, "Внутренние документ %s", new Button[]{
    Button.PERFORMANCE,
    Button.PRIMARY_CONSIDERATION,
    Button.VIEWED
  },
    true,
    new ConditionBuilder[]{
      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.UID.like( "%" + Fields.Journal.ORDERS.getValue() ) )
    },
    new ConditionBuilder[]{}
  ),

  ON_CONTROL ( 8, "На контроле %s", new Button[]{},
    false,
    new ConditionBuilder[]{
      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.CONTROL.eq( true ) )
    },
    new ConditionBuilder[]{}
  );

  private final Integer index;
  private final Button[] buttons;
  private final String name;
  private final Boolean showOrganization;
  private ConditionBuilder[] countConditions;
  private ConditionBuilder[] queryConditions;
  private final ArrayList<ButtonBuilder> buttonsList = new ArrayList<>();

  Item(final int index, final String name, final Button[] buttons, Boolean showOrganizations, ConditionBuilder[] countCounditions, ConditionBuilder[] queryConditions) {
    this.index = index;
    this.name  = name;
    this.buttons = buttons;
    this.showOrganization = showOrganizations;
    this.countConditions = countCounditions;
    this.queryConditions = queryConditions;
  }

  public Integer getIndex(){
    return index;
  }

  public ConditionBuilder[] getCountConditions(){
    return countConditions;
  }

  public ConditionBuilder[] getQueryConditions(){
    return queryConditions;
  }

  public String getName(){
    return name;
  }

  public ArrayList<ButtonBuilder> getButtons(){

    if ( buttonsList.size() == 0 ){
      if ( buttons.length > 0 ){
        for (int i = 0, length = buttons.length-1; i <= length; i++) {

          ButtonBuilder button = new ButtonBuilder(
            buttons[i].getFormat(),
            buttons[i].getConditions()
          );

          if (i == 0){
            button.setLeftCorner();
          } else if ( i == length ){
            button.setRightCorner();
          } else {
            button.setNoneCorner();
          }

          buttonsList.add( button );
        }
      }
    }

    return buttonsList;
  }

  public Boolean isVisible(){
    return showOrganization;
  }

  @Override
  public String toString() {
    return name;
  }

}

