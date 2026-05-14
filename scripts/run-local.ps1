# Run the CompanyProject app without Ant (compiles and runs using javac/java)
$projRoot = 'C:\Users\ACER\Documents\NetBeansProjects\CompanyProject'
$propsFile = Join-Path $projRoot 'nbproject\project.properties'
if (-not (Test-Path $propsFile)) { Write-Error "project.properties not found: $propsFile"; exit 1 }

# Read file.reference entries to build classpath
$props = Get-Content $propsFile -ErrorAction Stop
$refs = @()
foreach ($line in $props) {
    if ($line -match '^file\.reference\.[^=]+=') {
        $parts = $line -split '=',2
        $path = $parts[1].Trim()
        if ($path -ne '') {
            # Normalize path (remove surrounding quotes if any)
            $path = $path.Trim('\"')
            # Replace forward slashes if any
            $path = $path -replace '/', '\\'
            $refs += $path
        }
    }
}
# Add lib/*.jar from project lib if present
$libDir = Join-Path $projRoot 'lib'
if (Test-Path $libDir) {
    Get-ChildItem -Path $libDir -Filter '*.jar' -File | ForEach-Object { $refs += $_.FullName }
}
# Filter out javadoc jars and non-existing paths
$refs = $refs | Where-Object { $_ -and ($_ -notmatch '\-javadoc\.jar$') -and (Test-Path $_) }
if ($refs.Count -eq 0) {
    Write-Error "No referenced jars found in project.properties or lib/. Please ensure dependencies are present."
    exit 1
}
$classpath = [string]::Join(';', $refs)

# Prepare build directory
$buildDir = Join-Path $projRoot 'build\classes'
New-Item -ItemType Directory -Force -Path $buildDir | Out-Null

# Collect sources
$sources = Get-ChildItem -Path (Join-Path $projRoot 'src') -Recurse -Filter *.java | ForEach-Object { $_.FullName }
if ($sources.Count -eq 0) { Write-Error 'No Java sources found under src/'; exit 1 }

# Compile
Write-Host "Compiling $($sources.Count) sources..."
$javac = 'javac'
$modulePath = 'C:\Users\ACER\Documents\FX SDK\javafx-sdk-26\lib'
$javafxModules = 'javafx.controls,javafx.fxml'
$javacArgs = @('--release','23','--module-path',$modulePath,'--add-modules',$javafxModules,'-cp',$classpath,'-d',$buildDir)
$javacArgs += $sources
& $javac @javacArgs
if ($LASTEXITCODE -ne 0) { Write-Error 'javac failed'; exit $LASTEXITCODE }

# Run
$java = 'java'
$modulePath = 'C:\Users\ACER\Documents\FX SDK\javafx-sdk-26\lib'
if (-not (Test-Path $modulePath)) { Write-Warning "JavaFX SDK not found at $modulePath. The app may fail to start without JavaFX." }
$javafxModules = 'javafx.controls,javafx.fxml'
$vmArgList = @('--add-opens','java.base/java.lang.reflect=ALL-UNNAMED','--add-opens','java.base/java.lang=ALL-UNNAMED','--enable-native-access=javafx.graphics')
$runClasspath = "$buildDir;$classpath"
Write-Host 'Starting application (MainApp)...'
$javaArgs = @('--module-path',$modulePath,'--add-modules',$javafxModules) + $vmArgList + @('-cp',$runClasspath,'com.companyproject.MainApp')
& $java @javaArgs
exit $LASTEXITCODE
