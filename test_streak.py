import sys
content = open("app/src/main/java/lumia/tracker/viewmodel/ScholarViewModel.kt", "r").read()
idx = content.find("                        .putLong(\"streak_last_date\", todayStart)")
if idx != -1:
    print(content[idx:idx+1500])
