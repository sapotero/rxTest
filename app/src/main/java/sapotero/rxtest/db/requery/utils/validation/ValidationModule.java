package sapotero.rxtest.db.requery.utils.validation;

import com.f2prateek.rx.preferences.RxSharedPreferences;

import dagger.Module;
import dagger.Provides;
import sapotero.rxtest.application.scopes.ValidationScope;

@Module
public final class ValidationModule {

  @Provides
  @ValidationScope
  Validation provideValidation(RxSharedPreferences rxSharedPreferences) {
    return new Validation(rxSharedPreferences);
  }

}