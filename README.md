# Borai

Este é um sistema de gerenciamento e armazenamento de eventos, que distingue dois tipos de contas: **Participant** e **Promoter**. 
As contas **Promoter** têm a capacidade de criar e gerenciar eventos, além de desfrutarem de vantagens em termos de visibilidade, 
o que proporciona maior alcance aos seus eventos. Por outro lado, as contas **Participant** podem marcar presença nos eventos, interagir com comentários e 
avaliações, e também avaliar os eventos e seus promotores. Embora as contas *Participant* possam criar eventos, elas não têm o mesmo nível de visibilidade que as contas *Promoter*, 
o que limita o alcance de seus eventos dentro da plataforma.

O sistema ainda utiliza microserviços para a melhor usabilidade, como o [NotifyBot](https://github.com/sanisamoj/NotifyBot) para envio de mensagens
ou notificações para usuários, e o [EventLoggerServer](https://github.com/sanisamoj/EventLoggerServer) para centralização de logs, como erros e avisos.

> *Microserviços foram utilizados, pois os outros projetos do meu repositório utilizam esses serviços em comum.*

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

  - Ver as contas públicas que marcaram presença no evento.
  - Visualizar informações como, local, data, horário, descrição, fotos, informações do organizador e o tipo de evento.
  

## Endpoints disponíveis
No momento apenas alguns endpoints estão disponíveis, e estão hospedados na página de endpoints do Postman.
https://documenter.getpostman.com/view/29175154/2sAYBPnEij