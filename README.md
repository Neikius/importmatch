# Importer

## Prepare
* have maven, JDK 1.8, ansible

Locally install&run postgres docker using ansible
```
cd ansible
ansible-playbook ./postgres.yml
```
OR do it manually, reading postgres.yml will yield parameters easily.

## Build
Drop `fo_random.txt` file in src/main/resources before build to embed default data (then you will be able to run without parameters).

`mvn package`

## Run
```
java -jar target/importmatch-1.0-SNAPSHOT-shaded.jar
```
Or specify the import file
```
java -jar target/importmatch-1.0-SNAPSHOT-shaded.jar ./target/file.txt
```

This takes a minute on my system with provided file