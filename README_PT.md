# Open Markdown Notes - Notas em Markdown Aberto

<a href="https://play.google.com/store/apps/details?id=net.basov.omn.b"><img src="https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png" height="80" alt="Disponível no Google Play" /></a>
<a href="https://f-droid.org/en/packages/net.basov.omn.fdroid/"><img src="https://f-droid.org/badge/get-it-on.png" height="80" alt="Disponível no F-Droid" /></a>

## Informações Gerais

Esta é uma aplicação simples, leve e de código aberto para criar e organizar notas do dia a dia. As notas são armazenadas em formato Markdown e exibidas como HTML. Uma nova página HTML é criada se ela não existir ou se a página Markdown correspondente tiver um carimbo de data/hora de modificação mais recente.

**Este programa usa um editor externo.** Atualmente, apenas editores com suporte a FileProvider são adequados, por exemplo: [QuickEdit Text Editor - Writer, Code Editor](https://play.google.com/store/apps/details?id=com.rhmsoft.edit) ou [Acode editor - Android code editor](https://f-droid.org/en/packages/com.foxdebug.acode/). (Porque o Google requer nível mínimo de API 26, o que exige o uso de FileProvider com esquema de dados content://.)

O suporte a meta informações do Pelican CMS pode ser habilitado na tela de preferências.

Eu projetei esta aplicação para minhas necessidades e a uso todos os dias. Ficarei feliz se esta aplicação atender às necessidades de outras pessoas também.

## Novos Recursos (Versão 35)

### 🤖 Inteligência Artificial com Engenharia de Contexto

- **Análise de Contexto**: Extração automática de palavras-chave e contexto das suas notas
- **Sugestões Inteligentes**: Recomendações baseadas em IA para organização de notas
- **Resumos Automáticos**: Geração de resumos de notas usando técnicas de IA
- **Notas Relacionadas**: Descoberta automática de conexões entre notas baseada em contexto

### 📊 Funcionalidade de Grafos

- **Visualização de Grafos**: Visualize as conexões entre suas notas
- **Backlinks**: Veja quais notas referenciam a nota atual
- **Forward Links**: Acompanhe as referências da nota atual
- **Navegação em Grafo**: Navegue facilmente entre notas relacionadas
- **Análise de Relacionamentos**: Entenda a estrutura do seu conhecimento

### 🎯 Melhorias de Usabilidade

- **Suporte ao Android 15/16**: Atualizado para API 35 com suporte total
- **Tradução Completa em Português**: Interface totalmente traduzida
- **Permissões Modernas**: Permissões atualizadas para Android 13+
- **Interface Otimizada**: Melhorias na experiência do usuário

## Como Usar os Novos Recursos

### Análise de Contexto AI

As notas são automaticamente analisadas para extrair contexto e palavras-chave. A aplicação identifica:
- Palavras-chave principais do conteúdo
- Notas relacionadas por similaridade de contexto
- Sugestões para melhor organização

### Visualização de Grafos

Cada nota mostra:
- **Backlinks**: Outras notas que fazem referência a esta nota
- **Forward Links**: Notas referenciadas nesta nota
- **Grafo Visual**: Representação gráfica das conexões (quando JavaScript está habilitado)

## Gestão de Projetos e Documentação

Esta aplicação é ideal para:
- 📝 Gestão de projetos pessoais
- 📚 Documentação técnica
- 🧠 Base de conhecimento pessoal
- ✅ Listas de tarefas e acompanhamento
- 💡 Ideias e brainstorming
- 🔗 Conexão de conceitos relacionados

## Requisitos do Sistema

- Android 5.0 (API 17) ou superior
- Recomendado: Android 15 (API 35) para todos os recursos
- Editor externo compatível com FileProvider
- Espaço de armazenamento para suas notas

## Permissões

A aplicação solicita as seguintes permissões:

- **Armazenamento**: Para ler e escrever arquivos de notas
- **Atalhos**: Para criar atalhos na tela inicial
- **Notificações** (Android 13+): Para notificações sobre atualizações de notas
- **Termux** (Opcional): Para integração com Termux

## Privacidade

- Todas as notas são armazenadas localmente no seu dispositivo
- Nenhum dado é enviado para servidores externos
- Você tem controle total sobre seus dados
- Código aberto e auditável

## Construção e Desenvolvimento

### Dependências

Este projeto usa vários componentes de código aberto:

#### Mustache JavaScript (node)
```
sudo npm install -g mustache
```

#### Markdown para HTML (marked.js)
```
git clone https://github.com/chjj/marked
```

#### Realce de código (highlight.js)
```
git clone https://github.com/isagalaev/highlight.js
npm install
nodejs tools/build.js browser :common
```

#### Ícones Material Design
```
git clone http://github.com/google/material-design-icons/
```

### Compilar

```bash
./gradlew build
```

## Contribuindo

Contribuições são bem-vindas! Por favor:
1. Faça um fork do projeto
2. Crie uma branch para sua feature
3. Commit suas mudanças
4. Faça push para a branch
5. Abra um Pull Request

## Licença

Copyright (c) 2017-2024 Mikhail Basov

Licenciado sob a Licença MIT. Veja o arquivo LICENSE para detalhes.

## Suporte

Para problemas, sugestões ou perguntas:
- Abra uma issue no GitHub
- Entre em contato com o desenvolvedor

## Agradecimentos

- Comunidade Android de código aberto
- Contribuidores do Markdown
- Usuários que fornecem feedback valioso

---

**Aproveite suas anotações inteligentes!** 🚀
