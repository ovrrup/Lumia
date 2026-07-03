import re
import os

with open("app/src/main/java/lumia/tracker/ui/screens/study/CourseDetailScreen.kt", "r") as f:
    content = f.read()

# Let's extract the "if (showXXXDialog) {" blocks at the root level of CourseDetailScreen
blocks_to_extract = [
    "if (showLinkSubjectDialog) {",
    "if (showAddAssignmentDialog) {",
    "if (assignmentToEdit != null) {",
    "if (showAddNoteDialog) {",
    "if (showAddAttachmentDialog) {"
]

for block_start in blocks_to_extract:
    idx = content.find(block_start)
    if idx != -1:
        # Find matching brace
        brace_count = 0
        end_idx = -1
        for i in range(idx, len(content)):
            if content[i] == '{':
                brace_count += 1
            elif content[i] == '}':
                brace_count -= 1
                if brace_count == 0:
                    end_idx = i
                    break
        if end_idx != -1:
            block_content = content[idx:end_idx+1]
            print(f"Found block: {block_start}")
            print(block_content[:100] + "...\n")
