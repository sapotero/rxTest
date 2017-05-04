package sapotero.rxtest.views.menu.fields;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.utils.Fields;
import sapotero.rxtest.views.menu.builders.ConditionBuilder;

import static sapotero.rxtest.views.menu.fields.V2FilterType.FOR_REPORT;
import static sapotero.rxtest.views.menu.fields.V2FilterType.PRIMARY;
import static sapotero.rxtest.views.menu.fields.V2FilterType.SIGNING;

enum V2FilterType{
  SIGNING    ("signing"),
  APPROVAL   ("approval"),
  FOR_REPORT ("sent_to_the_report"),
  PRIMARY    ("primary_consideration");


  private final String name;

  V2FilterType(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}

public enum MainMenuButton {

  PROJECTS ( 1,
    "Проекты %s" ,
    new ConditionBuilder[]{
      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FILTER.in( ButtonStatus.getProject() )  ),
      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.PROCESSED.eq(false) ),
//      new ConditionBuilder( ConditionBuilder.Condition.OR,  RDocumentEntity.FILTER.eq( Fields.Status.SIGNING.getValue() )  )
    }
  ),
  PERFORMANCE ( 2,
    "На рассмотрение %s" ,
    new ConditionBuilder[]{
      // V3
      // new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FILTER.in( ButtonStatus.getPerformance() )  ),
      //new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FROM_LINKS.eq( false  ) ),
      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.PROCESSED.eq(false) ),
      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FILTER.eq( FOR_REPORT.getName() )  )
    }
  ),
  PRIMARY_CONSIDERATION ( 3,
    "Первичное рассмотрение %s" ,
    new ConditionBuilder[]{
      //V3
      //new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FILTER.in(Arrays.asList(Fields.Status.PRIMARY_CONSIDERATION.getValue()))  ),
      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FILTER.eq( PRIMARY.getName() )  ),
      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.PROCESSED.eq(false) ),
    }
  ),
  VIEWED ( 4,
    "Рассмотренные %s" ,
    new ConditionBuilder[]{
      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.PROCESSED.eq(true) ),
//      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FILTER.ne(Fields.Status.LINK.getValue() ) ),
//      new ConditionBuilder( ConditionBuilder.Condition.OR, RDocumentEntity.FILTER.in(Arrays.asList(Fields.Status.PROCESSED.getValue() ) ) ),
    }
  ),
  ASSIGN ( 5,
    "На подпись %s" ,
    new ConditionBuilder[]{

      //new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FILTER.in(Arrays.asList(Fields.Status.SIGNING.getValue())  ) ),
      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.PROCESSED.eq(false) ),
      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FILTER.eq( SIGNING.getName() )  )

    }
  ),
  APPROVAL ( 6, "На согласование %s" ,
    new ConditionBuilder[]{
      //new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FILTER.in(Arrays.asList(Fields.Status.APPROVAL.getValue()) ) ),
      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.PROCESSED.eq(false) ),
      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FILTER.eq( V2FilterType.APPROVAL.getName() )  )
    }
  ),
  PROCESSED ( 7, "Обработанные %s" ,
    new ConditionBuilder[]{
      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.PROCESSED.eq(true) ),
      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FILTER.in(Arrays.asList( Fields.Status.APPROVAL.getValue(), Fields.Status.SIGNING.getValue() ) ) ),
//      new ConditionBuilder( ConditionBuilder.Condition.OR, RDocumentEntity.FILTER.eq( Fields.Status.PROCESSED.getValue() )  ),
    }
  ),
  FAVORITES ( 8, "Избранное %s" ,
    new ConditionBuilder[]{
      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FAVORITES.eq(true)  ),
      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FROM_LINKS.in(Arrays.asList(null, false))  ),
    }
  ),
  SHARED_PRIMARY ( 9, "Аппараты %s" ,
    new ConditionBuilder[]{
      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FILTER.eq( PRIMARY.getName() )  ),
      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.PROCESSED.eq(false) ),
    }
  );

  private final Integer index;
  public final String format;
  public final ConditionBuilder[] conditions;

  MainMenuButton(final Integer index, final String format, final ConditionBuilder[] conditions) {
    this.index = index;
    this.format = format;
    this.conditions = conditions;
    Holder.MAP.put( String.valueOf(index), this);
  }



  public ConditionBuilder[] getConditions() {
    return conditions;
  }

  public String getFormat() {
    return format;
  }

  public Integer getIndex() {
    return index;
  }

  private static class Holder {
    static Map<String, MainMenuButton> MAP = new HashMap<>();
  }

  public static MainMenuButton getByIndex(int index){

//    if( item == null ) {
//      throw new IllegalStateException(String.format("Unsupported type %s.", index));
//    }

    return Holder.MAP.get( String.valueOf(index) );
  }

  public static class ButtonStatus {
    public static ArrayList<String> getPerformance(){
      ArrayList<String> projectArray = new ArrayList<String>();
      projectArray.add( Fields.Status.SENT_TO_THE_REPORT.getValue() );
//      projectArray.addByOne( Fields.Status.SENT_TO_THE_PERFORMANCE.getValue() );
      return projectArray;
    }
    public static ArrayList<String> getProject(){
      ArrayList<String> projectArray = new ArrayList<String>();
      projectArray.add( Fields.Status.APPROVAL.getValue() );
      projectArray.add( Fields.Status.SIGNING.getValue());
      return projectArray;
    }
    public static ArrayList<String> forAllDocuments(){
      ArrayList<String> projectArray = new ArrayList<String>();
      projectArray.add( Fields.Status.SENT_TO_THE_REPORT.getValue());
      projectArray.add( Fields.Status.PRIMARY_CONSIDERATION.getValue());;
      return projectArray;
    }

  }

}