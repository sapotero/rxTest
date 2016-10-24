package sapotero.rxtest.db.Storio.resolvers;

import android.support.annotation.NonNull;

import com.pushtorefresh.storio.sqlite.StorIOSQLite;
import com.pushtorefresh.storio.sqlite.operations.put.PutResolver;
import com.pushtorefresh.storio.sqlite.operations.put.PutResult;
import com.pushtorefresh.storio.sqlite.operations.put.PutResults;

import java.util.HashSet;
import java.util.Set;

import sapotero.rxtest.db.Storio.entities.DocWithSigner;
import sapotero.rxtest.db.Storio.tables.DocTable;
import sapotero.rxtest.db.Storio.tables.SignerTable;

import static java.util.Arrays.asList;

public class DocWithSignerPutResolver extends PutResolver<DocWithSigner> {

  @NonNull
  @Override
  public PutResult performPut(@NonNull StorIOSQLite storIOSQLite, @NonNull DocWithSigner docWithSigner) {

    final PutResults<Object> putResults = storIOSQLite
      .put()
      .objects(asList(docWithSigner.doc(), docWithSigner.signer()))
      .prepare()
      .executeAsBlocking();

    final Set<String> affectedTables = new HashSet<String>(2);

    affectedTables.add(DocTable.TABLE);
    affectedTables.add(SignerTable.TABLE);

    return PutResult.newUpdateResult(putResults.numberOfUpdates(), affectedTables);
  }

}