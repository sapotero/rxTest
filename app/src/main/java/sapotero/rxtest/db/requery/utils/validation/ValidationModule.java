package sapotero.rxtest.db.requery.utils.validation;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public final class ValidationModule {

  @Provides
  @Singleton
  Validation provideValidation() {
    return new Validation();
  }

}