package app.web.mapper;

import app.model.Picture;
import app.web.dto.PictureUploadResponse;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DtoMapper {

    public static PictureUploadResponse fromPicture(Picture picture) {

        return PictureUploadResponse.builder()
                .uploadDate(picture.getUploadDate())
                .pictureOriginalFileName(picture.getOriginalFileName())
                .id(picture.getId())
                .build();
    }
}
