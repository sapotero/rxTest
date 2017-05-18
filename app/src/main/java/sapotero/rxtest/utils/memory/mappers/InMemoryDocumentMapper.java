package sapotero.rxtest.utils.memory.mappers;

import sapotero.rxtest.retrofit.models.documents.Document;
import sapotero.rxtest.utils.memory.models.InMemoryDocument;

public class InMemoryDocumentMapper {

  public static InMemoryDocument toMemoryModel(Document document) {
    InMemoryDocument imd = new InMemoryDocument();
    imd.setUid( document.getUid() );
    imd.setMd5( document.getMd5() );
    imd.setAsNew();

    return imd;
  }
}
