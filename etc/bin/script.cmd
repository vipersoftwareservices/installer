@echo off
echo [InternetShortcut] >> "%AllUsersProfile%\desktop\NOTEPAD.url"
echo URL="C:\WINDOWS\NOTEPAD.EXE" >> "%AllUsersProfile%\desktop\NOTEPAD.url"
echo IconFile=C:\WINDOWS\system32\SHELL32.dll >> "%AllUsersProfile%\desktop\NOTEPAD.url"
echo IconIndex=20 >> "%AllUsersProfile%\desktop\NOTEPAD.url"