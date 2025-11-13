# Base image con Java 21
FROM mcr.microsoft.com/devcontainers/java:1-21-bullseye

# Instalar herramientas adicionales
RUN apt-get update && export DEBIAN_FRONTEND=noninteractive \
    && apt-get -y install --no-install-recommends \
        postgresql-client \
        curl \
        wget \
        git \
        vim \
        htop \
    && apt-get clean -y && rm -rf /var/lib/apt/lists/*

# Instalar Maven 3.9
ARG MAVEN_VERSION=3.9.6
ARG MAVEN_DOWNLOAD_URL=https://dlcdn.apache.org/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz

RUN mkdir -p /usr/share/maven /usr/share/maven/ref \
    && curl -fsSL -o /tmp/apache-maven.tar.gz ${MAVEN_DOWNLOAD_URL} \
    && tar -xzf /tmp/apache-maven.tar.gz -C /usr/share/maven --strip-components=1 \
    && rm -f /tmp/apache-maven.tar.gz \
    && ln -s /usr/share/maven/bin/mvn /usr/bin/mvn

ENV MAVEN_HOME /usr/share/maven
ENV MAVEN_CONFIG /home/vscode/.m2

# Crear usuario vscode si no existe
RUN if ! id -u vscode > /dev/null 2>&1; then \
        groupadd --gid 1000 vscode \
        && useradd --uid 1000 --gid vscode --shell /bin/bash --create-home vscode; \
    fi

# Configurar Git para el usuario vscode
USER vscode
RUN git config --global core.autocrlf input \
    && git config --global init.defaultBranch main

# Volver a root para instalaciones finales
USER root

# Instalar extensiones útiles de CLI
RUN curl -fsSL https://cli.github.com/packages/githubcli-archive-keyring.gpg | dd of=/usr/share/keyrings/githubcli-archive-keyring.gpg \
    && chmod go+r /usr/share/keyrings/githubcli-archive-keyring.gpg \
    && echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/githubcli-archive-keyring.gpg] https://cli.github.com/packages stable main" | tee /etc/apt/sources.list.d/github-cli.list > /dev/null \
    && apt-get update \
    && apt-get install -y gh \
    && apt-get clean -y && rm -rf /var/lib/apt/lists/*

USER vscode
