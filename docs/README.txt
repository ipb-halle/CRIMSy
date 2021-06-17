Dokumentation in Markdown
=========================


Erzeugung von PDFs 
------------------

   pandoc *.md -o x.pdf

Achtung: Das Format gfm unterstuetzt nur wenige pandoc-Plugins; komplexere Tabellen 
sind nicht darunter. Entsprechend ist die Formatierung von Tabellen schwierig, sobald 
Zeilenumbrueche in Tabellenzellen vorkommen sollen. Daher normales" Markdown.


Erzeugung von HTML
------------------
- einzelnes HTML-Dokument (ohne externe Links)
- Problem: UTF-8 Zeichen werden nicht richtig interpretiert; Workaround: Einfügen eines Headers -->

<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
</head>
<body>



