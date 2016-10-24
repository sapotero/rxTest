package sapotero.rxtest.db.Storio.resolvers;

import android.database.Cursor;
import android.support.annotation.NonNull;

import com.pushtorefresh.storio.sqlite.operations.get.DefaultGetResolver;

import sapotero.rxtest.db.Storio.entities.Doc;
import sapotero.rxtest.db.Storio.entities.DocWithSigner;
import sapotero.rxtest.db.Storio.entities.Signer;
import sapotero.rxtest.db.Storio.relations.DocRelations;

public class DocWithSignerGetResolver extends DefaultGetResolver<DocWithSigner> {

  @NonNull
  @Override
  public DocWithSigner mapFromCursor(@NonNull Cursor cursor) {
    final Doc tweet = Doc.add(
      cursor.getLong(  cursor.getColumnIndexOrThrow( DocRelations.QUERY_COLUMN_DOCUMENT_ID)),
      cursor.getString(cursor.getColumnIndexOrThrow( DocRelations.QUERY_COLUMN_DOCUMENT_TITLE)),
      cursor.getString(cursor.getColumnIndexOrThrow( "")),
      cursor.getString(cursor.getColumnIndexOrThrow( DocRelations.QUERY_COLUMN_DOCUMENT_REGISTRATION_NUMBER)),
      cursor.getString(cursor.getColumnIndexOrThrow( DocRelations.QUERY_COLUMN_DOCUMENT_REGISTRATION_DATE)),
      cursor.getString(cursor.getColumnIndexOrThrow( DocRelations.QUERY_COLUMN_DOCUMENT_EXTERNAL_DOCUMENT_NUMBER))
    );

    final Signer user = Signer.add(
      cursor.getLong(  cursor.getColumnIndexOrThrow( DocRelations.QUERY_COLUMN_SIGNER_ID)),
      cursor.getString(cursor.getColumnIndexOrThrow( DocRelations.QUERY_COLUMN_SIGNER_NAME)),
      cursor.getString(cursor.getColumnIndexOrThrow( DocRelations.QUERY_COLUMN_SIGNER_ORGANISATION)),
      cursor.getString(cursor.getColumnIndexOrThrow( DocRelations.QUERY_COLUMN_SIGNER_TYPE))
    );

    return new DocWithSigner(tweet, user);
  }
}
