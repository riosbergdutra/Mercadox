package user.api.usuario.usuario.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import user.api.usuario.usuario.dtos.EnderecoDto.EnderecoDto;
import user.api.usuario.usuario.dtos.enderecosemid.EnderecoSemId;

public interface EnderecoService {
    public EnderecoSemId saveEndereco(EnderecoSemId enderecoDto, UUID userId);

    public Optional<EnderecoDto> getEnderecoById(UUID idEndereco, UUID userId);

    public List<EnderecoDto> getAllEnderecosByUserId(UUID userId);

    public EnderecoSemId updateEndereco(UUID idEndereco, EnderecoSemId enderecoDto, UUID userId);
    
    public void deleteEndereco(UUID idEndereco, UUID userId);
}
