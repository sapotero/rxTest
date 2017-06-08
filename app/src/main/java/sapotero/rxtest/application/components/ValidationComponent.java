package sapotero.rxtest.application.components;

import dagger.Subcomponent;
import sapotero.rxtest.application.scopes.ValidationScope;
import sapotero.rxtest.db.requery.utils.validation.ValidationModule;

@ValidationScope
@Subcomponent(modules = {
  ValidationModule.class
})

public interface ValidationComponent {
//  void inject(DocumentTypeItem documentTypeItem);
//  void inject(ButtonBuilder buttonBuilder);
//  void inject(DBQueryBuilder dbQueryBuilder);
}
