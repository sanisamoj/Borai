# Borai

Este é um sistema de gerenciamento e armazenamento de eventos, que distingue dois tipos de contas: **Participant** e **Promoter**. 
As contas **Promoter** têm a capacidade de criar e gerenciar eventos, além de desfrutarem de vantagens em termos de visibilidade, 
o que proporciona maior alcance aos seus eventos. Por outro lado, as contas **Participant** podem marcar presença nos eventos, interagir com comentários e 
avaliações, e também avaliar os eventos e seus promotores. Embora as contas *Participant* possam criar eventos, elas não têm o mesmo nível de visibilidade que as contas *Promoter*, 
o que limita o alcance de seus eventos dentro da plataforma.

O sistema ainda utiliza microserviços para a melhor usabilidade, como o [NotifyBot](https://github.com/sanisamoj/NotifyBot) para envio de mensagens
ou notificações para usuários, e o [EventLoggerServer](https://github.com/sanisamoj/EventLoggerServer) para centralização de logs, como erros e avisos.

> Microserviços foram utilizados, pois os outros projetos do meu repositórios utilizam esses serviços em comum.

## Funcionalidades
Há a possibilidade de visualizar e encontrar eventos a partir de filtros pré-determinados, como local, horário, tipo de evento ou eventos
no qual contas em específicas as criaram. Também há a possibilidade de marcar presença nos eventos disponíveis, e visualizar contas nas quais
o usuário segue que marcaram presença também, os eventos podem ser comentados. Contas participantes e promotoras podem ter insignias a partir de 
pontos adiquiridos dentro da plataforma, como frequência de participação, de comentários, ou para promotores como eventos bem sucedidos, e bem avaliados
e quantidade de eventos feitos e etc.

- **É possível encontrar e visualizar eventos utilizando filtros pré-determinados, como:**

    - Localização.
    - Horário.
    - Eventos criados por contas específicas.


- **Interação com eventos:**

    - **Marcar presença:** Confirmar participação nos eventos disponíveis.
    - **Visualizar interações de outros usuários:** Ver contas seguidas que marcaram presença no evento.
    - **Comentar:** Publicar comentários nos eventos.


- **Insígnias para Participantes e Promotores**