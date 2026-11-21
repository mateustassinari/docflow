workspace "DocFlow" "Gestão de contratos, eventos e tarefas para negócios de arte em eventos." {

    configuration {
        scope softwaresystem
    }

    properties {
        # Documentação e ADRs ficam no site MkDocs (docs/*.md e docs/adr), não dentro do modelo
        "structurizr.inspection.model.softwaresystem.documentation" "ignore"
        "structurizr.inspection.model.softwaresystem.decisions" "ignore"
    }

    model {
        usuaria = person "Dona do negócio" "Cadastra clientes e eventos, gera e envia contratos, acompanha tarefas e o financeiro."
        cliente = person "Cliente" "Contrata o serviço e assina o contrato pelo Autentique." "Externo"

        autentique = softwareSystem "Autentique" "Assinatura eletrônica de documentos." "Externo"
        email = softwareSystem "Servidor de e-mail" "Entrega os avisos e lembretes (SMTP, como o Gmail)." "Externo"
        ollama = softwareSystem "Ollama" "Modelo de linguagem rodando na própria máquina. Opcional." "Externo"

        docflow = softwareSystem "DocFlow" "Do contrato à entrega: gera contratos em PDF, envia para assinatura e cria as tarefas do evento quando o contrato é assinado." {

            spa = container "Aplicação web" "Telas de eventos, contratos, tarefas (lista, kanban e calendário), financeiro e templates." "React 19, Vite" "Navegador"

            proxy = container "Servidor web" "Entrega os arquivos do frontend e repassa /api para a API, na mesma origem. Limita tentativas de login e aplica os cabeçalhos de segurança." "nginx"

            api = container "API" "Regras de negócio, geração de PDFs, integrações e tarefas agendadas. Monolito em camadas." "Java 25, Spring Boot 4" {

                group "presentation" {
                    seguranca = component "Segurança" "Login com sessão no servidor, proteção CSRF e respostas 401/403 em JSON." "Spring Security"
                    controllers = component "Controllers REST" "Recebem as requisições, validam os DTOs e chamam a camada de aplicação." "Spring MVC"
                }

                group "application" {
                    contratos = component "Casos de uso de contrato" "Gerar o PDF, enviar para assinatura, sincronizar o status e guardar o PDF assinado." "Java"
                    automacao = component "Automação de tarefas" "Cria as tarefas do evento quando o contrato é assinado, com prazos relativos à data do evento ou da assinatura." "Java"
                    tarefas = component "Serviço de tarefas" "Tarefas, checklist, comentários, etiquetas e anexos." "Java"
                    lembretes = component "Lembretes de prazo" "Seleciona as tarefas que vencem em 7 dias, em 3 dias e no dia, sem repetir avisos." "Java"
                    assistente = component "Assistente de IA" "Monta o contexto do negócio e conversa com o modelo." "Java"
                }

                group "domain" {
                    dominio = component "Domínio" "Cliente, Evento, Contrato, Tarefa e suas regras, mais as portas (interfaces) que a aplicação usa para falar com o mundo externo. Não depende de Spring nem de JPA." "Java puro"
                }

                group "infrastructure" {
                    agendadores = component "Agendadores" "Verificam os contratos pendentes a cada 30 minutos e disparam os lembretes às 8h." "Spring @Scheduled"
                    persistencia = component "Persistência" "Implementa os repositórios do domínio." "Spring Data JPA, Hibernate"
                    pdf = component "Geradores de PDF" "Contrato a partir do template do tipo de serviço e agenda de tarefas (mês, semana e dia)." "OpenPDF"
                    arquivos = component "Armazenamento de arquivos" "Salva os PDFs e os anexos com nome gerado pelo servidor." "Sistema de arquivos"
                    clienteAutentique = component "Cliente do Autentique" "Envia o contrato, consulta as assinaturas e baixa o PDF assinado." "RestClient, GraphQL"
                    notificacoes = component "Notificações por e-mail" "Aviso de contrato assinado e lembretes de prazo." "Spring Mail"
                    clienteOllama = component "Cliente do Ollama" "Envia o contexto e a pergunta ao modelo local." "RestClient"
                }
            }

            db = container "Banco de dados" "Clientes, eventos, contratos, templates e tarefas. Esquema versionado com Flyway." "PostgreSQL 16" "Banco"
            uploads = container "Arquivos" "PDFs dos contratos e anexos das tarefas." "Volume Docker" "Pasta"
        }

        # Pessoas e sistemas externos
        usuaria -> spa "Usa pelo navegador" "Navegador"
        cliente -> autentique "Assina o contrato" "Navegador"
        autentique -> cliente "Envia o contrato para assinatura" "E-mail"
        email -> usuaria "Entrega avisos de contrato assinado e lembretes de prazo" "E-mail"

        # Frontend até a API
        spa -> proxy "Chama a API em /api, na mesma origem" "JSON, cookie de sessão"
        proxy -> seguranca "Repassa as chamadas /api" "HTTP"
        seguranca -> controllers "Libera as requisições autenticadas" "Filtros do Spring Security"

        # Presentation -> application
        controllers -> contratos "Gerar, enviar e revisar contratos" "Chamada Java"
        controllers -> tarefas "Criar e atualizar tarefas" "Chamada Java"
        controllers -> assistente "Perguntas e insights" "Chamada Java"
        controllers -> pdf "Gera o PDF da agenda de tarefas" "porta TaskCalendarDocumentGenerator"

        # Agendadores
        agendadores -> contratos "Verifica os contratos aguardando assinatura" "A cada 30 minutos"
        agendadores -> lembretes "Dispara os lembretes do dia" "Diariamente às 8h"

        # Application -> domain
        contratos -> dominio "Aplica as regras do contrato" "Chamada Java"
        automacao -> dominio "Calcula os prazos das tarefas" "Chamada Java"
        tarefas -> dominio "Aplica as regras da tarefa" "Chamada Java"

        # Application -> infrastructure (pelas portas do domínio)
        contratos -> pdf "Gera o PDF do contrato" "porta ContractDocumentGenerator"
        contratos -> arquivos "Guarda o PDF gerado e o assinado" "porta ContractFileStorage"
        contratos -> clienteAutentique "Envia para assinatura e baixa o PDF assinado" "porta SignatureService"
        contratos -> persistencia "Lê e salva contratos e eventos" "portas de repositório"
        contratos -> automacao "Pede as tarefas do evento quando o contrato é assinado" "Chamada Java"
        contratos -> notificacoes "Avisa que o contrato foi assinado" "porta NotificationService"
        automacao -> persistencia "Salva as tarefas criadas" "porta TaskRepository"
        tarefas -> persistencia "Lê e salva tarefas" "porta TaskRepository"
        tarefas -> arquivos "Guarda os anexos" "porta TaskAttachmentStorage"
        tarefas -> automacao "Cria as tarefas encadeadas (ex.: Moldura e Caixa)" "Chamada Java"
        lembretes -> persistencia "Busca tarefas por prazo e registra os avisos enviados" "portas de repositório"
        lembretes -> notificacoes "Envia os lembretes" "porta NotificationService"
        assistente -> persistencia "Lê os dados do negócio para o contexto" "portas de repositório"
        assistente -> clienteOllama "Envia contexto e pergunta" "porta AiAssistantService"

        # Infrastructure -> mundo externo
        persistencia -> dominio "Converte entidades JPA em objetos do domínio" "Chamada Java"
        persistencia -> db "Lê e grava" "JDBC"
        arquivos -> uploads "Lê e grava arquivos" "Sistema de arquivos"
        clienteAutentique -> autentique "Envia contratos e consulta assinaturas" "GraphQL, HTTPS"
        notificacoes -> email "Envia os e-mails" "SMTP"
        clienteOllama -> ollama "Gera respostas" "HTTP"

        live = deploymentEnvironment "Docker Compose" {
            deploymentNode "Computador" "Máquina de quem usa o sistema ou roda a demo." "Linux, Windows (WSL) ou macOS" {
                deploymentNode "Navegador" "Roda a aplicação React; guarda o cookie de sessão." "Chrome, Firefox, Edge" {
                    containerInstance spa
                }
                deploymentNode "Docker" "Rede interna do compose: só o frontend é publicado." "Docker Compose" {
                    deploymentNode "frontend" "Publicado só em 127.0.0.1:8081 (BIND_ADDRESS abre para a rede)." "nginx:1.30-alpine" {
                        containerInstance proxy
                    }
                    deploymentNode "backend" "Não publicado. Roda sem root; só escreve na pasta de uploads." "eclipse-temurin:25-jre-alpine" {
                        containerInstance api
                    }
                    deploymentNode "db" "Não publicado." "postgres:16-alpine" {
                        containerInstance db
                    }
                    deploymentNode "volumes" "Sobrevivem a reinícios; docker compose down -v apaga." "Volumes Docker" {
                        containerInstance uploads
                    }
                }
                deploymentNode "Ollama local" "Opcional, acessado por host.docker.internal." "Ollama" {
                    softwareSystemInstance ollama
                }
            }
            deploymentNode "Internet" "Serviços externos, acessados pela API." "Serviços externos" {
                softwareSystemInstance autentique
                softwareSystemInstance email
            }
        }
    }

    views {
        systemContext docflow "Contexto" "Quem usa o DocFlow e com quais sistemas ele conversa." {
            include * cliente
            autolayout lr
        }

        container docflow "Containers" "As peças que rodam separadas e como se comunicam." {
            include *
            exclude "email -> usuaria"
            autolayout lr
        }

        component api "ComponentesContratos" "API por dentro: o caminho do contrato, da geração à assinatura." {
            include seguranca controllers contratos automacao dominio agendadores persistencia pdf arquivos clienteAutentique notificacoes
            include proxy autentique email db uploads
            autolayout lr
        }

        component api "ComponentesTarefas" "API por dentro: tarefas, anexos, agenda em PDF e lembretes de prazo." {
            include controllers tarefas automacao lembretes dominio agendadores persistencia pdf arquivos notificacoes
            include email db uploads
            autolayout lr
        }

        component api "ComponentesIA" "API por dentro: o assistente de IA, opcional, com modelo local." {
            include controllers assistente persistencia clienteOllama
            include ollama db
            autolayout lr
        }

        dynamic api "FluxoDoContrato" "Do contrato assinado às tarefas do evento: o que acontece quando o agendador encontra uma assinatura." {
            agendadores -> contratos "Verifica os contratos aguardando assinatura"
            contratos -> clienteAutentique "Consulta o status no Autentique e baixa o PDF assinado"
            clienteAutentique -> autentique "Consulta assinaturas e baixa o PDF"
            contratos -> arquivos "Guarda o PDF assinado"
            contratos -> persistencia "Marca o contrato como assinado"
            contratos -> automacao "Pede as tarefas do evento"
            automacao -> persistencia "Salva as tarefas com prazos calculados"
            contratos -> notificacoes "Avisa que o contrato foi assinado"
            notificacoes -> email "Envia o e-mail"
            properties {
                "plantuml.sequenceDiagram" "true"
            }
        }

        deployment docflow live "Implantacao" "Como o docker compose distribui o sistema e o que fica exposto." {
            include *
            autolayout lr
        }

        properties {
            # Exportação para C4-PlantUML (docs/gerar-diagramas.sh): aplica as cores e formas dos estilos abaixo
            # e não repete o título, que já está na página do site
            "c4plantuml.tags" "true"
            "plantuml.title" "false"
        }

        styles {
            element "Element" {
                color #ffffff
                stroke #3730a3
            }
            element "Person" {
                shape Person
                background #4338ca
            }
            element "Software System" {
                background #4f46e5
            }
            element "Container" {
                background #6366f1
            }
            element "Component" {
                background #818cf8
                color #0f172a
            }
            element "Externo" {
                background #94a3b8
                stroke #475569
                color #0f172a
            }
            element "Banco" {
                shape Cylinder
            }
            element "Pasta" {
                shape Folder
            }
            element "Navegador" {
                shape WebBrowser
            }
            relationship "Relationship" {
                color #475569
                style solid
            }
        }
    }
}
