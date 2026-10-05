from pathlib import Path
import json
v=json.loads(Path('version.json').read_text())
s=Path('index.html').read_text()
for name in ['update.js','android-runtime.js']:
 s=s.replace('<script src="'+name+'"></script>','<script>'+Path(name).read_text()+'</script>')
Path('dist/QasDM_v'+v['version']+'.html').write_text(s)
