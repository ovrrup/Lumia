import re

with open("app/src/main/java/lumia/tracker/ui/screens/home/DashboardScreen.kt", "r") as f:
    content = f.read()

# Match the topBar = { ... }, block
pattern = r"(\s*)topBar = \{.*?\}(?=\s*,\s*bottomBar = \{)"
# Note: it's not working because there are nested braces. Let's use a simpler string replacement.

top_bar_start_str = "            topBar = {"
bottom_bar_str = "            bottomBar = {"

start_idx = content.find(top_bar_start_str)
end_idx = content.find(bottom_bar_str)

if start_idx != -1 and end_idx != -1:
    new_content = content[:start_idx] + content[end_idx:]
    with open("app/src/main/java/lumia/tracker/ui/screens/home/DashboardScreen.kt", "w") as f:
        f.write(new_content)
    print("Patched TopBar.")
else:
    print("Could not find TopBar or BottomBar.")
