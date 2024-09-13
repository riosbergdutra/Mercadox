package api.product.produtos.dtos.ProdutosDto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;


import org.springframework.web.multipart.MultipartFile;

import api.product.produtos.enums.CategoriaProduto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ProdutoDtoRequest(@NotBlank(message = "Nome do produto não pode estar vazio") String nomeProduto,
                @NotNull(message = "URL da imagem não pode estar vazia") MultipartFile imagem,
                @NotNull(message = "Descrição do produto não pode estar vazia") String descricao,
                @NotEmpty(message = "URLs de fotos não podem estar vazias") List<@NotNull(message = "Cada foto deve ser um arquivo válido") MultipartFile> fotos,
                @NotBlank(message = "Cidade do vendedor não pode estar vazia") String cidadeVendedor,
                @NotNull(message = "Preço do produto não pode ser nulo") @Positive(message = "Preço do produto deve ser positivo") BigDecimal precoProduto,
                @NotNull(message = "Categoria do produto não pode ser nula") CategoriaProduto categoriaProduto,
                @NotNull(message = "ID do vendedor não pode ser nulo") UUID idVendedor) {
}
