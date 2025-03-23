package app.web;

import app.model.Picture;
import app.service.PictureService;
import app.web.dto.PictureUploadResponse;
import app.web.mapper.DtoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/pictures")
public class PictureController {

    private final PictureService pictureService;

    @Autowired
    public PictureController(PictureService pictureService) {
        this.pictureService = pictureService;
    }

    @PostMapping("/upload")
    public ResponseEntity<PictureUploadResponse> uploadPicture(@RequestParam("picture") MultipartFile picture) {
        if(picture.isEmpty())
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        Picture storedPicture = pictureService.savePictureToStorage(picture);

        PictureUploadResponse response = DtoMapper.fromPicture(storedPicture);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/view/{pictureId:.+}")
    public ResponseEntity<byte[]> viewByteFile(@PathVariable UUID pictureId) {

        final String picId = pictureId.toString();
        byte[] pictureFromStorage = pictureService.getPicture(picId);
        //TODO: use mapper here.
        Optional<Picture> pictureById = pictureService.getPictureById(pictureId);

        return pictureById.map(picture ->
             ResponseEntity
                    .status(HttpStatus.OK)
                    .contentType(MediaType.parseMediaType(picture.getType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + picture.getOriginalFileName() + "\"")
                    .body(pictureFromStorage)
        ).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{pictureId:.+}")
    public ResponseEntity<Void> deletePicture(@PathVariable UUID pictureId) {
        pictureService.deletePicture(pictureId);
        return ResponseEntity.noContent().build();
    }
}
