import re

with open("app/src/main/java/lumia/tracker/util/TrueAodManager.kt", "r") as f:
    content = f.read()

content = content.replace("import java.time.LocalTime\nimport java.time.format.DateTimeFormatter", "import java.text.SimpleDateFormat\nimport java.util.Date\nimport java.util.Locale")
content = content.replace("val timeFormat = remember { DateTimeFormatter.ofPattern(\"HH:mm\") }", "val timeFormat = remember { SimpleDateFormat(\"HH:mm\", Locale.getDefault()) }")
content = content.replace("LocalTime.now().format(timeFormat)", "timeFormat.format(Date())")

with open("app/src/main/java/lumia/tracker/util/TrueAodManager.kt", "w") as f:
    f.write(content)
