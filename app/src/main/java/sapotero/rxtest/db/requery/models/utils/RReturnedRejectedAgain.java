package sapotero.rxtest.db.requery.models.utils;

import io.requery.Entity;
import io.requery.Generated;
import io.requery.Key;
import sapotero.rxtest.db.requery.models.utils.enums.DocumentCondition;

// resolved https://tasks.n-core.ru/browse/MVDESD-13213
// Плашки "Возвращено" и "Повторно"

// Хранит состояние документа после выполнения операции для корректного проставления плашки
// в случае, если документ будет снова направлен пользователю.
@Entity
public class RReturnedRejectedAgain {
  @Key
  @Generated
  int id;

  String documentUid;
  String user;
  DocumentCondition documentCondition;
}
