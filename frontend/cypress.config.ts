import { defineConfig } from "cypress";

export default defineConfig({
  e2e: {
    setupNodeEvents(on, config) {
      // implement node event listeners here
    },
    baseUrl:"http://localhost:4200/",
    // não vai limpar o estado da tela a cada it
    testIsolation: false
  },
});
