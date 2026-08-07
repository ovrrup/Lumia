import os
import glob

def fix_trailing():
    files = glob.glob("app/src/main/java/lumia/tracker/**/*.kt", recursive=True)
    for f in files:
        with open(f, 'r', encoding='utf-8') as file:
            lines = file.readlines()
        
        # Check if the last non-empty line starts with @ or ends weirdly
        changed = False
        while lines:
            last = lines[-1].strip()
            if last == "" or last.startswith("@Composable") or last.startswith("@OptIn") or last.startswith("@"):
                lines.pop()
                changed = True
            else:
                break
        
        if changed:
            with open(f, 'w', encoding='utf-8') as file:
                file.writelines(lines)
            print(f"Cleaned trailing annotations in {f}")

if __name__ == "__main__":
    fix_trailing()
