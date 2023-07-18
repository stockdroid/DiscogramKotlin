# DiscogramKotlin

Assistenza Telegram per il gruppo Il Villaggio Dei Crisatici, si interfaccia con discord dalla parte dei mod, per una gestione del ticket completa e facile (thread, embed ecc.).

# Autori

- [@Chicchi7393](https://github.com/Chicchi7393)

# Contributori

- [@Repressoh](https://github.com/Repressoh)
- [@Dependabot](https://github.com/dependabot)



## Variabili d'ambiente

Per eseguire questo progetto, devi copiare [settings.json.example](https://github.com/stockdroidcommunity/DiscogramKotlin/blob/master/json/settings.json.example) a settings.json e modificarlo seguendo [la Wiki](https://github.com/stockdroidcommunity/DiscogramKotlin/wiki)


## Eseguire in locale - primo metodo

- Clona il progetto

```bash
  git clone https://github.com/Chicchi7393/discogramKotlinRewrite.git
```

- Segui [Variabili di Ambiente](https://github.com/stockdroidcommunity/DiscogramKotlin/master/README.md#variabili-dambiente)

- Installa [maven](https://maven.apache.org/install.html) e [Java 17](https://www.oracle.com/java/technologies/downloads/#java17) 

- Compila il progetto
```bash
  mvn compile && mvn package
```

- Avvia il progetto
```bash
  java -jar target/discogramRewrite-1.0-SNAPSHOT.jar
```

## Eseguire in locale - secondo metodo

- Crea ed entra nella cartella del progetto

```bash
  mkdir DiscogramRewrite & cd DiscogramRewrite
```
- Installa [Java 17](https://www.oracle.com/java/technologies/downloads/#java17) 

- Scarica il jar [ dalle actions](https://github.com/stockdroidcommunity/DiscogramKotlin/actions) e scompatta lo zip

- Scarica [la cartella json](https://github.com/stockdroidcommunity/DiscogramKotlin/tree/master/json) e mettila nella tree del progetto

- Segui [Variabili di Ambiente](https://github.com/stockdroidcommunity/DiscogramKotlin/master/README.md#variabili-dambiente)

- Avvia il progetto
```bash
  java -jar DiscogramRewrite.jar
```
