package user.api.usuario.usuario.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Service
public class S3Service {

    @Autowired
    private S3Client s3Client;

    private final String bucketName = "FotoPerfil"; // Substitua pelo nome do seu bucket

   
    public String uploadImage(String key, byte[] imageContent) {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(imageContent));
            System.out.println("Imagem carregada com chave: " + key); // Log da chave
            return key; // Retorna o nome da imagem para armazenamento no banco de dados
        } catch (S3Exception e) {
            e.printStackTrace(); // Tratar exceções de forma adequada
            throw new RuntimeException("Erro ao fazer upload da imagem para o S3", e);
        }
    }

    public String getImageUrl(String key) {
        // Cria a URL completa do objeto no S3
        return String.format("https://%s.s3.amazonaws.com/%s", bucketName, key);
    }
}

