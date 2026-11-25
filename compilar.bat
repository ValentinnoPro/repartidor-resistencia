@echo off
setlocal enabledelayedexpansion
if exist out rmdir /s /q out
mkdir out
if exist fuentes.txt del fuentes.txt
rem Rutas entre comillas y con "/" para que funcionen aunque la carpeta tenga espacios
for /r src %%f in (*.java) do (
    set "ruta=%%f"
    echo "!ruta:\=/!">>fuentes.txt
)
javac -encoding UTF-8 -d out @fuentes.txt
del fuentes.txt
