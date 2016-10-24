package sapotero.rxtest.db.Storio.resolvers;

import android.support.annotation.NonNull;

import com.pushtorefresh.storio.sqlite.StorIOSQLite;
import com.pushtorefresh.storio.sqlite.operations.delete.DeleteResolver;
import com.pushtorefresh.storio.sqlite.operations.delete.DeleteResult;

import java.util.HashSet;
import java.util.Set;

import sapotero.rxtest.db.Storio.entities.DocWithSigner;
import sapotero.rxtest.db.Storio.tables.DocTable;
import sapotero.rxtest.db.Storio.tables.SignerTable;
import static java.util.Arrays.asList;

public class DocWithSignerDeleteResolver  extends DeleteResolver<DocWithSigner> {
  @NonNull
  @Override
  public DeleteResult performDelete(@NonNull StorIOSQLite storIOSQLite, @NonNull DocWithSigner docWithSigner) {

    storIOSQLite
      .delete()
      .objects(
        asList(
          docWithSigner.doc(),
          docWithSigner.signer()
        )
      )
      .prepare()
      .executeAsBlocking();

    final Set<String> affectedTables = new HashSet<String>(2);

    affectedTables.add(SignerTable.TABLE);
    affectedTables.add(DocTable.TABLE);

    return DeleteResult.newInstance(2, affectedTables);
  }
}