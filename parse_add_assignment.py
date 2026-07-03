with open("app/src/main/java/lumia/tracker/ui/screens/study/CourseDetailScreen.kt", "r") as f:
    content = f.read()
idx = content.find("if (showAddAssignmentDialog) {")
end_idx = -1
brace_count = 0
for i in range(idx, len(content)):
    if content[i] == '{': brace_count += 1
    elif content[i] == '}':
        brace_count -= 1
        if brace_count == 0:
            end_idx = i
            break
if end_idx != -1:
    print(content[idx:end_idx+1][:1000])
