package app.web.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class PictureUploadResponse {

    private UUID id;

    private String pictureUniqueName;

    private LocalDateTime uploadDate;
}
