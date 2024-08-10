package user.api.usuario.usuario.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.io.InputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

public class S3ServiceTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private S3Service s3Service;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testUploadImage() throws Exception {
        String key = "test-key";
        long fileSize = 1024L; // Tamanho do arquivo simulado

        // Simular comportamento do MultipartFile
        when(multipartFile.getInputStream()).thenReturn(mock(InputStream.class));
        when(multipartFile.getSize()).thenReturn(fileSize);

        // Executar o método a ser testado
        String returnedKey = s3Service.uploadImage(key, multipartFile);

        // Verificar se o método putObject foi chamado corretamente
        verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));

        // Verificar se a chave retornada é a esperada
        assertEquals(key, returnedKey);
    }
}
