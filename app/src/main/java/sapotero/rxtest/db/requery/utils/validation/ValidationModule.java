package sapotero.rxtest.db.requery.utils.validation;

import com.f2prateek.rx.preferences.RxSharedPreferences;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public final class ValidationModule {

  @Provides
  @Singleton
  Validation provideValidation(RxSharedPreferences rxSharedPreferences) {
    return new Validation(rxSharedPreferences);
  }

}