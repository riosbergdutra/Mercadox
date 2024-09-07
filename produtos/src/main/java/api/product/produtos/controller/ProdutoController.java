package api.product.produtos.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import api.product.produtos.dtos.ProdutosDto.ProdutoDtoResponse;
import api.product.produtos.service.ProdutoService;

@RestController
@RequestMapping("/produtos")
public class ProdutoController {

    @Autowired
    private ProdutoService produtoService;

    @GetMapping("/findall")
    public ResponseEntity<List<ProdutoDtoResponse>> getAllProducts() {
        List<ProdutoDtoResponse> produtos = produtoService.FindAllProducts();
        return new ResponseEntity<>(produtos, HttpStatus.OK);
    }
}
