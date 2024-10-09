package cart.api.carrinho.service;

import org.springframework.stereotype.Component;

import io.awspring.cloud.sqs.annotation.SqsListener;

@Component
public class CarrinhoSqsListener {
    private final CarrinhoService carrinhoService;

    public CarrinhoSqsListener(CarrinhoService carrinhoService) {
        this.carrinhoService = carrinhoService;
    }

    @SqsListener("usuario")
    public void receberMensagemSQS(String mensagemSQS) {
        try {
            // Aqui você pode processar a mensagem SQS como uma String simples
            System.out.println("Mensagem SQS recebida: " + mensagemSQS);

            // Você pode chamar o serviço de conta bancária diretamente ou converter a String para o formato desejado, se necessário
            carrinhoService.processarMensagemSQS(mensagemSQS);
        } catch (Exception e) {
            System.err.println("Erro ao processar mensagem SQS: " + e.getMessage());

            e.printStackTrace();
        }
    }
}
