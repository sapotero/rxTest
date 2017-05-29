package sapotero.rxtest.utils.memory.utils;

public class IMDValidation {
  public static Boolean isMd5Changed(String m1, String m2){
    return !m1.equals( m2 );
  }
}