package sapotero.rxtest.application.components;

import javax.inject.Singleton;

import dagger.Component;
import sapotero.rxtest.application.modules.EsdModule;
import sapotero.rxtest.application.modules.SettingsModule;
import sapotero.rxtest.db.requery.query.DBQueryBuilder;
import sapotero.rxtest.db.requery.utils.RequeryDbModule;
import sapotero.rxtest.db.requery.utils.validation.ValidationModule;
import sapotero.rxtest.views.adapters.models.DocumentTypeItem;
import sapotero.rxtest.views.menu.builders.ButtonBuilder;

@Singleton
@Component(modules = {
        EsdModule.class,
        SettingsModule.class,
        RequeryDbModule.class,
        ValidationModule.class
})

public interface ValidationComponent {

  void inject(DocumentTypeItem context);
  void inject(ButtonBuilder context);
  void inject(DBQueryBuilder context);
}
