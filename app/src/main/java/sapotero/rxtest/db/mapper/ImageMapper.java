package sapotero.rxtest.db.mapper;

import sapotero.rxtest.db.requery.models.images.RImageEntity;
import sapotero.rxtest.retrofit.models.document.Image;

// Maps between Image and RImageEntity
public class ImageMapper extends AbstractMapper<Image, RImageEntity> {

  @Override
  public RImageEntity toEntity(Image model) {
    RImageEntity entity = new RImageEntity();

    entity.setTitle(model.getTitle());
    entity.setNumber(model.getNumber());
    entity.setMd5(model.getMd5());
    entity.setSize(model.getSize());
    entity.setPath(model.getPath());
    entity.setContentType(model.getContentType());
    entity.setSigned(model.getSigned());
    entity.setCreatedAt(model.getCreatedAt());
    entity.setImageId( model.getPath().substring(11, 35) );
    entity.setLoading(false);
    entity.setComplete(false);
    entity.setError(false);
    entity.setDeleted(false);
    entity.setFileName( String.format( "%s_%s", entity.getImageId(), entity.getTitle() ) );
    entity.setNoFreeSpace(false);
    entity.setToDeleteFile(false);
    entity.setToLoadFile(true);

    return entity;
  }

  @Override
  public Image toModel(RImageEntity entity) {
    Image model = new Image();

    model.setTitle(entity.getTitle());
    model.setNumber(entity.getNumber());
    model.setMd5(entity.getMd5());
    model.setSize(entity.getSize());
    model.setPath(entity.getPath());
    model.setContentType(entity.getContentType());
    model.setSigned(entity.isSigned());
    model.setCreatedAt(entity.getCreatedAt());
    model.setDeleted(entity.isDeleted());
    model.setFileName(entity.getFileName());
    model.setNoFreeSpace(entity.isNoFreeSpace());
    model.setImageId(entity.getImageId());
    model.setIdInDb(entity.getId());

    return model;
  }
}
