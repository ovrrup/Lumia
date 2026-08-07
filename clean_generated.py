import os
import glob

def clean_generated():
    files = glob.glob("app/src/main/java/lumia/tracker/**/*.kt", recursive=True)
    for f in files:
        if "_" in os.path.basename(f) or "Part" in os.path.basename(f):
            if f.endswith("Part2.kt") and "Entities" in f:
                continue
            try:
                os.remove(f)
                print(f"Removed {f}")
            except Exception as e:
                print(e)

if __name__ == "__main__":
    clean_generated()
