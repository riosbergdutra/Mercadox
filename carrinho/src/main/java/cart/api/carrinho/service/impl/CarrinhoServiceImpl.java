package cart.api.carrinho.service.impl;

import java.util.Optional;
import java.util.UUID;
import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import cart.api.carrinho.dto.CarrinhoDtoRequest;
import cart.api.carrinho.dto.CriarPedidoRequestDto;

import cart.api.carrinho.exceptions.CarrinhoNotFoundException;
import cart.api.carrinho.model.Carrinho;
import cart.api.carrinho.model.ItemCarrinho;
import cart.api.carrinho.repository.CarrinhoRepository;
import cart.api.carrinho.service.CarrinhoService;

@Service
public class CarrinhoServiceImpl implements CarrinhoService {
    @Autowired
    CarrinhoRepository carrinhoRepository;

    @Autowired
    RestTemplate restTemplate;

    @Override
    public Carrinho getCarrinhoByIdUsuario(UUID idUsuario) {
        return carrinhoRepository.findByIdUsuario(idUsuario)
                .filter(carrinho -> carrinho.getIdUsuario().equals(idUsuario))
                .orElseThrow(() -> new CarrinhoNotFoundException("Carrinho não encontrado ou não autorizado."));
    }

    /**
     * Processa a mensagem SQS para executar ações no carrinho.
     *
     * @param mensagemSQS Mensagem recebida da fila SQS.
     */
    public void processarMensagemSQS(String mensagemSQS) {
        // Extraia o ID do usuário da mensagem
        UUID idUsuario = extrairIdUsuarioDaMensagem(mensagemSQS);

        // Verifique se a mensagem contém informações para criar ou deletar um carrinho
        if (mensagemSQS.contains("CRIAR")) {
            // Criar um novo carrinho para o usuário
            Carrinho novoCarrinho = new Carrinho();
            novoCarrinho.setIdUsuario(idUsuario);
            carrinhoRepository.save(novoCarrinho);
            System.out.println("Novo carrinho criado para o usuário: " + idUsuario);
        } else if (mensagemSQS.contains("DELETAR")) {
            // Remover o carrinho associado ao usuário
            Optional<Carrinho> carrinhoOptional = carrinhoRepository.findByIdUsuario(idUsuario);
            if (carrinhoOptional.isPresent()) {
                carrinhoRepository.delete(carrinhoOptional.get());
                System.out.println("Carrinho deletado para o usuário: " + idUsuario);
            } else {
                System.out.println("Nenhum carrinho encontrado para o usuário: " + idUsuario);
            }
        } else {
            System.out.println("Ação desconhecida na mensagem: " + mensagemSQS);
        }
    }

    @Override
    public void adicionarProdutoAoCarrinho(CarrinhoDtoRequest carrinhoDtoRequest, UUID idCarrinho, UUID idUsuario) {
        // Valida se o carrinho pertence ao usuário autenticado
        Carrinho carrinho = validarCarrinhoUsuario(idCarrinho, idUsuario);

        // Verifica se o produto já está no carrinho
        Optional<ItemCarrinho> itemExistente = carrinho.getItens().stream()
                .filter(item -> item.getIdProduto().equals(carrinhoDtoRequest.idProduto()))
                .findFirst();

        if (itemExistente.isPresent()) {
            // Se o produto já está no carrinho, atualiza a quantidade
            ItemCarrinho item = itemExistente.get();
            item.setQuantidade(item.getQuantidade() + carrinhoDtoRequest.quantidade());
        } else {
            // Caso contrário, cria um novo item no carrinho
            ItemCarrinho novoItem = new ItemCarrinho();
            novoItem.setIdProduto(carrinhoDtoRequest.idProduto());
            novoItem.setQuantidade(carrinhoDtoRequest.quantidade());
            novoItem.setPrecoUnitario(carrinhoDtoRequest.precoUnitario()); // Preço unitário
            carrinho.getItens().add(novoItem);
        }

        // Recalcula o valor total do carrinho
        BigDecimal novoValorTotal = carrinho.getItens().stream()
                .map(item -> item.getPrecoUnitario().multiply(BigDecimal.valueOf(item.getQuantidade()))) // Multiplica
                                                                                                         // quantidade
                                                                                                         // pelo preço
                                                                                                         // unitário
                .reduce(BigDecimal.ZERO, BigDecimal::add); // Soma todos os valores usando reduce

        carrinho.setValorTotal(novoValorTotal); // Atualiza o valor total do carrinho

        // Salva o carrinho atualizado
        carrinhoRepository.save(carrinho);
    }

