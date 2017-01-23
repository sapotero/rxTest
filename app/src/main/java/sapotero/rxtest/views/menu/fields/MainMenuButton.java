package sapotero.rxtest.views.menu.fields;

import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.utils.Fields;
import sapotero.rxtest.views.menu.builders.ConditionBuilder;

public enum MainMenuButton {

  PROJECTS ( 1,
    "Проекты %s" ,
    new ConditionBuilder[]{
      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FILTER.eq( Fields.Status.APPROVAL.getValue() )  ),
      new ConditionBuilder( ConditionBuilder.Condition.OR,  RDocumentEntity.FILTER.eq( Fields.Status.SIGNING.getValue() )  )
    }
  ),
  PERFORMANCE ( 2,
    "На рассмотрение %s" ,
    new ConditionBuilder[]{
      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FILTER.eq(Fields.Status.SENT_TO_THE_REPORT.getValue())  ),
      new ConditionBuilder( ConditionBuilder.Condition.OR, RDocumentEntity.FILTER.eq(Fields.Status.PRIMARY_CONSIDERATION.getValue())  ),
    }
  ),
  PRIMARY_CONSIDERATION ( 3,
    "Первичное рассмотрение %s" ,
    new ConditionBuilder[]{
      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FILTER.eq(Fields.Status.PRIMARY_CONSIDERATION.getValue())  ),
    }
  ),
  VIEWED ( 4,
    "Рассмотренные %s" ,
    new ConditionBuilder[]{
      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.PROCESSED.eq(true)  ),
    }
  ),
  ASSIGN ( 5,
    "На подпись %s" ,
    new ConditionBuilder[]{
      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FILTER.eq(Fields.Status.SIGNING.getValue())  ),
    }
  ),
  APPROVAL ( 6, "На согласование %s" ,
    new ConditionBuilder[]{
      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FILTER.eq(Fields.Status.APPROVAL.getValue())  ),
    }
  ),
  PROCESSED ( 7, "Обработанные %s" ,
    new ConditionBuilder[]{
      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.PROCESSED.eq(true)  ),
    }
  ),
  FAVORITES ( 8, "Избранное %s" ,
    new ConditionBuilder[]{
      new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FAVORITES.eq(true)  ),
    }
  );

  private final Integer index;
  public final String format;
  public final ConditionBuilder[] conditions;

  public Boolean active;

  MainMenuButton(final Integer index, final String format, final ConditionBuilder[] conditions ) {
    this.index = index;
    this.format = format;
    this.conditions = conditions;
  }

  public ConditionBuilder[] getConditions() {
    return conditions;
  }

  public String getFormat() {
    return format;
  }
}