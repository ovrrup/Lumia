import os
import shutil

src_root = "/app/applet/app/src/main/java/lumia/tracker"
dst_root = "app/src/main/java/lumia/tracker"

# Preserve these files we customized with mesh gradient and glass bars
preserve = {
    "app/src/main/java/lumia/tracker/ui/components/MeshGradient.kt",
    "app/src/main/java/lumia/tracker/ui/theme/GlassBars.kt",
    "app/src/main/java/lumia/tracker/ui/theme/Glass.kt",
    "app/src/main/java/lumia/tracker/ui/components/header/InteractivePushPullHeader.kt",
    "app/src/main/java/lumia/tracker/ui/components/navigation/FloatingCapsuleNavBar.kt",
}

# First remove any generated files in dst_root that are not in src_root
for root, dirs, files in os.walk(dst_root):
    for f in files:
        dst_path = os.path.join(root, f)
        rel_path = os.path.relpath(dst_path, dst_root)
        src_path = os.path.join(src_root, rel_path)
        if not os.path.exists(src_path) and dst_path not in preserve:
            os.remove(dst_path)
            print(f"Removed extra file: {dst_path}")

# Now copy files from src_root to dst_root
for root, dirs, files in os.walk(src_root):
    for f in files:
        src_path = os.path.join(root, f)
        rel_path = os.path.relpath(src_path, src_root)
        dst_path = os.path.join(dst_root, rel_path)
        if dst_path in preserve and os.path.exists(dst_path):
            print(f"Preserving edited file: {dst_path}")
            continue
        os.makedirs(os.path.dirname(dst_path), exist_ok=True)
        shutil.copy2(src_path, dst_path)
        print(f"Restored: {dst_path}")

print("Sync completed!")