    /**
     * Método auxiliar para extrair o idUsuario da mensagem SQS.
     * 
     * @param mensagemSQS Mensagem SQS.
     * @return idUsuario extraído da mensagem.
     */
    private UUID extrairIdUsuarioDaMensagem(String mensagemSQS) {
        String[] partes = mensagemSQS.split("idUsuario: ");
        if (partes.length < 2) {
            throw new IllegalArgumentException("Formato de mensagem inválido: " + mensagemSQS);
        }

        String idUsuarioString = partes[1].split("\n")[0].trim(); // Supondo que a próxima linha ou quebra seja o fim do
                                                                  // ID

        try {
            return UUID.fromString(idUsuarioString);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("ID de usuário inválido: " + idUsuarioString);
        }
    }

    @Override
    public void limparCarrinho(UUID idCarrinho, UUID idUsuario) {
        // Valida se o carrinho pertence ao usuário autenticado
        Carrinho carrinho = validarCarrinhoUsuario(idCarrinho, idUsuario);

        // Limpa todos os itens do carrinho
        carrinho.getItens().clear();

        // Salva o carrinho atualizado (sem itens)
        carrinhoRepository.save(carrinho);
    }

    @Override
    public void removerItemDoCarrinho(UUID idCarrinho, UUID idUsuario, Long idProduto) {
        // Valida se o carrinho pertence ao usuário autenticado
        Carrinho carrinho = validarCarrinhoUsuario(idCarrinho, idUsuario);

        // Verifica se o produto existe no carrinho
        Optional<ItemCarrinho> itemExistente = carrinho.getItens().stream()
                .filter(item -> item.getIdProduto().equals(idProduto))
                .findFirst();

        if (itemExistente.isPresent()) {
            // Remove o item do carrinho
            carrinho.getItens().remove(itemExistente.get());

            // Salva o carrinho atualizado
            carrinhoRepository.save(carrinho);
        } else {
            throw new CarrinhoNotFoundException("Produto não encontrado no carrinho.");
        }
    }

    public void finalizarPedido(UUID idUsuario, UUID idCarrinho, CriarPedidoRequestDto pedidoRequestDto) {
        Carrinho carrinho = validarCarrinhoUsuario(idCarrinho, idCarrinho);

        // Calcular o valor total (se ainda não foi calculado)
        carrinho.calcularValorTotal();

        var pedidoRequest = new CriarPedidoRequestDto(
                pedidoRequestDto.enderecoEntrega(),
                carrinho.getItens(),
                carrinho.getValorTotal(),
                pedidoRequestDto.formaPagamento());


                String url = String.format("http://localhost:8085/pedidos/%s/checkout/", idUsuario);
                restTemplate.postForEntity(url, pedidoRequest, Void.class);

        limparCarrinho(idCarrinho, idUsuario);
    }

    private Carrinho validarCarrinhoUsuario(UUID idCarrinho, UUID userId) {
        return carrinhoRepository.findByIdCarrinhoAndIdUsuario(idCarrinho, userId)
                .filter(carrinho -> carrinho.getIdUsuario().equals(userId))
                .orElseThrow(() -> new CarrinhoNotFoundException(
                        "carrinho não encontrado"));
    }
}
