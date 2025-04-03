package app.service;

import app.exception.PictureNotFoundException;
import app.model.Picture;
import app.repository.PictureRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class PictureService {

    private final Path fileStorageLocation;
    private final PictureRepository pictureRepository;

    @Autowired
    public PictureService(@Value("${file.upload.dir}") String fileUploadDir, PictureRepository pictureRepository) {
        this.fileStorageLocation = Paths.get(fileUploadDir).toAbsolutePath().normalize();
        this.pictureRepository = pictureRepository;
    }

    // Save image in a local directory
    public Picture savePictureToStorage(MultipartFile pictureFile) {
        if (pictureFile == null)
            throw new PictureNotFoundException();

        UUID pictureId = UUID.randomUUID();

        try {
            Path filePath = fileStorageLocation.resolve(pictureId.toString());

            if (!Files.exists(fileStorageLocation)) {
                Files.createDirectories(fileStorageLocation);
            }

            Files.copy(pictureFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            log.info("Saving picture to storage with id [%s]".formatted(pictureId.toString()));

            String pictureUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("api/v1/pictures")
                    .path("/view/")
                    .path(pictureId.toString())
                    .toUriString();
            // http://localhost:8081/api/v1/pictures/view/uuuid.jpg

            Picture picture = Picture.builder()
                    .id(pictureId)
                    .originalFileName(StringUtils.cleanPath(pictureFile.getOriginalFilename()))
                    .type(pictureFile.getContentType())
                    .size(pictureFile.getSize())
                    .uploadDate(LocalDateTime.now())
                    .filePath(filePath.toAbsolutePath().toString())
                    .pictureUrl(pictureUri)
                    .build();

            pictureRepository.save(picture);

            return picture;

        } catch (Exception e) {
            throw new RuntimeException("Could not store picture " + pictureId + ". Please try again!", e);
        }
    }

    // get image data from database
    public Optional<Picture> getPictureById(UUID pictureId) {

        return pictureRepository.findById(pictureId);
    }

    // to view an image
    public byte[] getPicture(String picId) {
        try {
            Path picturePath = Path.of(String.valueOf(fileStorageLocation), picId);

            if (Files.exists(picturePath)) {
                return Files.readAllBytes(picturePath);
            } else {
                throw new PictureNotFoundException("Picture with id [%s] not found".formatted(picId));
            }
        } catch (Exception e) {
            throw new PictureNotFoundException("Picture with id [%s] not found".formatted(picId));
        }
    }

    public String getFileType(String pictureId) throws IOException {
        String contentType = Files.probeContentType(fileStorageLocation.resolve(pictureId).normalize());
        if (contentType == null) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
        return contentType;
    }

    // delete an image
    public void deletePicture(UUID pictureId) {
        Path picturePath = Path.of(String.valueOf(fileStorageLocation), String.valueOf(pictureId));


        Optional<Picture> optionalPicture = pictureRepository.findById(pictureId);
        if (optionalPicture.isEmpty()) {
            throw new PictureNotFoundException("Picture with id [%s] not found".formatted(pictureId));
        }

        try {
            Picture picture = optionalPicture.get();

            if (Files.exists(picturePath)) {
                Files.delete(picturePath);
                pictureRepository.delete(picture);
                log.info("Successfully deleted picture with id:[%s]".formatted(pictureId));
            }

        } catch (Exception e) {
            throw new PictureNotFoundException("Picture with name [%s] not found".formatted(pictureId));
        }
    }

}
