import os
import re

def get_line_count(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        return len(f.readlines())

def split_file(filepath):
    lines = open(filepath, 'r', encoding='utf-8').readlines()
    if len(lines) <= 190:
        return

    print(f"Splitting {filepath} ({len(lines)} lines)")
    
    pkg_line = ""
    imports = []
    i = 0
    while i < len(lines):
        line = lines[i]
        if line.startswith("package "):
            pkg_line = line
        elif line.startswith("import "):
            imports.append(line)
        elif line.strip().startswith("@Composable") or line.strip().startswith("@OptIn") or line.strip().startswith("class ") or line.strip().startswith("fun ") or line.strip().startswith("sealed ") or line.strip().startswith("data class "):
            break
        i += 1

    header = pkg_line + "".join(imports) + "\n"
    body_lines = lines[i:]

    # Parse top-level blocks
    blocks = []
    current_block = []
    current_name = None
    brace_depth = 0
    
    for line in body_lines:
        # Check if new top level declaration at depth 0
        if brace_depth == 0 and (line.startswith("fun ") or line.startswith("class ") or line.startswith("data class ") or line.startswith("sealed class ") or line.startswith("enum class ") or line.startswith("private fun ") or line.startswith("@Composable")):
            # Match declaration name
            m = re.search(r'(?:fun|class|interface)\s+(?:<[^>]+>\s+)?([A-Za-z0-9_]+)', line)
            if m:
                if current_block:
                    blocks.append((current_name or "Header", current_block))
                current_block = []
                current_name = m.group(1)

        current_block.append(line)
        brace_depth += line.count("{") - line.count("}")

    if current_block:
        blocks.append((current_name or "Misc", current_block))

    # Group blocks into chunk files of <= 160 lines
    dir_path = os.path.dirname(filepath)
    base_name = os.path.basename(filepath).replace(".kt", "")

    chunks = []
    curr_chunk = []
    curr_chunk_size = 0

    for name, blk in blocks:
        blk_size = len(blk)
        if curr_chunk_size + blk_size > 160 and curr_chunk:
            chunks.append(curr_chunk)
            curr_chunk = []
            curr_chunk_size = 0
        
        curr_chunk.append((name, blk))
        curr_chunk_size += blk_size

    if curr_chunk:
        chunks.append(curr_chunk)

    if len(chunks) <= 1:
        return

    # Write first chunk back to original file
    first_chunk = chunks[0]
    out_lines = [header]
    for _, blk in first_chunk:
        out_lines.extend(blk)
    with open(filepath, 'w', encoding='utf-8') as f:
        f.writelines(out_lines)

    # Write remaining chunks to new files
    for idx, chunk in enumerate(chunks[1:], start=1):
        first_decl_name = chunk[0][0]
        new_file_name = f"{base_name}Part{idx}.kt" if not first_decl_name or first_decl_name == "Header" else f"{base_name}_{first_decl_name}.kt"
        new_file_path = os.path.join(dir_path, new_file_name)
        out_lines = [header]
        for _, blk in chunk:
            out_lines.extend(blk)
        with open(new_file_path, 'w', encoding='utf-8') as f:
            f.writelines(out_lines)
        print(f"Created {new_file_path} ({len(out_lines)} lines)")

if __name__ == "__main__":
    import sys
    for path in sys.argv[1:]:
        if os.path.exists(path):
            split_file(path)
