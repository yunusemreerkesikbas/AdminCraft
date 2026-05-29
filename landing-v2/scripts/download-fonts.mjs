#!/usr/bin/env node
// Download the Framer-used Google fonts: Stack Sans Headline, Google Sans Flex (Variable + 500), Geist
import { mkdir, writeFile } from 'node:fs/promises';
import { existsSync } from 'node:fs';
import { join } from 'node:path';

const OUT = join(process.cwd(), 'public', 'fonts');
await mkdir(OUT, { recursive: true });

const fonts = [
  // Stack Sans Headline (variable axes) — 500 weight
  { name: 'StackSansHeadline-500.woff2', url: 'https://fonts.gstatic.com/s/stacksansheadline/v1/1PtFg9jZXvmMnkLnuURbaukKZJTyrDV326uH6mSinjBIwc5fIgFCqgUA3ZCX.woff2' },
  { name: 'StackSansHeadline-400.woff2', url: 'https://fonts.gstatic.com/s/stacksansheadline/v1/1PtFg9jZXvmMnkLnuURbaukKZJTyrDV326uH6mSinjBIwc5tIjFAoAQ.woff2' },
  // Google Sans Flex Variable
  { name: 'GoogleSansFlex-Variable.woff2', url: 'https://fonts.gstatic.com/s/googlesansflex/v16/t5t7IQcYNIWbFgDgAAzZ34auoVyXip6sfhcat2c.woff2' },
  { name: 'GoogleSansFlex-500.woff2', url: 'https://fonts.gstatic.com/s/googlesansflex/v16/t5sJIQcYNIWbFgDgAAzZ34auoVyXkJCOvp3SFWJbN5hF8Ju1x6sKCyp0l9sI40swNJwInycYAJzz0m7kJ4qFQOJBOjLvDSndo0SKMpKSTzwliVdHAy4bxTDHg_ugnAakp_mbyc5qU4LBMUM.woff2' },
  // Geist 700
  { name: 'Geist-700.woff2', url: 'https://fonts.gstatic.com/s/geist/v4/gyBhhwUxId8gMGYQMKR3pzfaWI_Re-Q4mJPby1QNtA.woff2' },
];

for (const f of fonts) {
  const dest = join(OUT, f.name);
  if (existsSync(dest)) { console.log(`skip ${f.name}`); continue; }
  try {
    const res = await fetch(f.url);
    if (!res.ok) throw new Error(`${res.status}`);
    const buf = Buffer.from(await res.arrayBuffer());
    await writeFile(dest, buf);
    console.log(`✓ ${f.name} (${buf.length} bytes)`);
  } catch (err) {
    console.error(`✗ ${f.name}: ${err.message}`);
  }
}
