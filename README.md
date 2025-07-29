# 🚀 Forum Hub Alura

Um backend RESTful desenvolvido com Spring Boot na formação #ALura e #oraclenexteducation, projetado para servir como a base para uma plataforma de fórum online. Este projeto permite que usuários se registrem, autentiquem, criem e gerenciem tópicos (posts), adicionem comentários, adicione curtida em posts, e administradores gerenciem cursos.

## ✨ Funcionalidades

* **Autenticação de Usuário**: Registro e Login de usuários utilizando JWT (JSON Web Tokens) para segurança.
* **Gerenciamento de Usuários**: Criação e atualização de perfis de usuário.
* **Gerenciamento de Tópicos (Posts)**:
    * Criação, leitura, atualização e exclusão (CRUD) de posts.
    * Listagem de todos os posts ou posts por ID.
    * Marcação de posts como "solucionado".
* **Gerenciamento de Comentários**:
    * Adição, atualização e exclusão de comentários em posts específicos.
    * Listagem de comentários por post.
    * Permissões para exclusão/atualização de comentários (apenas autor ou ADMIN).
* **Likes em Posts**: Funcionalidade para marcar "gostei" em posts.
* **Gerenciamento de Cursos**:
    * CRUD de cursos.
    * Apenas usuários com a role `ADMIN` podem criar, atualizar e excluir cursos.
* **Validação de Dados**: Validação robusta de entradas utilizando Jakarta Validation (Bean Validation).
* **Autorização Baseada em Roles**: Controle de acesso através de Spring Security com roles de usuário (USER, ADMIN).
* **Tratamento Global de Exceções**: Retorno de mensagens de erro claras e status HTTP apropriados para validações e regras de negócio.
* **Documentação Interativa da API**: Swagger UI integrado para explorar e testar os endpoints da API.

## 🛠️ Tecnologias Utilizadas

* **Java 17+**
* **Spring Boot 3.3.1+**
* **Spring Security**: Autenticação e Autorização.
* **Spring Data JPA**: Abstração e persistência de dados.
* **JWT (JSON Web Tokens)**: Para autenticação sem estado.
* **MySQL**: Banco de dados relacional principal.
* **Maven**: Gerenciador de dependências do projeto.
* **Lombok**: Para reduzir código boilerplate (getters, setters, construtores).
* **Jakarta Validation**: Validação de dados de entrada.
* **ModelMapper**: Para mapeamento entre entidades e DTOs.
* **OpenAPI (Swagger UI)**: Documentação interativa da API.
* **SLF4j + Logback**: Sistema de logging.
* **JUnit 5 & Mockito**: Para testes unitários.

## ⚙️ Como Rodar o Projeto Localmente

### Pré-requisitos

* Java Development Kit (JDK) 17 ou superior
* Maven 3.6+
* Um IDE (IntelliJ IDEA, VS Code, Eclipse)
* **MySQL Server** (versão 8.0+ recomendada) ou **Docker** para rodar o MySQL facilmente.

### Configuração do Banco de Dados

O projeto está configurado para usar MySQL. Você precisará garantir que um servidor MySQL esteja em execução e que o banco de dados e as credenciais estejam configurados corretamente.

#### Opção 1: Usando Docker (Recomendado para Desenvolvimento)

A maneira mais fácil de configurar o MySQL é usando Docker.

1.  **Instale o Docker** (se ainda não tiver).
2.  **Execute o seguinte comando no terminal** para iniciar um contêiner MySQL:
    ```bash
    docker run --name mysql-forumhub -e MYSQL_ROOT_PASSWORD=password -e MYSQL_DATABASE=forumdb -e MYSQL_USER=forumuser -e MYSQL_PASSWORD=forumpass -p 3306:3306 -d mysql:8.0
    ```
    * Este comando cria um banco de dados `forumdb` com o usuário `forumuser` e senha `forumpass`.
    * A porta `3306` do contêiner é mapeada para a porta `3306` da sua máquina local.

#### Opção 2: Usando uma Instalação Local do MySQL

Certifique-se de ter um servidor MySQL instalado e funcionando. Você precisará criar um banco de dados (ex: `forumdb`) e um usuário (ex: `user` com senha `password`) com permissões para esse banco de dados.

#### Configuração no `application.properties`

Abra seu arquivo `src/main/resources/application.properties` e verifique/modifique as seguintes linhas para o MySQL:

