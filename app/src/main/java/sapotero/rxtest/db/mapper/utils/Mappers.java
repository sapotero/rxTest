package sapotero.rxtest.db.mapper.utils;

import sapotero.rxtest.db.mapper.DecisionMapper;
import sapotero.rxtest.db.mapper.DocumentMapper;
import sapotero.rxtest.db.mapper.FavoriteUserMapper;
import sapotero.rxtest.db.mapper.PrimaryConsiderationMapper;

// Keeps all mappers in one place
public class Mappers {

  public Mappers() {
  }

  // Каждый раз создается новый экземпляр маппера, чтобы job одного пользователя не смогла поменать логин
  // в маппере другого (вход/выход в режим замещения)

  public DecisionMapper getDecisionMapper() {
    return new DecisionMapper(this);
  }

  public DocumentMapper getDocumentMapper() {
    return new DocumentMapper(this);
  }

  public FavoriteUserMapper getFavoriteUserMapper() {
    return new FavoriteUserMapper(this);
  }

  public PrimaryConsiderationMapper getPrimaryConsiderationMapper() {
    return new PrimaryConsiderationMapper(this);
  }
}
