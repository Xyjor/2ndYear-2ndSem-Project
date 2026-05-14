param(
    [string] $JavaPath = "C:\Program Files\Java\jdk-23\bin\java.exe"
)

$ErrorActionPreference = "Stop"

if (-not (Test-Path -LiteralPath $JavaPath)) {
    throw "java.exe was not found at '$JavaPath'."
}

$modulePath = @(
    "C:\Users\ACER\.m2\repository\org\openjfx\javafx-base\17.0.7\javafx-base-17.0.7-win.jar",
    "C:\Users\ACER\.m2\repository\org\openjfx\javafx-controls\17.0.7\javafx-controls-17.0.7-win.jar",
    "C:\Users\ACER\.m2\repository\org\openjfx\javafx-fxml\17.0.7\javafx-fxml-17.0.7-win.jar",
    "C:\Users\ACER\.m2\repository\org\openjfx\javafx-graphics\17.0.7\javafx-graphics-17.0.7-win.jar"
) -join ";"

$classpath = @(
    "build\classes",
    "C:\Users\ACER\Downloads\jfoenix-9.0.10.jar",
    "C:\Users\ACER\Downloads\postgresql-42.7.11.jar",
    "lib\HikariCP-7.0.2.jar",
    "lib\slf4j-api-2.0.17.jar",
    "lib\slf4j-simple-2.0.17.jar"
) -join ";"

& $JavaPath `
    --module-path $modulePath `
    --add-modules javafx.controls,javafx.fxml `
    --enable-native-access=javafx.graphics `
    --add-exports javafx.base/com.sun.javafx.event=ALL-UNNAMED `
    --add-opens java.base/java.lang.reflect=ALL-UNNAMED `
    --add-opens javafx.controls/com.sun.javafx.scene.control.behavior=ALL-UNNAMED `
    --add-opens javafx.controls/com.sun.javafx.scene.control=ALL-UNNAMED `
    --add-opens javafx.base/com.sun.javafx.binding=ALL-UNNAMED `
    --add-opens javafx.graphics/com.sun.javafx.stage=ALL-UNNAMED `
    -cp $classpath `
    com.companyproject.MainApp
