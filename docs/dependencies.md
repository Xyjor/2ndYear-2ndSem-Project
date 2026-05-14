# Dependencies

This project is a NetBeans Ant JavaFX project, so add JARs through Project Properties > Libraries.

Required JARs:

- PostgreSQL JDBC: `org.postgresql:postgresql:42.7.11`
- HikariCP: `com.zaxxer:HikariCP:7.0.2`
- SLF4J API: `org.slf4j:slf4j-api:2.0.17`
- SLF4J Simple runtime: `org.slf4j:slf4j-simple:2.0.17`
- JFoenix: `com.jfoenix:jfoenix:9.0.10`
- JavaFX runtime for local script: OpenJFX `17.0.7`

The project already referenced PostgreSQL JDBC and JFoenix from `C:\Users\ACER\Downloads`.
The new HikariCP and SLF4J JARs were added under `lib`.

Database setup helper:

```powershell
powershell -ExecutionPolicy Bypass -File database\setup-database.ps1
```

The script creates `company_dms`, applies `database/schema.sql`, and seeds a manager login:

- Database user: `company_app`
- Database password: `company_app_dev`
- Username: `manager`
- Password: `ChangeMe123!`

After compiling in NetBeans or with Ant, run the app from PowerShell:

```powershell
powershell -ExecutionPolicy Bypass -File scripts\run-app.ps1
```

The launcher uses Java 23 plus JavaFX 17 because JFoenix 9 relies on JavaFX internals that are fragile on newer JavaFX runtimes.

Maven coordinates, if this project is later converted to Maven:

```xml
<dependencies>
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <version>42.7.11</version>
    </dependency>
    <dependency>
        <groupId>com.zaxxer</groupId>
        <artifactId>HikariCP</artifactId>
        <version>7.0.2</version>
    </dependency>
    <dependency>
        <groupId>org.slf4j</groupId>
        <artifactId>slf4j-simple</artifactId>
        <version>2.0.17</version>
    </dependency>
    <dependency>
        <groupId>com.jfoenix</groupId>
        <artifactId>jfoenix</artifactId>
        <version>9.0.10</version>
    </dependency>
</dependencies>
```
