package user.api.usuario.usuario.service;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class S3ServiceTest {

    @Mock
    private S3Client s3Client;

    @InjectMocks
    private S3Service s3Service;

    public S3ServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void uploadImagemS3_ShouldUploadImageAndReturnUrl() throws IOException {
        // Arrange
        String key = "path/to/image.jpg";
        MultipartFile imagem = new MockMultipartFile("file", "image.jpg", "image/jpeg", "image content".getBytes());

        // Simula a resposta do S3Client
        PutObjectResponse response = PutObjectResponse.builder().build();
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class))).thenReturn(response);

        // Act
        String url = s3Service.uploadImagemS3(key, imagem);

        // Assert
        assertNotNull(url);
        assertTrue(url.contains(key));
        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void uploadImagemS3_ShouldThrowIOException_WhenFileConversionFails() throws IOException {
        // Arrange
        String key = "path/to/image.jpg";
        MultipartFile imagem = new MockMultipartFile("file", "image.jpg", "image/jpeg", "image content".getBytes()) {
            @Override
            public byte[] getBytes() throws IOException {
                throw new IOException("Simulated exception");
            }
        };

        // Act & Assert
        IOException thrown = assertThrows(IOException.class, () -> s3Service.uploadImagemS3(key, imagem));
        assertEquals("Simulated exception", thrown.getMessage());
    }

    @Test
    void convertMultipartFileToFile_ShouldConvertSuccessfully() throws IOException {
        // Arrange
        MultipartFile file = new MockMultipartFile("file", "image.jpg", "image/jpeg", "image content".getBytes());

        // Act
        File convertedFile = s3Service.convertMultipartFileToFile(file);

        // Assert
        assertTrue(convertedFile.exists());
        assertEquals("image.jpg", convertedFile.getName());
        // Limpar o arquivo temporário após o teste
        convertedFile.delete();
    }
}
