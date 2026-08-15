# AM Portões - System

Sistema de gestão para instalação e manutenção de portões, com dashboard full-stack.

## Estrutura do repositório

```
Am-Portoes-System/
├── backend/     # API REST (Java 17 + Spring Boot + JPA + H2)
├── frontend/    # Dashboard (Angular + TypeScript)
└── docs/        # Documentação, diagramas, decisões de arquitetura
```

## Backend

Java 17, Spring Boot, Spring Data JPA, H2, Lombok.

```bash
cd backend
./mvnw spring-boot:run
```

## Frontend

Angular.

```bash
cd frontend
npm install
npm start
```

## Fluxo de branches

- `main` — código estável, pronto para produção
- `develop` — integração das features em desenvolvimento
- `feat/*` — uma branch por funcionalidade, criada a partir de `develop` e mergeada de volta via Pull Request

## Convenção de commits

Commits seguem o padrão [Conventional Commits](https://www.conventionalcommits.org/):

- `feat:` nova funcionalidade
- `fix:` correção de bug
- `chore:` tarefas de manutenção (config, dependências, etc.)
- `docs:` documentação
- `refactor:` refatoração sem mudança de comportamento
