package sapotero.rxtest.utils;

// todo запилить хранилку настроек
public class Settings {

  private static Settings instance;

  private Settings() {

  }

  public static Settings getInstance() {

    if (instance == null) {
      instance = new Settings();
    }

    return instance;
  }
}
