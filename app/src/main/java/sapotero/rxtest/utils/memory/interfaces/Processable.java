package sapotero.rxtest.utils.memory.interfaces;

import java.util.HashMap;

import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.retrofit.models.documents.Document;
import sapotero.rxtest.utils.memory.fields.DocumentType;
import sapotero.rxtest.utils.memory.utils.Transaction;

public interface Processable {
  void process( RDocumentEntity doc, String filter, String index );
  void process( HashMap<String, Document> docs, String filter, String index, String login, String currentUserId );
  void process( HashMap<String, Document> docs, String folderUid, DocumentType documentType, String login, String currentUserId );
  void process( Transaction transaction  );
}
