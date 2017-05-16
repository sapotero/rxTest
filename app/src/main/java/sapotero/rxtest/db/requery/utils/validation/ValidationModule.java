package sapotero.rxtest.db.requery.utils.validation;

import dagger.Module;
import dagger.Provides;
import sapotero.rxtest.application.scopes.ValidationScope;
import sapotero.rxtest.utils.Settings;

@Module
public final class ValidationModule {

  @Provides
  @ValidationScope
  Validation provideValidation(Settings settings) {
    return new Validation(settings);
  }

}