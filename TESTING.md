# Testes e cobertura

Execute a suíte com o banco H2 isolado pelas configurações de teste:

    ./mvnw -B -ntp clean verify
    ./mvnw -B -ntp dependency:copy-dependencies -DincludeScope=test -DoutputDirectory=target/dependency

O JaCoCo gera `target/site/jacoco/jacoco.xml` e o Surefire grava os resultados em `target/surefire-reports/`.
Os testes não substituem nem sobrescrevem as configurações de banco da aplicação; `src/test/resources/application.properties`
seleciona H2 exclusivamente no contexto de teste. A verificação HTTP inicia o servidor em uma porta aleatória local.

O workflow `Validação Sonar` roda os testes em pull requests e publica os relatórios e binários para a análise
central. A execução manual valida apenas a suíte.