```properties

# Configurações do Banco de Dados MySQL
spring.datasource.url=jdbc:mysql://localhost:3306/forumdb?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
spring.datasource.username=${MYSQL_USERNAME:root} # Use :root como valor padrão, se a variável não for definida nas variaveis de ambiente
spring.datasource.password=${MYSQL_PASSWORD:admin} # Use :admin como valor padrão, se a variável não for definida nas variaveis de ambiente

# Driver do MySQL
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# Configurações do JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update # Ou 'create' para recriar as tabelas a cada inicialização (cuidado com dados)
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.jdbc.lob.non_contextual_creation=true
````

**Observações:**

  * `createDatabaseIfNotExist=true`: O Spring tentará criar o banco de dados `forumdb` se ele não existir. Remova esta opção em ambientes de produção.
  * `useSSL=false`: Desabilita SSL. Para produção, é altamente recomendado configurar e usar SSL.
  * `serverTimezone=UTC`: É importante configurar o fuso horário para evitar problemas de data/hora.

### Passos para Rodar

1.  **Clone o repositório:**
    ```bash
    git clone [https://github.com/arycomputer/forum_hub_alura.git](https://github.com/arycomputer/forum_hub_alura.git)
    cd forum_hub_alura
    ```
2.  **Compile o projeto com Maven:**
    ```bash
    mvn clean install
    ```
3.  **Execute a aplicação:**
    Você pode rodar diretamente pelo seu IDE ou via linha de comando:
    ```bash
    mvn spring-boot:run
    ```
    A aplicação estará disponível em `http://localhost:8080`.

## 📖 Documentação da API (Swagger UI)

O Swagger UI (também conhecido como OpenAPI UI) é uma ferramenta poderosa que facilita a documentação e a interação com APIs RESTful. No contexto do seu projeto Forum Hub, ele oferece as seguintes funcionalidades principais:

  * **Documentação Interativa e Visual:** Gera uma documentação visual e interativa de todos os seus endpoints da API (GET, POST, PUT, DELETE, etc.). Você pode ver todos os caminhos, os métodos HTTP associados e suas descrições.
  * **Detalhes Abrangentes dos Endpoints:** Para cada endpoint, o Swagger UI exibe informações detalhadas como:
      * **Parâmetros de Requisição:** Quais parâmetros de caminho (`@PathVariable`), parâmetros de consulta (`@RequestParam`) ou corpos de requisição (`@RequestBody`) o endpoint espera, incluindo seus tipos de dados e se são obrigatórios.
      * **Modelos de Dados (Schemas):** Mostra a estrutura dos DTOs (Data Transfer Objects) esperados nos corpos de requisição e nas respostas, tornando claro qual formato de JSON é necessário ou será retornado.
      * **Códigos de Resposta HTTP:** Lista os possíveis códigos de status HTTP que o endpoint pode retornar (ex: 200 OK, 201 Created, 400 Bad Request, 401 Unauthorized, 403 Forbidden, 404 Not Found, 409 Conflict) e suas respectivas descrições, incluindo o formato do corpo da resposta para cada status.
  * **Testes Diretos da API:** Uma das funcionalidades mais úteis é a capacidade de "experimentar" os endpoints diretamente na interface do navegador. Você pode preencher os parâmetros e corpos de requisição e enviar a solicitação, vendo a resposta do servidor em tempo real. Isso é excelente para testar sua API sem precisar de ferramentas externas como Postman ou Insomnia.
  * **Geração Automática:** Com as anotações do Spring Boot e Swagger (como `@Operation`, `@ApiResponses`, `@Tag`, `@Schema`, etc.), a documentação é gerada automaticamente a partir do seu código-fonte, o que ajuda a mantê-la atualizada com o desenvolvimento da API.

Após iniciar a aplicação, você pode acessar a documentação interativa da API no seu navegador:
[http://localhost:8080/swagger-ui.html](https://www.google.com/search?q=http://localhost:8080/swagger-ui.html)

## 🔑 Endpoints da API

Aqui estão alguns dos principais endpoints da API. Para detalhes completos, consulte o Swagger UI.

### 1\. Autenticação e Registro

  * **Registro de Usuário**
      * `POST /auth/register`
        ```json
        {
          "email": "novo.usuario@example.com",
          "password": "SenhaSegura123!"
        }
        ```
        Status: `201 Created`
  * **Login de Usuário**
      * `POST /auth/login`
        ```json
        {
          "email": "usuario@example.com",
          "password": "SuaSenha123!"
        }
        ```
        Status: `200 OK`
        Resposta:
        ```json
        {
          "jwtToken": "eyJhbGciOiJIUzI1Ni...",
          "refreshToken": "eyJhbGciOiJIUzI1Ni..."
        }
        ```

### 2\. Tópicos (Posts)

  * **Listar Todos os Posts**
      * `GET /posts`
        Status: `200 OK`
  * **Criar Novo Post**
      * `POST /posts` (Requer token JWT)
        ```json
        {
          "title": "Dúvida sobre Spring Security",
          "content": "Estou com dificuldades para configurar o JWT...",
          "courseId": 1
        }
        ```
        Status: `201 Created`
  * **Obter Post por ID**
      * `GET /posts/{id}`
        Status: `200 OK` (ou `404 Not Found`)
  * **Atualizar Post**
      * `PUT /posts/{id}` (Requer token JWT, apenas autor do post ou ADMIN)
        ```json
        {
          "title": "Dúvida sobre Spring Security (Resolvida)",
          "content": "Consegui resolver o problema seguindo o tutorial X."
        }
        ```
        Status: `200 OK` (ou `403 Forbidden`, `404 Not Found`)
  * **Excluir Post**
      * `DELETE /posts/{id}` (Requer token JWT, apenas autor do post ou ADMIN)
        Status: `204 No Content` (ou `403 Forbidden`, `404 Not Found`)
  * **Marcar Post como Solucionado**
      * `PUT /posts/{id}/solved` (Requer token JWT, apenas autor do post ou ADMIN)
        Status: `200 OK` (ou `403 Forbidden`, `404 Not Found`)
  * **Dar Like em Post**
      * `POST /posts/{id}/like` (Requer token JWT)
        Status: `200 OK` (ou `400 Bad Request` se já deu like/dislike)

### 3\. Comentários

  * **Listar Comentários de um Post**
      * `GET /posts/{postId}/comments`
        Status: `200 OK`
  * **Adicionar Comentário**
      * `POST /posts/{postId}/comments` (Requer token JWT)
        ```json
        {
          "content": "Ótima dúvida! Tive o mesmo problema."
        }
        ```
        Status: `201 Created`
  * **Atualizar Comentário**
      * `PUT /posts/{postId}/comments/{commentId}` (Requer token JWT, apenas autor do comentário ou ADMIN)
        ```json
        {
          "content": "Realmente, a solução foi essa!"
        }
        ```
        Status: `200 OK` (ou `403 Forbidden`, `404 Not Found`)
  * **Excluir Comentário**
      * `DELETE /posts/{postId}/comments/{commentId}` (Requer token JWT, apenas autor do comentário ou ADMIN)
        Status: `204 No Content` (ou `403 Forbidden`, `404 Not Found`)

### 4\. Cursos (Apenas ADMIN)

  * **Listar Todos os Cursos**
      * `GET /courses`
        Status: `200 OK`
  * **Criar Novo Curso**
      * `POST /courses` (Requer token JWT de ADMIN)
        ```json
        {
          "name": "Spring Boot Avançado"
        }
        ```
        Status: `201 Created`
  * **Atualizar Curso**
      * `PUT /courses/{id}` (Requer token JWT de ADMIN)
        ```json
        {
          "name": "Spring Boot Masterclass"
        }
        ```
        Status: `200 OK` (ou `403 Forbidden`, `404 Not Found`, `409 Conflict`)

## 🤝 Contribuindo

Contribuições são bem-vindas\! Se você deseja contribuir com o projeto, por favor, siga estes passos:

1.  Faça um fork do repositório.
2.  Crie uma nova branch (`git checkout -b feature/sua-feature`).
3.  Faça suas alterações e commit-as (`git commit -am 'feat: Adiciona nova funcionalidade X'`).
4.  Envie para a branch (`git push origin feature/sua-feature`).
5.  Abra um Pull Request.

## 📄 Licença

Este projeto está licenciado sob a licença MIT. Veja o arquivo [LICENSE](https://www.google.com/search?q=LICENSE) para mais detalhes.

## 🧑‍💻 Autor

[Ary Augusto / @arycomputer](https://www.google.com/search?q=https://github.com/arycomputer)

```
```
