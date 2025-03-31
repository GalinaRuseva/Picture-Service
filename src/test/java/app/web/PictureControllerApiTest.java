package app.web;

import app.model.Picture;
import app.service.PictureService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;


import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PictureController.class)
public class PictureControllerApiTest {

    @MockitoBean
    private PictureService pictureService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void givenMultipartFile_whenUploadPicture_thenReturnCreatedResponse() throws Exception {
        // Given
        MockMultipartFile pictureFile = new MockMultipartFile(
                "picture",
                "test.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test data".getBytes()
        );
        Picture storedPicture = Picture.builder()
                .id(UUID.randomUUID())
                .originalFileName("test.jpg")
                .type(MediaType.IMAGE_JPEG_VALUE)
                .size(10)
                .uploadDate(LocalDateTime.now())
                .filePath("/test/path")
                .pictureUrl("/test/url")
                .build();

        when(pictureService.savePictureToStorage(any())).thenReturn(storedPicture);

        // 1. Build Request
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.multipart("/api/v1/pictures/upload")
                .file(pictureFile);

        // 2. Send Request and Verify
        mockMvc.perform(requestBuilder)
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists());

        verify(pictureService, times(1)).savePictureToStorage(any());
    }

    @Test
    void givenNonExistingPictureId_whenViewByteFile_thenReturnsNotFound() throws Exception {
        // given
        UUID pictureId = UUID.randomUUID();
        when(pictureService.getPicture(pictureId.toString())).thenReturn(null);
        when(pictureService.getPictureById(pictureId)).thenReturn(Optional.empty());

        // request and response
        mockMvc.perform(get("/api/v1/pictures/view/{pictureId}", pictureId))
                .andExpect(status().isNotFound());
        verify(pictureService, times(1)).getPicture(pictureId.toString());
        verify(pictureService, times(1)).getPictureById(pictureId);
    }

    @Test
    void givenExistingPictureId_whenDeletePicture_thenReturnsNoContent() throws Exception {
        // given
        UUID pictureId = UUID.randomUUID();
        doNothing().when(pictureService).deletePicture(pictureId);

        // request
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/pictures/{pictureId}", pictureId))
                .andExpect(status().isNoContent());
        verify(pictureService, times(1)).deletePicture(pictureId);
    }


}
