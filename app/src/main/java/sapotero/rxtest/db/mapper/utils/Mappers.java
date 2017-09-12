package sapotero.rxtest.db.mapper.utils;

import sapotero.rxtest.db.mapper.DocumentMapper;

// Keeps all mappers in one place
public class Mappers {

  public Mappers() {
  }

  // Каждый раз создается новый экземпляр маппера, чтобы job одного пользователя не смогла поменать логин
  // в маппере другого (вход/выход в режим замещения)

  public DocumentMapper getDocumentMapper() {
    return new DocumentMapper(this);
  }
}
