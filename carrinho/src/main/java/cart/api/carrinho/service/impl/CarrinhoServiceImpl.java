package cart.api.carrinho.service.impl;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cart.api.carrinho.model.Carrinho;
import cart.api.carrinho.repository.CarrinhoRepository;
import cart.api.carrinho.service.CarrinhoService;

@Service
public class CarrinhoServiceImpl implements CarrinhoService {
    @Autowired
    CarrinhoRepository carrinhoRepository;

    @Override
    public Optional<Carrinho> getCarrinhoByIdUsuario(UUID idUsuario, UUID userId) {
        return carrinhoRepository.findByIdUsuario(idUsuario)
                .filter(carrinho -> carrinho.getIdUsuario().equals(userId));
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
        
        String idUsuarioString = partes[1].split("\n")[0].trim(); // Supondo que a próxima linha ou quebra seja o fim do ID
        
        try {
            return UUID.fromString(idUsuarioString);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("ID de usuário inválido: " + idUsuarioString);
        }
    }
    
    
}
