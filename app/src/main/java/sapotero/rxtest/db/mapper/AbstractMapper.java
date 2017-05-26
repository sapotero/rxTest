package sapotero.rxtest.db.mapper;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import sapotero.rxtest.db.mapper.utils.Mappers;
import sapotero.rxtest.utils.Settings;

public abstract class AbstractMapper<M, E> implements Mapper<M, E> {

  public Settings settings;
  public Mappers mappers;

  public AbstractMapper(Settings settings, Mappers mappers) {
    this.settings = settings;
    this.mappers = mappers;
  }

  @Override
  public boolean hasDiff(E entity, E entity2) {
    return !entity.equals(entity2);
  }

  public <T> boolean notEmpty(Collection<T> collection) {
    return collection != null && collection.size() > 0;
  }

  public boolean notEmpty(String s) {
    return s != null && !Objects.equals(s, "");
  }

  public boolean exist(Object obj) {
    return obj != null;
  }

  interface StringFieldSetter {
    void setField(String s);
  }

  interface ListFieldSetter<T> {
    void setField(List<T> list);
  }

  public void set(StringFieldSetter stringFieldSetter, String s) {
    if ( exist( s ) ) {
      stringFieldSetter.setField( s );
    }
  }
}
