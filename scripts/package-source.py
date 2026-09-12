#!/usr/bin/env python3
"""Package the local Clash Meta Plus source tree; excludes ignored/private outputs.

Added 2026-09-13 for this independent GPL v3 modified version.
This is a source-tree export, not an audit or automatic export of dependency source.
"""
from pathlib import Path
import hashlib
import re
import subprocess
import zipfile

root = Path(__file__).resolve().parent.parent

def git(*args, **kwargs):
    return subprocess.run(['git', '-C', str(root), *args], check=True,
                          stdout=subprocess.PIPE, **kwargs).stdout

candidates = sorted(set(git('ls-files', '--cached', '--others', '--exclude-standard', '-z').split(b'\0')) - {b''})
ignored_result = subprocess.run(
    ['git', '-C', str(root), 'check-ignore', '--no-index', '-z', '--stdin'],
    input=b'\0'.join(candidates) + b'\0', stdout=subprocess.PIPE, check=False)
if ignored_result.returncode not in (0, 1):
    raise SystemExit('Cannot validate Git ignore rules; export cancelled.')
ignored = set(ignored_result.stdout.split(b'\0'))
files = []
for entry in candidates:
    if entry in ignored:
        continue
    path = root / entry.decode('utf-8')
    if path.is_symlink():
        raise SystemExit(f'Review symlink before exporting: {path.relative_to(root)}')
    if path.is_file():
        if not path.resolve().is_relative_to(root):
            raise SystemExit('Source file escaped project root.')
        files.append(path)
required = ['README.md', 'LICENSE', 'NOTICE', 'THIRD_PARTY.md', 'CHANGELOG.md',
            'core/src/foss/golang/clash/LICENSE', 'gradle/wrapper/gradle-wrapper.jar']
for name in required:
    if root / name not in files:
        raise SystemExit(f'Required source/notice missing or ignored: {name}')
version = re.search(r'versionName\s*=\s*"([^"]+)"', (root / 'build.gradle.kts').read_text()).group(1)
dist = root / 'dist'
dist.mkdir(exist_ok=True)
archive = dist / f'Clash-Meta-Plus-{version}-source.zip'
with zipfile.ZipFile(archive, 'w', zipfile.ZIP_DEFLATED, compresslevel=6) as output:
    for path in files:
        output.write(path, 'Clash-Meta-Plus/' + path.relative_to(root).as_posix())
with zipfile.ZipFile(archive) as check:
    if check.testzip() is not None:
        raise SystemExit('Source archive integrity check failed.')
checksum = archive.with_suffix('.zip.sha256')
checksum.write_text(hashlib.sha256(archive.read_bytes()).hexdigest() + '  ' + archive.name + '\n')
manifest = dist / 'SOURCE-FILES.sha256'
manifest.write_text(''.join(hashlib.sha256(path.read_bytes()).hexdigest() + '  ' + path.relative_to(root).as_posix() + '\n' for path in files))
print(f'Exported {len(files)} source files to {archive}')
print('External dependency source and Geo licensing require separate distribution review.')
