package sapotero.rxtest.db.requery;


import java.util.Comparator;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.Callable;

import io.requery.Persistable;
import io.requery.rx.SingleEntityStore;

public class CreateDoc implements Callable<rx.Observable<Iterable<Doc>>> {

  private final SingleEntityStore<Persistable> data;

  public CreateDoc(SingleEntityStore<Persistable> data) {
    this.data = data;
  }

  @Override
  public rx.Observable<Iterable<Doc>> call() {
    String[] firstNames = new String[]{
      "Alice", "Bob", "Carol", "Chloe", "Dan", "Emily", "Emma", "Eric", "Eva",
      "Frank", "Gary", "Helen", "Jack", "James", "Jane",
      "Kevin", "Laura", "Leon", "Lilly", "Mary", "Maria",
      "Mia", "Nick", "Oliver", "Olivia", "Patrick", "Robert",
      "Stan", "Vivian", "Wesley", "Zoe"};
    String[] lastNames = new String[]{
      "Hall", "Hill", "Smith", "Lee", "Jones", "Taylor", "Williams", "Jackson",
      "Stone", "Brown", "Thomas", "Clark", "Lewis", "Miller", "Walker", "Fox",
      "Robinson", "Wilson", "Cook", "Carter", "Cooper", "Martin" };
    Random random = new Random();

    final Set<Doc> documents = new TreeSet<>(new Comparator<Doc>() {
      @Override
      public int compare(Doc lhs, Doc rhs) {
        return lhs.getName().compareTo(rhs.getName());
      }
    });
    // creating many documents (but only with unique names)
    for (int i = 0; i < 3000; i++) {
      DocEntity dic = new DocEntity();
      String first = firstNames[random.nextInt(firstNames.length)];
      String last  = lastNames[random.nextInt(lastNames.length)];
      dic.setName(first + " " + last);
      dic.setUUID(UUID.randomUUID());
      dic.setEmail(Character.toLowerCase(first.charAt(0)) +
        last.toLowerCase() + "@gmail.com");

      SignerEntity signer = new SignerEntity();
      signer.setLine1("123 Market St");
      signer.setZip("94105");
      signer.setCity("San Francisco");
      signer.setState("CA");
      signer.setCountry("US");
      dic.setSigner(signer);
      documents.add(dic);
    }
    return data.insert(documents).toObservable();
  }
}