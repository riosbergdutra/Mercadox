package user.api.usuario.usuario.service;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import user.api.usuario.usuario.exceptions.S3ImageDeletionException;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes para o serviço S3Service.
 * 
 * Os testes verificam a funcionalidade de upload, exclusão e conversão de arquivos para o serviço S3.
 */
public class S3ServiceTest {

    @Mock
    private S3Client s3Client;

    @InjectMocks
    private S3Service s3Service;

    public S3ServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Testa o upload de uma imagem para o S3.
     * Verifica se a imagem é enviada corretamente e se a URL é retornada conforme o esperado.
     */
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
        assertTrue(url.contains(key)); // Verifica se a URL contém o caminho da imagem
        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class)); // Verifica se o método putObject foi chamado
    }

    /**
     * Testa o upload de uma imagem quando a conversão do arquivo falha.
     * Verifica se uma exceção IOException é lançada quando a conversão do arquivo falha.
     */
    @Test
    void uploadImagemS3_ShouldThrowIOException_WhenFileConversionFails() throws IOException {
        // Arrange
        String key = "path/to/image.jpg";
        MultipartFile imagem = new MockMultipartFile("file", "image.jpg", "image/jpeg", "image content".getBytes()) {
            @Override
            public byte[] getBytes() throws IOException {
                throw new IOException("Simulated exception"); // Simula a falha na conversão do arquivo
            }
        };

        // Act & Assert
        IOException thrown = assertThrows(IOException.class, () -> s3Service.uploadImagemS3(key, imagem));
        assertEquals("Simulated exception", thrown.getMessage()); // Verifica se a exceção lançada é a esperada
    }

    /**
     * Testa a conversão de um MultipartFile para um arquivo.
     * Verifica se o arquivo é convertido corretamente e se o arquivo temporário é criado com o nome esperado.
     */
    @Test
    void convertMultipartFileToFile_ShouldConvertSuccessfully() throws IOException {
        // Arrange
        MultipartFile file = new MockMultipartFile("file", "image.jpg", "image/jpeg", "image content".getBytes());

        // Act
        File convertedFile = s3Service.convertMultipartFileToFile(file);

        // Assert
        assertTrue(convertedFile.exists()); // Verifica se o arquivo foi criado
        assertEquals("image.jpg", convertedFile.getName()); // Verifica se o nome do arquivo está correto
        convertedFile.delete(); // Limpa o arquivo temporário após o teste
    }

    /**
     * Testa a exclusão de uma imagem do S3.
     * Verifica se a imagem é excluída corretamente e se a chamada ao método deleteObject do S3Client ocorre.
     */
    @Test
    void deleteImagemS3_ShouldDeleteImageSuccessfully() {
        // Arrange
        String imagemKey = "path/to/image.jpg";

        // Act
        s3Service.deleteImagemS3(imagemKey);

        // Assert
        verify(s3Client).deleteObject(any(DeleteObjectRequest.class)); // Verifica se o método deleteObject foi chamado
    }

    /**
     * Testa a exclusão de uma imagem do S3 quando ocorre uma falha na exclusão.
     * Verifica se uma exceção S3ImageDeletionException é lançada quando ocorre uma falha na exclusão.
     */
    @Test
    void deleteImagemS3_ShouldThrowS3ImageDeletionException_WhenDeletionFails() {
        // Arrange
        String imagemKey = "path/to/image.jpg";
        doThrow(RuntimeException.class).when(s3Client).deleteObject(any(DeleteObjectRequest.class)); // Simula uma falha na exclusão

        // Act & Assert
        S3ImageDeletionException thrown = assertThrows(S3ImageDeletionException.class, () -> s3Service.deleteImagemS3(imagemKey));
        assertEquals("Falha ao excluir a imagem do S3", thrown.getMessage()); // Verifica se a exceção lançada é a esperada
    }
}
