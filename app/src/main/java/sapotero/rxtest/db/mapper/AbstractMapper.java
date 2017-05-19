package sapotero.rxtest.db.mapper;

public abstract class AbstractMapper<M, E> implements Mapper<M, E> {
  @Override
  public boolean hasDiff(E entity, E entity2) {
    return !entity.equals(entity2);
  }
}
