package sapotero.rxtest.db.mapper;

// Maps between model (pojo) (M) and requery entity (E)
public interface Mapper<M, E> {
  E toEntity(M model);
  M toModel(E entity);
  boolean hasDiff(E entity, E entity2);
}
