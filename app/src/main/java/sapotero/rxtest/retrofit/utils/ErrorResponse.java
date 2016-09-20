package sapotero.rxtest.retrofit.utils;

/**
 * Created by sapotero on 19.09.16.
 */
public class ErrorResponse {
  Error error;

  public static class Error {
    Data data;

    public static class Data {
      String message;
    }
  }
}
