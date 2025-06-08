@echo off
echo Compilation et exécution des tests...

REM Définir les chemins
set CLASSPATH=target/classes;target/test-classes;lib/*

REM Exécuter les tests individuellement
echo Exécution de BombTest...
java -cp %CLASSPATH% org.junit.platform.console.ConsoleLauncher --select-class=com.example.bomberman.tests.models.entities.BombTest

echo Exécution de PlayerTest...
java -cp %CLASSPATH% org.junit.platform.console.ConsoleLauncher --select-class=com.example.bomberman.tests.models.entities.PlayerTest

echo Exécution de PowerUpTest...
java -cp %CLASSPATH% org.junit.platform.console.ConsoleLauncher --select-class=com.example.bomberman.tests.models.entities.PowerUpTest

echo Exécution de GameBoardTest...
java -cp %CLASSPATH% org.junit.platform.console.ConsoleLauncher --select-class=com.example.bomberman.tests.models.world.GameBoardTest

echo Exécution de GameTest...
java -cp %CLASSPATH% org.junit.platform.console.ConsoleLauncher --select-class=com.example.bomberman.tests.models.world.GameTest

echo Exécution de ProfileManagerTest...
java -cp %CLASSPATH% org.junit.platform.console.ConsoleLauncher --select-class=com.example.bomberman.tests.service.ProfileManagerTest

echo Exécution de MainTest...
java -cp %CLASSPATH% org.junit.platform.console.ConsoleLauncher --select-class=com.example.bomberman.tests.MainTest

echo Tests terminés. 