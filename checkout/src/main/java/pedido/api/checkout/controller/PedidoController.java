package pedido.api.checkout.controller;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import pedido.api.checkout.dto.pedido.CriarPedidoRequestDto;
import pedido.api.checkout.dto.pedido.CriarPedidoResponseDto;
import pedido.api.checkout.service.PedidoService;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {
    @Autowired
    PedidoService pedidoService;

     @PostMapping("/{idUsuario}/criarpedido")
    public ResponseEntity<CriarPedidoResponseDto> criarPedido(
            @PathVariable("idUsuario") UUID idUsuario,
            @ModelAttribute @Valid CriarPedidoRequestDto pedidoRequest,
            Authentication authentication) {
        UUID userId = getUserIdFromAuthentication(authentication);
        CriarPedidoResponseDto resposta = pedidoService.criarPedido(pedidoRequest, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }

    private UUID getUserIdFromAuthentication(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }
}