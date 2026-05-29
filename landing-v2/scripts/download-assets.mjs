#!/usr/bin/env node
// Downloads all assets from extraction.json + unique-assets.json into public/
// Run via: node scripts/download-assets.mjs

import { mkdir, writeFile, readFile, access } from 'node:fs/promises';
import { createWriteStream, existsSync } from 'node:fs';
import { dirname, extname, basename, join } from 'node:path';
import { pipeline } from 'node:stream/promises';
import { Readable } from 'node:stream';

const ROOT = process.cwd();
const PUBLIC_IMG = join(ROOT, 'public', 'images');
const PUBLIC_VID = join(ROOT, 'public', 'videos');
const PUBLIC_SEO = join(ROOT, 'public', 'seo');
const ICONS_DIR = join(PUBLIC_IMG, 'icons');
const PHOTOS_DIR = join(PUBLIC_IMG, 'photos');
const CLOUDS_DIR = join(PUBLIC_IMG, 'clouds');

await mkdir(PUBLIC_IMG, { recursive: true });
await mkdir(PUBLIC_VID, { recursive: true });
await mkdir(PUBLIC_SEO, { recursive: true });
await mkdir(ICONS_DIR, { recursive: true });
await mkdir(PHOTOS_DIR, { recursive: true });
await mkdir(CLOUDS_DIR, { recursive: true });

const extract = JSON.parse(await readFile(join(ROOT, 'docs/research/unique-assets.json'), 'utf-8'));
const tags = JSON.parse(await readFile(join(ROOT, 'docs/research/extraction.json'), 'utf-8'));

const targets = [];

for (const img of extract.uniqueImages) {
  const base = img.base; // strip query
  const filename = basename(base);
  const ext = extname(filename).toLowerCase();
  let dir = PUBLIC_IMG;
  if (ext === '.svg') dir = ICONS_DIR;
  else if (img.alt === 'Cloud') dir = CLOUDS_DIR;
  else if (ext === '.jpg' || ext === '.jpeg' || ext === '.png' || ext === '.webp') dir = PHOTOS_DIR;
  targets.push({ url: img.url, dest: join(dir, filename), kind: 'image' });
}

for (const v of extract.uniqueVideos) {
  const filename = basename(v.url.split('?')[0]);
  targets.push({ url: v.url, dest: join(PUBLIC_VID, filename), kind: 'video' });
  if (v.poster) {
    const pfile = basename(v.poster.split('?')[0]);
    targets.push({ url: v.poster, dest: join(PUBLIC_IMG, 'posters', pfile), kind: 'image' });
  }
}

// SEO favicons / OG
for (const f of tags.favicons || []) {
  if (!f.href) continue;
  const filename = basename(f.href.split('?')[0]);
  targets.push({ url: f.href, dest: join(PUBLIC_SEO, filename), kind: 'image' });
}
if (tags.metaTags?.ogImage) {
  const filename = basename(tags.metaTags.ogImage.split('?')[0]);
  targets.push({ url: tags.metaTags.ogImage, dest: join(PUBLIC_SEO, filename), kind: 'image' });
}

await mkdir(join(PUBLIC_IMG, 'posters'), { recursive: true });

console.log(`Downloading ${targets.length} assets...`);

async function downloadOne({ url, dest }) {
  await mkdir(dirname(dest), { recursive: true });
  if (existsSync(dest)) return { url, dest, skipped: true };
  try {
    const res = await fetch(url, {
      headers: {
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/120.0',
        'Referer': 'https://habitline-wbs.framer.website/',
      },
    });
    if (!res.ok) throw new Error(`${res.status} ${res.statusText}`);
    await pipeline(Readable.fromWeb(res.body), createWriteStream(dest));
    return { url, dest, ok: true };
  } catch (err) {
    return { url, dest, error: String(err) };
  }
}

const BATCH = 6;
const results = [];
for (let i = 0; i < targets.length; i += BATCH) {
  const batch = targets.slice(i, i + BATCH);
  const r = await Promise.all(batch.map(downloadOne));
  results.push(...r);
  process.stdout.write(`\r${Math.min(i + BATCH, targets.length)}/${targets.length}`);
}
console.log('');

const ok = results.filter((r) => r.ok || r.skipped).length;
const failed = results.filter((r) => r.error);
console.log(`✓ ${ok} downloaded/skipped, ✗ ${failed.length} failed`);
if (failed.length) {
  for (const f of failed.slice(0, 20)) console.log(`  - ${f.url}: ${f.error}`);
}
