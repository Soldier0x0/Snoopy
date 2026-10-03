const fs = require("node:fs");

const SPEED_PROPERTIES = ["org.gradle.caching=true", "org.gradle.parallel=true"];

function withGradleSpeedProperties(contents) {
  const base = contents.endsWith("\n") || contents.length === 0 ? contents : `${contents}\n`;
  return `${base}${SPEED_PROPERTIES.join("\n")}\n`;
}

function main() {
  const file = process.argv[2];
  if (!file) {
    console.error("Usage: node .github/scripts/gradle-properties.js <gradle.properties>");
    process.exit(1);
  }
  const next = withGradleSpeedProperties(fs.readFileSync(file, "utf8"));
  fs.writeFileSync(file, next);
}

if (require.main === module) main();

module.exports = { withGradleSpeedProperties };
