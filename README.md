# API de Movimentações Financeiras

API REST em **Java + Spring Boot** para cadastrar receitas e despesas e consultar o saldo de um mês.
Atividade de Sistemas Distribuídos.

**Tecnologias:** Java 17, Spring Boot, Spring Web, Spring Data JPA, Bean Validation, banco H2 (em memória) e Maven.

## Arquitetura

```
controller/  -> MovimentacaoController (endpoints REST)
repository/  -> MovimentacaoRepository (acesso ao banco via Spring Data JPA)
model/       -> Movimentacao (entidade) e TipoMovimentacao (enum RECEITA | DESPESA)
dto/         -> SaldoMensal (resposta do relatório)
exception/   -> tratamento de erros (404 / 400)
```

O saldo **não é armazenado**: é calculado a cada consulta somando receitas e despesas do mês pelo repository.

## Como executar

Requisitos: **JDK 17+** e acesso à internet na primeira execução (download das dependências).

```bash
# Linux / macOS
./mvnw spring-boot:run

# Windows
mvnw.cmd spring-boot:run

# ou, com Maven instalado
mvn spring-boot:run
```

A API sobe em `http://localhost:8080`. O console do H2 fica em `http://localhost:8080/h2-console`
(JDBC URL: `jdbc:h2:mem:financas`, usuário `sa`, senha vazia). Os dados são perdidos ao reiniciar.

## Entidade `Movimentacao`

| Campo       | Tipo                   | Regra                              |
|-------------|------------------------|------------------------------------|
| `id`        | Long                   | gerado automaticamente             |
| `descricao` | String                 | obrigatória (não vazia)            |
| `valor`     | Decimal                | obrigatório, **maior que zero**    |
| `tipo`      | Enum `RECEITA`/`DESPESA` | obrigatório                      |
| `data`      | Data (`yyyy-MM-dd`)    | obrigatória                        |
| `categoria` | String                 | opcional                           |

## Endpoints

| Método | Rota                                | Descrição                                   | Sucesso |
|--------|-------------------------------------|---------------------------------------------|---------|
| POST   | `/movimentacoes`                    | Cadastra uma movimentação                   | 201     |
| GET    | `/movimentacoes`                    | Lista todas (filtros opcionais `tipo`, `categoria`) | 200 |
| GET    | `/movimentacoes/{id}`               | Busca por id                                | 200     |
| PUT    | `/movimentacoes/{id}`               | Altera uma movimentação                     | 200     |
| DELETE | `/movimentacoes/{id}`               | Exclui uma movimentação                     | 204     |
| GET    | `/movimentacoes/saldo?mes=yyyy-MM`  | Relatório de saldo do mês                   | 200     |

Erros: `400` (dados inválidos) e `404` (id inexistente).

## Exemplos

### Cadastrar (POST /movimentacoes)

Requisição:
```json
{
  "descricao": "Salário",
  "valor": 5000.00,
  "tipo": "RECEITA",
  "data": "2026-09-01",
  "categoria": "Trabalho"
}
```
Resposta `201 Created`:
```json
{
  "id": 1,
  "descricao": "Salário",
  "valor": 5000.00,
  "tipo": "RECEITA",
  "data": "2026-09-01",
  "categoria": "Trabalho"
}
```

### Listar (GET /movimentacoes)

Filtros opcionais: `/movimentacoes?tipo=DESPESA`, `/movimentacoes?categoria=Trabalho`.

Resposta `200 OK`:
```json
[
  { "id": 1, "descricao": "Salário", "valor": 5000.00, "tipo": "RECEITA", "data": "2026-09-01", "categoria": "Trabalho" },
  { "id": 2, "descricao": "Aluguel", "valor": 1500.00, "tipo": "DESPESA", "data": "2026-09-05", "categoria": "Moradia" }
]
```

### Buscar por id (GET /movimentacoes/1)

Resposta `200 OK`: o objeto da movimentação. Para id inexistente, `404 Not Found`:
```json
{
  "timestamp": "2026-09-30T21:00:00Z",
  "status": 404,
  "erro": "Not Found",
  "mensagem": "Movimentação não encontrada: id 99"
}
```

### Alterar (PUT /movimentacoes/2)

Requisição:
```json
{
  "descricao": "Aluguel apartamento",
  "valor": 1600.00,
  "tipo": "DESPESA",
  "data": "2026-09-05",
  "categoria": "Moradia"
}
```
Resposta `200 OK`: a movimentação atualizada.

### Excluir (DELETE /movimentacoes/2)

Resposta `204 No Content` (sem corpo).

### Saldo mensal (GET /movimentacoes/saldo?mes=2026-09)

Resposta `200 OK`:
```json
{
  "mes": "2026-09",
  "totalReceitas": 5000.00,
  "totalDespesas": 1600.00,
  "saldo": 3400.00
}
```

### Validação (POST com valor inválido)

Requisição: `{ "descricao": "", "valor": -10, "tipo": "RECEITA", "data": "2026-09-01" }`

Resposta `400 Bad Request`:
```json
{
  "timestamp": "2026-09-30T21:00:00Z",
  "status": 400,
  "erro": "Bad Request",
  "mensagem": "Dados inválidos",
  "campos": {
    "descricao": "A descrição é obrigatória",
    "valor": "O valor deve ser maior que zero"
  }
}
```

Um `tipo` diferente de `RECEITA`/`DESPESA` ou uma data em formato errado também retorna `400`.

## Testando

O arquivo [`requests.http`](requests.http) contém todas as requisições acima (funciona no IntelliJ e no VS Code com a
extensão REST Client). Também podem ser reproduzidas no Postman ou Insomnia.
