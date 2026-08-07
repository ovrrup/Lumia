import os
import glob
import re

def recombine_all():
    # Find all base files that have corresponding _*.kt files
    all_kt = glob.glob("app/src/main/java/lumia/tracker/**/*.kt", recursive=True)
    base_files = set()
    for f in all_kt:
        if "_" in os.path.basename(f) and not f.endswith("_Impl.kt") and not f.endswith("JsonAdapter.kt"):
            # find base file
            base_name = f.split("_")[0] + ".kt"
            if os.path.exists(base_name):
                base_files.add(base_name)
    
    for base in base_files:
        dir_name = os.path.dirname(base)
        prefix = os.path.basename(base)[:-3] + "_"
        part_files = sorted([p for p in glob.glob(os.path.join(dir_name, prefix + "*.kt"))])
        
        print(f"Recombining {base} with {len(part_files)} part files...")
        content = ""
        with open(base, 'r', encoding='utf-8') as bf:
            content += bf.read()
        
        for pf in part_files:
            with open(pf, 'r', encoding='utf-8') as pf_file:
                pf_content = pf_file.read()
                # strip header package and imports from part file
                lines = pf_content.splitlines(True)
                # find first line after imports
                body_start = 0
                for idx, line in enumerate(lines):
                    if line.startswith("package ") or line.startswith("import ") or line.strip() == "":
                        continue
                    else:
                        body_start = idx
                        break
                content += "\n" + "".join(lines[body_start:])
            os.remove(pf)
            print(f"Removed part file {pf}")
        
        with open(base, 'w', encoding='utf-8') as bf:
            bf.write(content)
        print(f"Successfully restored {base}")

if __name__ == "__main__":
    recombine_all()
