function nextAndroidVersion(tags) {
  let highest = 0;
  for (const tag of tags) {
    const match = /^v0\.(\d+)$/.exec(String(tag).trim());
    if (!match) continue;
    const minor = Number(match[1]);
    if (minor > highest) highest = minor;
  }
  return `0.${highest + 1}`;
}

if (require.main === module) {
  const tags = process.argv.slice(2).flatMap((arg) => arg.split(/\s+/).filter(Boolean));
  const name = nextAndroidVersion(tags);
  if (!/^0\.[1-9]\d*$/.test(name)) {
    console.error(`Refusing version ${name}. Releases stay at 0.1, 0.2, and so on.`);
    process.exit(1);
  }
  process.stdout.write(`${name}\n`);
}

module.exports = { nextAndroidVersion };
