param([string]$CompilerVersion = '2.1.20')
$ErrorActionPreference = 'Stop'
$repoRoot = Split-Path $PSScriptRoot -Parent
Set-Location -LiteralPath $repoRoot
$checkRoot = Join-Path $repoRoot '.local-checks'
New-Item -ItemType Directory -Force -Path $checkRoot | Out-Null
$artifactCache = Join-Path $env:USERPROFILE '.gradle/caches/modules-2/files-2.1'
function Find-Jar($group, $module, $version = '') {
    $target = Join-Path $artifactCache "$group/$module/$version"
    if (!(Test-Path -LiteralPath $target)) { throw "Missing cached $module $version. This script never installs a toolchain; use cloud tests or supply the test jars in .local-checks." }
    $found = Get-ChildItem -LiteralPath $target -Filter '*.jar' -Recurse | Select-Object -First 1
    if (!$found) { throw "No jar for $module" }
    return $found.FullName
}
$compiler = Find-Jar 'org.jetbrains.kotlin' 'kotlin-compiler-embeddable' $CompilerVersion
$stdlib = Find-Jar 'org.jetbrains.kotlin' 'kotlin-stdlib' $CompilerVersion
$trove = Find-Jar 'org.jetbrains.intellij.deps' 'trove4j'
$coroutines = Find-Jar 'org.jetbrains.kotlinx' 'kotlinx-coroutines-core-jvm' '1.8.0'
$annotations = Find-Jar 'org.jetbrains' 'annotations'
$compilerClasspath = "$compiler;$stdlib;$trove;$coroutines;$annotations"
javac -cp $compilerClasspath -d $checkRoot tools/KotlinSyntaxCheck.java
if ($LASTEXITCODE -ne 0) { throw 'Parser helper did not compile' }
java -Xmx384m -cp "$checkRoot;$compilerClasspath" KotlinSyntaxCheck app/src
if ($LASTEXITCODE -ne 0) { throw 'Kotlin syntax errors' }

# These small test-only jars can be copied here. No downloads or Gradle/Android build are performed.
$junit = Join-Path $checkRoot 'junit-4.13.2.jar'
$hamcrest = Join-Path $checkRoot 'hamcrest-core-1.3.jar'
$json = Join-Path $checkRoot 'json.jar'
foreach ($file in @($junit, $hamcrest, $json)) { if (!(Test-Path -LiteralPath $file)) { throw "Missing test library $file" } }
@'
package androidx.room
annotation class Entity(val tableName: String)
annotation class PrimaryKey
annotation class ColumnInfo(val defaultValue: String)
'@ | Set-Content -LiteralPath (Join-Path $checkRoot 'RoomAnnotations.kt') -Encoding utf8
# Only Room metadata annotations are stand-ins; planner, quiz, models and codec are the actual source.
$testClasspath = "$stdlib;$junit;$hamcrest;$json;$annotations"
java -Xmx384m -cp $compilerClasspath org.jetbrains.kotlin.cli.jvm.K2JVMCompiler -no-stdlib -no-reflect -classpath $testClasspath -jvm-target 11 -d .local-checks/domain .local-checks/RoomAnnotations.kt app/src/main/java/com/example/data/model/Entities.kt app/src/main/java/com/example/domain/PlannerEngine.kt app/src/main/java/com/example/domain/QuizAttempt.kt app/src/main/java/com/example/data/repository/BackupCodec.kt app/src/test/java/com/example/DomainRegressionTest.kt app/src/test/java/com/example/BackupCodecTest.kt
if ($LASTEXITCODE -ne 0) { throw 'JVM domain compilation failed' }
java -Xmx128m -cp ".local-checks/domain;$testClasspath" org.junit.runner.JUnitCore com.example.DomainRegressionTest com.example.BackupCodecTest
if ($LASTEXITCODE -ne 0) { throw 'Domain tests failed' }
python -X utf8 tools/check_migration.py
if ($LASTEXITCODE -ne 0) { throw 'SQLite migration check failed' }
