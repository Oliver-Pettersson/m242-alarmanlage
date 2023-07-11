# m242-alarmanlage
Das Programm ist eine Alarmanlage, welches 2 M5Stack core2 verwendet, welche mit Bewegungssensoren ausgestattet sind. Die Alarmanlage wird durch eine Java Applikation gesteuert, welche Antworten von einem Telegram Bot annimmmt. Änderungen werden in eine MySQL Datenbank gespeichert um diese zu loggen.

## Verschiedene Modis
### Activity
Die Alarmanlage kann ein und ausgeschaltet werden, dabei ist es aber wichtig, dass sich nichts vor dem Bewegungssensor befindet und dass dies in den letzte 5 sekunden nicht der Fall war.
### Triggered
Wenn die Alarmanlage ausgelöst wird gibt sie ein Audiosignal und ein optisches Signal über die LED Streifen des M5Stack's. Bis der Alarm ausgeschaltet wird sind die Funktionen beschränkt.

## Getting started
- repository klonen
```bash
git clone https://github.com/Oliver-Pettersson/m242-alarmanlage.git
```
- In das repository wechseln
```bash
cd m242-alarmanlage/
```
- docker container für mysql datenbank starten
```bash
docker-compose up -d
```
- Java app "java_backend" starten
- C++ Applikationen "SirenCore" auf einen M5 laden und "NumpadCore" auf den anderen laden 
- Den Telegrambot @SchnawgBot auf Telegram adden
## Telegram Bot
Der Telegrambot muss auf Telegramm geadded werden durch die Suchleiste mit dem Namen @SchnawgBot
### Befehle
Der Telegram Bot hat verschiedene Befehle, welche durch den Befehl 
```bash
/help
```
einsehbar sind.

## Numpad
Wenn der Alarm losgeht kann der Alarm ausgeschaltet werden indem man das Numpad verwendet und den richtigen Code eingibt. Momentan haben wir den Code als "0" konfiguriert.

