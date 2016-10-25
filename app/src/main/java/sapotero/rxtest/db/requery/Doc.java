package sapotero.rxtest.db.requery;

import android.databinding.Bindable;
import android.databinding.Observable;
import android.os.Parcelable;

import java.util.Date;
import java.util.UUID;

import io.requery.Column;
import io.requery.Entity;
import io.requery.ForeignKey;
import io.requery.Generated;
import io.requery.Index;
import io.requery.Key;
import io.requery.OneToOne;
import io.requery.Persistable;

@Entity
public interface Doc extends Observable, Parcelable, Persistable {

  @Key @Generated
  int getId();

  @Bindable
  String getName();

  @Bindable
  @Index(value = "email_index")
  String getEmail();

  @Bindable
  Date getBirthday();

  @Bindable
  int getAge();

  @Bindable
  @ForeignKey
  @OneToOne
  Signer getSigner();

  @Bindable
  @Column(unique = true)
  UUID getUUID();
}