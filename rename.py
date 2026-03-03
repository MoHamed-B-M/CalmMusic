import os
import glob
import shutil

src_dir = r"c:\Users\Admin\Documents\kotlin proects\music\app\src\main\java\com\mohamed\calmplayer"
dest_dir = r"c:\Users\Admin\Documents\kotlin proects\music\app\src\main\java\com\music\calmplayer"

# Move the directory
if os.path.exists(src_dir):
    parent = os.path.dirname(src_dir)
    os.makedirs(os.path.dirname(dest_dir), exist_ok=True)
    os.rename(src_dir, dest_dir)

# Replace in all files
target_files = []
root = r"c:\Users\Admin\Documents\kotlin proects\music"
for ext in ["**/*.kt", "**/*.xml", "**/*.kts"]:
    target_files.extend(glob.glob(os.path.join(root, ext), recursive=True))

for file_path in target_files:
    if "build/generated" in file_path.replace("\\", "/"): continue
    try:
        with open(file_path, "r", encoding="utf-8") as f:
            content = f.read()
            
        if "com.mohamed.calmplayer" in content:
            content = content.replace("com.mohamed.calmplayer", "com.music.calmplayer")
            with open(file_path, "w", encoding="utf-8") as f:
                f.write(content)
            print("Updated", file_path)
    except Exception as e:
        print("Error processing", file_path, e)
