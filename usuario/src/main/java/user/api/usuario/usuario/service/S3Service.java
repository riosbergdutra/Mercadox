package user.api.usuario.usuario.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import user.api.usuario.usuario.exceptions.S3ImageDeletionException;

/**
 * Serviço para gerenciamento de arquivos no Amazon S3.
 * 
 * Este serviço fornece métodos para fazer upload de imagens para um bucket S3.
 */
@Service
public class S3Service {

    @Autowired
    private S3Client s3Client;

    private static final String BUCKET_NAME = "usuario";

    /**
     * Faz o upload de uma imagem para o bucket S3 especificado.
     * 
     * @param key    Chave do objeto no S3, geralmente o caminho do arquivo.
     * @param imagem Imagem a ser carregada, representada por um MultipartFile.
     * @return URL pública da imagem no S3.
     * @throws IOException Se ocorrer um erro ao converter o MultipartFile em um
     *                     arquivo.
     */
    public String uploadImagemS3(String key, MultipartFile imagem) throws IOException {
        File file = convertMultipartFileToFile(imagem);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(BUCKET_NAME)
                .key(key)
                .build();

        s3Client.putObject(putObjectRequest, software.amazon.awssdk.core.sync.RequestBody.fromFile(file));
        file.delete();

        // Construir a URL da imagem no S3
        return String.format("http://localhost:4566/%s/%s", BUCKET_NAME, key);
    }

    /**
     * Converte um MultipartFile em um arquivo temporário.
     * 
     * @param file MultipartFile a ser convertido.
     * @return Arquivo convertido.
     * @throws IOException Se ocorrer um erro ao criar o arquivo.
     */
    File convertMultipartFileToFile(MultipartFile file) throws IOException {
        File convFile = new File(file.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(convFile)) {
            fos.write(file.getBytes());
        }
        return convFile;
    }

    /**
     * Exclui uma imagem do bucket S3.
     * 
     * @param imagemKey Chave da imagem no S3 que deve ser excluída.
     * @throws S3ImageDeletionException Se ocorrer um erro ao tentar excluir a imagem.
     */
    public void deleteImagemS3(String imagemKey) {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(BUCKET_NAME)
                .key(imagemKey)
                .build();

            s3Client.deleteObject(deleteObjectRequest);
        } catch (Exception e) {
            throw new S3ImageDeletionException("Falha ao excluir a imagem do S3", e);
        }
    }
}