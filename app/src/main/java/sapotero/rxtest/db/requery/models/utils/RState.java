package sapotero.rxtest.db.requery.models.utils;

// resolved https://tasks.n-core.ru/browse/MVDESD-12618
// Режим замещения

import io.requery.Entity;
import io.requery.Generated;
import io.requery.Key;

// Хранит старое состояние документов, принадлежащих нескольким пользователям, перед сменой режима
@Entity
public abstract class RState {
  @Key
  @Generated
  int id;

  String uid;
  String user;
  String filter;
  String documentType;
  Boolean control;
  Boolean favorites;
  Boolean processed;
  Boolean fromProcessedFolder;
  Boolean fromFavoritesFolder;
  Boolean fromLinks;
  Boolean returned;
  Boolean rejected;
  Boolean again;
  String updatedAt;
}
