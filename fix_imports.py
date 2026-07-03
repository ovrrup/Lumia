with open("app/src/main/java/lumia/tracker/ui/screens/settings/StreakSettingsScreen.kt", "r") as f:
    lines = f.readlines()

new_lines = []
imports = []
pkg = ""

for line in lines:
    if line.startswith("package "):
        pkg = line
    elif line.startswith("import "):
        imports.append(line)
    else:
        new_lines.append(line)

final_content = pkg + "".join(sorted(set(imports))) + "".join(new_lines)
with open("app/src/main/java/lumia/tracker/ui/screens/settings/StreakSettingsScreen.kt", "w") as f:
    f.write(final_content)

