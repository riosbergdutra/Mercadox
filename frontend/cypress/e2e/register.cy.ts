describe('Página de Registro', () => {
    beforeEach(() => {
      // Visita a página de registro antes de cada teste
      cy.visit('/register');
    });
  
    it('Deve exibir o formulário de registro', () => {
      // Verifica se os campos de formulário estão visíveis
      cy.get('form').should('exist');
      cy.get('input[id="nome"]').should('be.visible');
      cy.get('input[id="email"]').should('be.visible');
      cy.get('input[id="senha"]').should('be.visible');
      cy.get('select[id="role"]').should('be.visible');
    });
  
    it('Deve desabilitar o botão de registro se o formulário for inválido', () => {
      // Verifica que o botão de submit está desabilitado ao carregar a página
      cy.get('button[type="submit"]').should('be.disabled');
    });
  
    it('Deve habilitar o botão de registro quando o formulário for válido', () => {
      // Preenche o formulário com dados válidos
      cy.get('input[id="nome"]').type('Teste Usuário');
      cy.get('input[id="email"]').type('teste@dominio.com');
      cy.get('input[id="senha"]').type('senha123');
      cy.get('select[id="role"]').select('USUARIO');
  
      // Verifica se o botão de submit é habilitado
      cy.get('button[type="submit"]').should('not.be.disabled');
    });
  
    it('Deve enviar o formulário de registro corretamente', () => {
      // Mockando a resposta da requisição para evitar chamadas reais
      cy.intercept('POST', '/usuario/criar', {
        statusCode: 200,
        body: {},
      }).as('postRegister');
  
      // Preenche o formulário com dados válidos
      cy.get('input[id="nome"]').type('Teste Usuário');
      cy.get('input[id="email"]').type('teste@dominio.com');
      cy.get('input[id="senha"]').type('senha123');
      cy.get('select[id="role"]').select('USUARIO');
  
      // Submete o formulário
      cy.get('button[type="submit"]').click();
  
      // Verifica se a requisição de POST foi feita
      cy.wait('@postRegister').its('request.body').should('deep.equal', {
        nome: 'Teste Usuário',
        email: 'teste@dominio.com',
        senha: 'senha123',
        role: 'USUARIO',
      });
  
      // Verifica se a navegação para a página de login ocorreu
      cy.url().should('include', '/login');
    });
  
    it('Deve exibir uma mensagem de erro se o registro falhar', () => {
      // Mockando a resposta de erro da requisição
      cy.intercept('POST', '/usuario/criar', {
        statusCode: 400,
        body: {
          message: 'Erro ao registrar usuário',
        },
      }).as('postRegisterError');
  
      // Preenche o formulário com dados válidos
      cy.get('input[id="nome"]').type('Teste Usuário');
      cy.get('input[id="email"]').type('teste@dominio.com');
      cy.get('input[id="senha"]').type('senha123');
      cy.get('select[id="role"]').select('USUARIO');
  
      // Submete o formulário
      cy.get('button[type="submit"]').click();
  
      // Verifica se a requisição de POST foi feita
      cy.wait('@postRegisterError');
  
      // Verifica se a mensagem de erro é exibida
      cy.get('.error-message').should('be.visible');
    });
  });
  