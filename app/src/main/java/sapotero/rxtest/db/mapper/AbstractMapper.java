package sapotero.rxtest.db.mapper;

import java.util.Collection;
import java.util.Objects;

public abstract class AbstractMapper<M, E> implements Mapper<M, E> {

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
}
