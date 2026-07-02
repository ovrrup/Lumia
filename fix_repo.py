import re

with open("app/src/main/java/lumia/tracker/data/ScholarRepository.kt", "r") as f:
    content = f.read()

content = content.replace("targetDao.clearPomodoroSessions()", "targetDao.clearPomodoro()")
content = content.replace("targetDao.insertAttendance(it)", "targetDao.insertAttendanceRecord(it)")

# fix syntax error (extra bracket at end?)
# Check the last characters
lines = content.split('\n')
while len(lines) > 0 and lines[-1].strip() == "}":
    lines = lines[:-1]

content = '\n'.join(lines) + "\n}\n"

with open("app/src/main/java/lumia/tracker/data/ScholarRepository.kt", "w") as f:
    f.write(content)
