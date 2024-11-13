package user.api.usuario.usuario.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import user.api.usuario.usuario.dtos.EnderecoDto.EnderecoDto;
import user.api.usuario.usuario.exceptions.EnderecoNotFoundException;
import user.api.usuario.usuario.exceptions.UsuarioNotFoundException;
import user.api.usuario.usuario.model.Endereco;
import user.api.usuario.usuario.repository.EnderecoRepository;
import user.api.usuario.usuario.service.EnderecoService;
import user.api.usuario.usuario.model.Usuario;
import user.api.usuario.usuario.repository.UsuarioRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementação do serviço para gerenciamento de endereços.
 * 
 * Esta classe implementa a interface {@link EnderecoService} e fornece as
 * operações de CRUD para endereços associados a usuários.
 */
@Service
public class EnderecoServiceImpl implements EnderecoService {

        @Autowired
        private EnderecoRepository enderecoRepository;

        @Autowired
        private UsuarioRepository usuarioRepository;

        /**
         * Salva um novo endereço associado ao usuário especificado.
         * 
         * @param enderecoDto O DTO contendo os dados do endereço a ser salvo.
         * @param userId      O ID do usuário ao qual o endereço será associado.
         * @return O DTO do endereço salvo.
         * @throws UsuarioNotFoundException Se o usuário com o ID fornecido não for
         *                                  encontrado.
         */
        @Override
        public EnderecoDto saveEndereco(EnderecoDto enderecoDto, UUID userId) {
                Usuario usuario = usuarioRepository.findById(userId)
                                .orElseThrow(() -> new UsuarioNotFoundException("user.not.found"));

                Endereco novoEndereco = new Endereco();
                novoEndereco.setRua(enderecoDto.rua());
                novoEndereco.setNumero(enderecoDto.numero());
                novoEndereco.setCidade(enderecoDto.cidade());
                novoEndereco.setEstado(enderecoDto.estado());
                novoEndereco.setCep(enderecoDto.cep());
                novoEndereco.setUsuario(usuario); // Associa o endereço ao usuário
                enderecoRepository.save(novoEndereco);
                return new EnderecoDto(novoEndereco.getRua(), novoEndereco.getNumero(), novoEndereco.getCidade(),
                                novoEndereco.getEstado(), novoEndereco.getCep());
        }

        /**
         * Recupera um endereço pelo ID, verificando se pertence ao usuário
         * especificado.
         * 
         * @param idEndereco     O ID do endereço a ser recuperado.
         * @param userId O ID do usuário que deve ser associado ao endereço.
         * @return Um {@link Optional} contendo o DTO do endereço se encontrado e
         *         associado ao usuário, ou vazio se não encontrado.
         * @throws EnderecoNotFoundException Se o endereço não for encontrado ou não
         *                                   pertencer ao usuário.
         */
        @Override
        public Optional<EnderecoDto> getEnderecoById(UUID idEndereco, UUID userId) {
                return enderecoRepository.findById(idEndereco)
                                .filter(endereco -> endereco.getUsuario().getIdUsuario().equals(userId)) // Verifica a
                                                                                                         // associação
                                                                                                         // com o
                                                                                                         // usuário
                                .map(endereco -> new EnderecoDto(endereco.getRua(), endereco.getNumero(),
                                                endereco.getCidade(),
                                                endereco.getEstado(), endereco.getCep()));
        }

        /**
         * Recupera todos os endereços associados ao usuário especificado.
         * 
         * @param userId O ID do usuário cujos endereços devem ser recuperados.
         * @return Uma lista de DTOs de endereços associados ao usuário.
         */
        @Override
        public List<EnderecoDto> getAllEnderecosByUserId(UUID userId) {
                // Verifica se o usuário existe e lida com a exceção, se necessário.
                usuarioRepository.findById(userId)
                                .orElseThrow(() -> new UsuarioNotFoundException("user.not.found"));

                // Recupera todos os endereços associados ao usuário
                return enderecoRepository.findAllByUsuario_IdUsuario(userId).stream()
                                .filter(endereco -> endereco.getUsuario().getIdUsuario().equals(userId)) // Verifica a
                                                                                                         // associação
                                                                                                         // com o //
                                                                                                         // usuário

                                .map(endereco -> new EnderecoDto(
                                                endereco.getRua(),
                                                endereco.getNumero(),
                                                endereco.getCidade(),
                                                endereco.getEstado(),
                                                endereco.getCep()))
                                .collect(Collectors.toList());
        }

        /**
         * Atualiza um endereço existente associado ao usuário especificado.
         * 
         * @param idEndereco          O ID do endereço a ser atualizado.
         * @param enderecoDto O DTO contendo os dados atualizados do endereço.
         * @param userId      O ID do usuário ao qual o endereço deve estar associado.
         * @return O DTO do endereço atualizado.
         * @throws EnderecoNotFoundException Se o endereço não for encontrado ou não
         *                                   pertencer ao usuário.
         */
        @Override
        public EnderecoDto updateEndereco(UUID idEndereco, EnderecoDto enderecoDto, UUID userId) {
                Endereco endereco = enderecoRepository.findById(idEndereco)
                                .filter(e -> e.getUsuario().getIdUsuario().equals(userId)) // Verifica a associação com
                                                                                           // o usuário
                                .orElseThrow(() -> new EnderecoNotFoundException("endereco.not.found"));

                endereco.setRua(enderecoDto.rua());
                endereco.setNumero(enderecoDto.numero());
                endereco.setCidade(enderecoDto.cidade());
                endereco.setEstado(enderecoDto.estado());
                endereco.setCep(enderecoDto.cep());
                Endereco updatedEndereco = enderecoRepository.save(endereco);
                return new EnderecoDto(updatedEndereco.getRua(), updatedEndereco.getNumero(),
                                updatedEndereco.getCidade(),
                                updatedEndereco.getEstado(), updatedEndereco.getCep());
        }

        /**
         * Exclui um endereço existente associado ao usuário especificado.
         * 
         * @param idEndereco     O ID do endereço a ser excluído.
         * @param userId O ID do usuário ao qual o endereço deve estar associado.
         * @throws EnderecoNotFoundException Se o endereço não for encontrado ou não
         *                                   pertencer ao usuário.
         */
        @Override
        public void deleteEndereco(UUID idEndereco, UUID userId) {
                Endereco endereco = enderecoRepository.findById(idEndereco)
                                .filter(e -> e.getUsuario().getIdUsuario().equals(userId)) // Verifica a associação com
                                                                                           // o usuário
                                .orElseThrow(() -> new EnderecoNotFoundException("endereco.not.found"));
                enderecoRepository.delete(endereco);
        }
}
