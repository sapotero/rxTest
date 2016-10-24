package sapotero.rxtest.db.Storio.entities;

import android.support.annotation.NonNull;

public class DocWithSigner {
  @NonNull
  private final Doc doc;

  @NonNull
  private final Signer signer;

  public DocWithSigner(@NonNull Doc doc, @NonNull Signer signer) {
    this.doc = doc;
    this.signer = signer;
  }

  @NonNull
  public Doc doc() {
    return doc;
  }

  @NonNull
  public Signer signer() {
    return signer;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    DocWithSigner that = (DocWithSigner) o;

    if (!doc.equals(that.doc)) return false;
    return signer.equals(that.signer);
  }

  @Override
  public int hashCode() {
    int result = doc.hashCode();
    result = 31 * result + signer.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "DocWithSigner{" +
      "doc=" + doc +
      ", signer=" + signer +
      '}';
  }
}
