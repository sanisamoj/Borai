# Borai

Este é um sistema de gerenciamento e armazenamento de eventos, que distingue dois tipos de contas: **Participant** e **Promoter**. 
As contas **Promoter** têm a capacidade de criar e gerenciar eventos, além de desfrutarem de vantagens em termos de visibilidade, 
o que proporciona maior alcance aos seus eventos. Por outro lado, as contas **Participant** podem marcar presença nos eventos, interagir com comentários e 
avaliações, e também avaliar os eventos e seus promotores. Embora as contas *Participant* possam criar eventos, elas não têm o mesmo nível de visibilidade que as contas *Promoter*, 
o que limita o alcance de seus eventos dentro da plataforma.

O sistema ainda utiliza microserviços para a melhor usabilidade, como o [NotifyBot](https://github.com/sanisamoj/NotifyBot) para envio de mensagens
ou notificações para usuários, e o [EventLoggerServer](https://github.com/sanisamoj/EventLoggerServer) para centralização de logs, como erros e avisos.

> *Microserviços foram utilizados, pois os outros projetos do meu repositório utilizam esses serviços em comum.*

## Objetivos iniciais do sistema 💡
Imagine aquela pessoa desligada, que sempre está por fora do que está rolando. O tipo que só descobre que teve um evento incrível *ontem* porque viu as fotos no Instagram *hoje*. 
Aquela pessoa que, por não ser muito ativa nas redes sociais, acaba se sentindo como um satélite perdido na órbita dos encontros sociais. É para essa pessoa que o nosso projeto nasceu. 💡

A ideia foi criar um espaço onde ninguém mais se sinta o "último a saber". Um *sistema de eventos* que funciona como aquele amigo antenado que sabe de tudo, mas sem ser invasivo. 
Ele não vai forçar você a ser descolado nem te bombardear com informações desnecessárias. Em vez disso, ele sugere eventos que combinam com o que você gosta, 
próximos de onde você está, e até ajuda a encontrar pessoas parecidas com você que talvez estejam indo também.

Esse projeto é, no fundo, um empurrãozinho gentil para que os tímidos e os distraídos tenham a chance de se conectar com o mundo ao redor. 
É sobre facilitar o acesso às experiências que podem transformar um dia qualquer em uma lembrança especial. Porque todo mundo merece fazer parte da festa – sem precisar descobrir depois que ela aconteceu. 🎉

## Funcionalidades
- **É possível encontrar e visualizar eventos utilizando filtros pré-determinados, como:**

    - Localização.
    - Horário ou data.
    - Eventos criados por contas específicas.


- **Interação com eventos:**

    - **Marcar presença:** Confirmar participação nos eventos disponíveis.
    - **Visualizar interações de outros usuários:** Ver contas seguidas que marcaram presença no evento.
    - **Comentar:** Publicar comentários nos eventos.


- **Insígnias para Participantes e Promotores**

  - São obtidas através de avaliações ou condições atendidas.


- Nos **eventos** é possível:

  - Visualizar as contas públicas que marcaram presença no evento.
  - Visualizar as contas privadas caso a conta que esteja visualizando seja seguidor mútuo da conta privada.
  - Visualizar informações como, local, data, horário, descrição, fotos, informações do organizador e o tipo de evento.


- **Nas interações de usuários:**

  - Os usuários podem possuir contas privadas e públicas.
  - As contas públicas podem ser visualizadas, e suas presenças ficam disponíveis.
  - As contas privadas não podem ser visualizadas (Apenas informações básicas como nick e imagem de perfil.), apenas seus seguidores aceitos podem visualizar as demais informações.


## Tecnologias e Ferramentas Utilizadas

- Backend: **Ktor (Kotlin)**
- Banco de Dados:
  - **MongoDB** - Para armazenamento de dados sensíveis.
  - **Redis** - Para armazenamento de dados que precisam de acesso mais rápido.
  

## Padrões de Design Utilizados

- **Factory**
  - O projeto utiliza a padrão factory, para auxiliar na criação dos objetos, para respostas de requisições ou para registro no banco de dados por exemplo.
  

- **Injeção de Dependência**
  - A Injeção de Dependência (DI) foi escolhida pois facilita os testes, podendo tanto trocar a implementações de registros, como também na expansão do projeto.


## Para instalação
Para instalar o projeto para testes, utilizaremos o Docker.

- Instale a última versão do **Docker** em sua máquina.
- Instale o **Mongodb** (Verifique na página oficial, ou monte uma imagem com o Docker).
- Instale o **Redis** na sua máquina (Verifique a página oficial, ou monte uma imagem com o Docker).
- Para a autenticação além do uso de E-mail, estou utilizando os bots do projeto [NotifyBot](https://github.com/sanisamoj/NotifyBot)
- Para o registro de erros, estou usando a API [EventLoggerServer](https://github.com/sanisamoj/EventLoggerServer)
- Crie um arquivo **.env** na pasta raiz do projeto, ou adicione um arquivo **.env** manualmente na construção da imagem docker.

```.env
#URL do banco de dados MONGODB
MONGODB_SERVER_URL=mongodb://localhost:27017 #No docker - mongodb://host.docker.internal:27017
#Nome do banco de dados do MONGODB
NAME_DATABASE=Borai
#URL do banco de dados do REDIS
REDIS_SERVER_URL=localhost  #No Docker - host.docker.internal
#Porta do banco de dados do REDIS
REDIS_SERVER_PORT=6379
#URl no qual a aplicação será instalada / Domain
SELF_URL=http://localhost:7373

#Credenciais para a API de BOTS
BOT_URL=http://localhost:8585/
BOT_LOGIN_EMAIL=
BOT_LOGIN_PASSWORD=
BOT_ID=

#Credenciais para API de logs
LOG_URL=http://localhost:9096/
APP_LOG_NAME=
APP_LOG_PASSWORD=

#Email que irá ser associado a aplicação para autenticação do serviço de email
EMAIL_SYSTEM=
#Senha do email para autenticação do serviço de email
EMAIL_PASSWORD=
#Email para validar superadmin
SUPERADMIN_EMAIL=

#Configuração para envios de email
SMTP_HOST=smtp.gmail.com
SMTP_STARTTLS_ENABLE=true
SMTP_SSL_PROTOCOLS=TLSv1.2
SMTP_SOCKETFACTORY_PORT=465
SMTP_SOCKETFACTORY_CLASS=javax.net.ssl.SSLSocketFactory
SMTP_AUTH=true
SMTP_PORT=465
SMTP_SSL_TRUST=*

#Audience do token, quem deve processar o token
JWT_AUDIENCE=
#Dominio do token, quem foi o emissor
JWT_DOMAIN=
#Secret Token do usuário
USER_SECRET=
#Secret Token do moderador
MODERATOR_SECRET=
```

#### Execute o comando a seguir para construir a imagem Docker.

    docker build -t borai .

#### Execute o comando a seguir para executar a imagem criada com o Docker.

    docker run --name borai -p 7070:7070 borai:latest
  

## Endpoints disponíveis
No momento apenas alguns endpoints estão disponíveis, e estão hospedados na página de endpoints do Postman.
https://documenter.getpostman.com/view/29175154/2sAYBPnEij
> As respostas dos endpoints estão incompletas pois o projeto precisa alcançar uma versão estável para que não haja mudanças frequentes nas respostas das requisições e para que se mantenha atualizada.💡