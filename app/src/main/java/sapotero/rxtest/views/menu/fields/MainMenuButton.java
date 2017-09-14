package sapotero.rxtest.views.menu.fields;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.utils.V2DocumentType;
import sapotero.rxtest.views.menu.builders.ConditionBuilder;


public enum MainMenuButton {

  PROJECTS ( 1,
    "Проекты %s" ,
    new ConditionBuilder[]{
      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FILTER.in( ButtonStatus.getProject() )  ),
      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.PROCESSED.eq(false) ),
//      new ConditionBuilder( ConditionBuilder.Condition.OR,  RDocumentEntity.FILTER.eq( Fields.Status.SIGNING.getIndex() )  )
    }
  ),
  PERFORMANCE ( 2,
    "На рассмотрение %s" ,
    new ConditionBuilder[]{
      // V3
      // new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FILTER.in( ButtonStatus.getPerformance() )  ),
      //new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FROM_LINKS.eq( false  ) ),
      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.PROCESSED.eq(false) ),
      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FILTER.eq( V2DocumentType.FOR_REPORT.getName() )  )
    }
  ),
  PRIMARY_CONSIDERATION ( 3,
    "Первичное рассмотрение %s" ,
    new ConditionBuilder[]{
      //V3
      //new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FILTER.in(Arrays.asList(Fields.Status.PRIMARY_CONSIDERATION.getIndex()))  ),
      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FILTER.eq( V2DocumentType.PRIMARY.getName() )  ),
      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.PROCESSED.eq(false) ),
    }
  ),
  VIEWED ( 4,
    "Рассмотренные %s" ,
    new ConditionBuilder[]{
      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.PROCESSED.eq(true) ),
//      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FILTER.ne(Fields.Status.LINK.getIndex() ) ),
//      new ConditionBuilder( ConditionBuilder.Condition.OR, RDocumentEntity.FILTER.in(Arrays.asList(Fields.Status.PROCESSED.getIndex() ) ) ),
    }
  ),
  ASSIGN ( 5,
    "На подпись %s" ,
    new ConditionBuilder[]{

      //new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FILTER.in(Arrays.asList(Fields.Status.SIGNING.getIndex())  ) ),
      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.PROCESSED.eq(false) ),
      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FILTER.eq( V2DocumentType.SIGNING.getName() )  )

    }
  ),
  APPROVAL ( 6, "На согласование %s" ,
    new ConditionBuilder[]{
      //new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FILTER.in(Arrays.asList(Fields.Status.APPROVAL.getIndex()) ) ),
      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.PROCESSED.eq(false) ),
      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FILTER.eq( V2DocumentType.APPROVAL.getName() )  )
    }
  ),
  PROCESSED ( 7, "Обработанные %s" ,
    new ConditionBuilder[]{
      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.PROCESSED.eq(true) ),
      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FILTER.in(Arrays.asList( V2DocumentType.APPROVAL.getName(), V2DocumentType.SIGNING.getName() ) ) ),
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
      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FILTER.eq( V2DocumentType.PRIMARY.getName() )  ),
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
      projectArray.add( V2DocumentType.FOR_REPORT.getName() );
//      projectArray.addByOne( Fields.Status.SENT_TO_THE_PERFORMANCE.getIndex() );
      return projectArray;
    }
    public static ArrayList<String> getProject(){
      ArrayList<String> projectArray = new ArrayList<String>();
      projectArray.add( V2DocumentType.APPROVAL.getName() );
      projectArray.add( V2DocumentType.SIGNING.getName());
      return projectArray;
    }
    public static ArrayList<String> forAllDocuments(){
      ArrayList<String> projectArray = new ArrayList<String>();
      projectArray.add( V2DocumentType.FOR_REPORT.getName());
      projectArray.add( V2DocumentType.PRIMARY.getName());;
      return projectArray;
    }

  }

}