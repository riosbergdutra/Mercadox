describe('Login Page Tests', () => {
    beforeEach(() => {
      // Visita a página de login antes de cada teste
      cy.visit('http://localhost:4200/login');
    });
  
    it('should display login form', () => {
      // Verifica se o formulário de login é exibido corretamente
      cy.get('h2').should('contain', 'Login');
      cy.get('form').should('be.visible');
      cy.get('input[id="email"]').should('exist');
      cy.get('input[id="senha"]').should('exist');
      cy.get('button').should('contain', 'Login');
    });
  
    it('should successfully login with valid credentials', () => {
      // Testa login com credenciais válidas
      const validEmail = 'joooao@example.com';
      const validPassword = 'senha123';
  
      // Preenche o formulário com as credenciais válidas
      cy.get('input[id="email"]').type(validEmail);
      cy.get('input[id="senha"]').type(validPassword);
      cy.get('button').click();
  
      // Verifica se o redirecionamento foi para a página inicial (ou dashboard)
      cy.url().should('eq', 'http://localhost:4200/');
    });
  
    it('should show error message with invalid credentials', () => {
      // Testa login com credenciais inválidas
      const invalidEmail = 'usuario@teste.com';
      const invalidPassword = 'senhaErrada';
  
      // Preenche o formulário com as credenciais inválidas
      cy.get('input[id="email"]').type(invalidEmail);
      cy.get('input[id="senha"]').type(invalidPassword);
      cy.get('button').click();
  
      // Verifica se a mensagem de erro é exibida
      cy.get('.error-message').should('be.visible').and('contain', 'Login falhou. Verifique suas credenciais.');
    });
  
    it('should disable login button if form is invalid', () => {
      // Testa se o botão de login está desabilitado com formulário inválido
      cy.get('button').should('be.disabled');
    });
  });
