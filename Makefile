# ====================================
# Makefile - Terrenos SaaS
# ====================================

.PHONY: help backend-build backend-run backend-test backend-clean docker-up docker-down docker-logs db-connect

# Colores para output
GREEN  := \033[0;32m
YELLOW := \033[0;33m
NC     := \033[0m # No Color

# Variables
DOCKER_COMPOSE := docker-compose

##@ General

help: ## Mostrar esta ayuda
	@echo "$(GREEN)Comandos disponibles:$(NC)"
	@awk 'BEGIN {FS = ":.*##"; printf "\n"} /^[a-zA-Z_-]+:.*?##/ { printf "  $(YELLOW)%-20s$(NC) %s\n", $$1, $$2 } /^##@/ { printf "\n$(GREEN)%s$(NC)\n", substr($$0, 5) } ' $(MAKEFILE_LIST)

##@ Backend

backend-build: ## Compilar el backend
	@echo "$(GREEN)🏗️  Compilando backend...$(NC)"
	./mvnw clean package -DskipTests

backend-run: ## Ejecutar el backend
	@echo "$(GREEN)🚀 Ejecutando backend...$(NC)"
	./mvnw spring-boot:run

backend-test: ## Ejecutar tests del backend
	@echo "$(GREEN)🧪 Ejecutando tests...$(NC)"
	./mvnw test

backend-test-integration: ## Ejecutar tests de integración
	@echo "$(GREEN)🧪 Ejecutando tests de integración...$(NC)"
	./mvnw verify

backend-clean: ## Limpiar archivos compilados
	@echo "$(GREEN)🧹 Limpiando...$(NC)"
	./mvnw clean

backend-install: ## Instalar dependencias
	@echo "$(GREEN)📦 Instalando dependencias...$(NC)"
	./mvnw clean install

backend-format: ## Formatear código
	@echo "$(GREEN)✨ Formateando código...$(NC)"
	./mvnw spotless:apply

##@ Docker

docker-up: ## Levantar servicios Docker (PostgreSQL + pgAdmin)
	@echo "$(GREEN)🐳 Levantando servicios Docker...$(NC)"
	$(DOCKER_COMPOSE) up -d

docker-down: ## Detener servicios Docker
	@echo "$(GREEN)🛑 Deteniendo servicios Docker...$(NC)"
	$(DOCKER_COMPOSE) down

docker-logs: ## Ver logs de Docker
	@echo "$(GREEN)📋 Logs de Docker:$(NC)"
	$(DOCKER_COMPOSE) logs -f

docker-ps: ## Ver estado de contenedores
	@echo "$(GREEN)📊 Estado de contenedores:$(NC)"
	$(DOCKER_COMPOSE) ps

docker-clean: ## Limpiar volúmenes de Docker
	@echo "$(YELLOW)⚠️  Limpiando volúmenes de Docker (se perderán los datos)...$(NC)"
	$(DOCKER_COMPOSE) down -v

docker-restart: ## Reiniciar servicios Docker
	@echo "$(GREEN)🔄 Reiniciando servicios Docker...$(NC)"
	$(DOCKER_COMPOSE) restart

##@ Database

db-connect: ## Conectar a PostgreSQL
	@echo "$(GREEN)🗄️  Conectando a PostgreSQL...$(NC)"
	psql -h localhost -p 5432 -U postgres -d terrenos_db

db-migrate: ## Ejecutar migraciones Flyway
	@echo "$(GREEN)📊 Ejecutando migraciones...$(NC)"
	./mvnw flyway:migrate

db-info: ## Ver información de migraciones
	@echo "$(GREEN)ℹ️  Información de migraciones:$(NC)"
	./mvnw flyway:info

db-reset: ## Limpiar y recrear base de datos
	@echo "$(YELLOW)⚠️  Limpiando base de datos...$(NC)"
	./mvnw flyway:clean flyway:migrate

##@ Development

dev: docker-up backend-run ## Iniciar ambiente de desarrollo completo

dev-clean: docker-clean backend-clean ## Limpiar todo el ambiente de desarrollo

check: backend-test ## Ejecutar validaciones (tests, checkstyle)
	@echo "$(GREEN)✅ Validaciones completadas$(NC)"

##@ Git

git-status: ## Ver estado de git de todos los módulos
	@echo "$(GREEN)📊 Estado de Git:$(NC)"
	git status

git-pull: ## Pull de todos los módulos
	@echo "$(GREEN)⬇️  Actualizando desde remote...$(NC)"
	git pull

##@ Utility

clean-all: backend-clean docker-clean ## Limpiar todo (compilados + Docker)
	@echo "$(GREEN)✨ Limpieza completa realizada$(NC)"

install-hooks: ## Instalar git hooks
	@echo "$(GREEN)🪝 Instalando git hooks...$(NC)"
	cp -r .githooks/* .git/hooks/
	chmod +x .git/hooks/*
	@echo "$(GREEN)✅ Hooks instalados$(NC)"
