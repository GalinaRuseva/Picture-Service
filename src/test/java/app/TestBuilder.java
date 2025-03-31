package app;

import lombok.experimental.UtilityClass;
import org.springframework.mock.web.MockMultipartFile;

@UtilityClass
public class TestBuilder {

    public static MockMultipartFile mockMultipartFile() {

        return new MockMultipartFile("file", "test.jpg", "image/jpeg", "test data".getBytes());

    }
}
