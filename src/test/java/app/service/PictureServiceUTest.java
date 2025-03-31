package app.service;

import app.exception.PictureNotFoundException;
import app.model.Picture;
import app.repository.PictureRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PictureServiceUTest {

    @Mock
    private PictureRepository pictureRepository;

    //@InjectMocks
    private PictureService pictureService;

    private MultipartFile pictureFile;
    private Path fileStorageLocation;
    private String fileUploadDir = "test-uploads";

    @BeforeEach
    void setUp() {
        pictureFile = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "test data".getBytes()
        );
        fileStorageLocation = Paths.get(fileUploadDir).toAbsolutePath().normalize();
        pictureService = new PictureService(fileUploadDir, pictureRepository); // Manual instantiation
    }


    @Test
    void givenExistingPicture_whenGetPictureById_thenReturnCorrectPicture() {

        //given
        Picture picture = Picture.builder()
                .id(UUID.randomUUID())
                .build();
        when(pictureRepository.findById(picture.getId())).thenReturn(Optional.of(picture));

        //when
        Optional<Picture> pictureById = pictureService.getPictureById(picture.getId());

        //then
        assertEquals(picture.getId(), pictureById.get().getId());
    }

    @Test
    void givenNullMultipartFile_whenSavePictureToStorage_thenThrowsPictureNotFoundException() {
        assertThrows(PictureNotFoundException.class, () -> pictureService.savePictureToStorage(null));
    }

    @Test
    void givenMultipartFile_whenSavePictureToStorage_thenPictureIsSaved() throws IOException {
        // Given
        UUID pictureId = UUID.randomUUID();
        Path filePath = fileStorageLocation.resolve(pictureId.toString());

        // Mock the ServletRequestAttributes
        MockHttpServletRequest request = new MockHttpServletRequest();
        ServletRequestAttributes requestAttributes = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(requestAttributes);

        Picture picture = Picture.builder()
                .id(pictureId)
                .originalFileName(pictureFile.getOriginalFilename())
                .type(pictureFile.getContentType())
                .size(pictureFile.getSize())
                .pictureUrl(ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path("/api/v1/pictures/view/")
                        .path(pictureId.toString())
                        .toUriString())
                .filePath(filePath.toAbsolutePath().toString())
                .build();
        Path picturePath = fileStorageLocation.resolve((picture.getId()).toString());
        when(pictureRepository.save(any())).thenReturn(picture);

        // When
        Picture savedPicture = pictureService.savePictureToStorage(pictureFile);

        // Then
        assertNotNull(savedPicture);
        assertEquals("test.jpg", savedPicture.getOriginalFileName());
        assertEquals("image/jpeg", savedPicture.getType());
        verify(pictureRepository, times(1)).save(any());
        Files.deleteIfExists(picturePath); //cleanup
        RequestContextHolder.resetRequestAttributes(); // Clean up
    }

    @Test
    void givenExistingPictureName_whenGetPicture_thenReturnByteArray() throws IOException {
        String pictureName = "test.jpg";
        byte[] pictureBytes = "test data".getBytes();
        Path picturePath = fileStorageLocation.resolve(pictureName);
        Files.write(picturePath, pictureBytes);

        byte[] result = pictureService.getPicture(pictureName);

        assertArrayEquals(pictureBytes, result);
        Files.deleteIfExists(picturePath); //cleanup
    }

    @Test
    void givenNonExistentPictureName_whenGetPicture_thenThrowsPictureNotFoundException() {
        assertThrows(PictureNotFoundException.class, () -> pictureService.getPicture("nonexistent.jpg"));
    }

    @Test
    void givenExistingPictureId_whenGetFileType_thenReturnContentType() throws IOException {
        String pictureId = "test.jpg";
        Path picturePath = fileStorageLocation.resolve(pictureId);
        String contentType = pictureService.getFileType(pictureId);
        assertEquals("image/jpeg", contentType);
        Files.deleteIfExists(picturePath); //cleanup
    }

    @Test
    void givenPictureId_whenDeletePicture_thenDeletePicture() throws IOException {
        UUID pictureId = UUID.randomUUID();
        Picture picture = Picture.builder().id(pictureId).build();
        when(pictureRepository.findById(pictureId)).thenReturn(Optional.of(picture));
        Path picturePath = fileStorageLocation.resolve(pictureId.toString());
        Files.createFile(picturePath);

        pictureService.deletePicture(pictureId);

        verify(pictureRepository, times(1)).delete(picture);
        assertFalse(Files.exists(picturePath));
        Files.deleteIfExists(picturePath); //cleanup
    }

    @Test
    void givenNonExistentPictureId_whenDeletePicture_thenThrowsPictureNotFoundException() {
        UUID pictureId = UUID.randomUUID();
        when(pictureRepository.findById(pictureId)).thenReturn(Optional.empty());
        assertThrows(PictureNotFoundException.class, () -> pictureService.deletePicture(pictureId));
    }
}
