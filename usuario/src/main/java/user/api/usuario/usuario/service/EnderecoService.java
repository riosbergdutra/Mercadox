package user.api.usuario.usuario.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import user.api.usuario.usuario.dtos.EnderecoDto.EnderecoDto;

public interface EnderecoService {
    public EnderecoDto saveEndereco(EnderecoDto enderecoDto, UUID userId);

    public Optional<EnderecoDto> getEnderecoById(UUID idEndereco, UUID userId);

    List<EnderecoDto> getAllEnderecosByUserId(UUID userId);

    public EnderecoDto updateEndereco(UUID idEndereco, EnderecoDto enderecoDto, UUID userId);

    public void deleteEndereco(UUID idEndereco, UUID userId);
}
