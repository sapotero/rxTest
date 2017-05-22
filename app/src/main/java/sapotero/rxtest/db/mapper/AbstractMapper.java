package sapotero.rxtest.db.mapper;

import java.util.List;
import java.util.Set;

public abstract class AbstractMapper<M, E> implements Mapper<M, E> {

  @Override
  public boolean hasDiff(E entity, E entity2) {
    return !entity.equals(entity2);
  }

  public <T> boolean listNotEmpty(List<T> list) {
    return list != null && list.size() > 0;
  }

  public <T> boolean setNotEmpty(Set<T> set) {
    return set != null && set.size() > 0;
  }
}
