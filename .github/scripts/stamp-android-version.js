const fs = require("node:fs");

const versionCode = Number(process.env.ANDROID_VERSION_CODE);
if (!Number.isInteger(versionCode) || versionCode < 1) {
  console.error("ANDROID_VERSION_CODE must be a positive integer.");
  process.exit(1);
}

const versionName = process.env.ANDROID_VERSION_NAME;
if (versionName !== undefined && versionName !== "" && !/^0\.[1-9]\d*$/.test(versionName)) {
  console.error("ANDROID_VERSION_NAME must look like 0.1 or 0.6. It stays below 1.");
  process.exit(1);
}

const lines = [
  `versionCode=${versionCode}`,
  `versionName=${versionName || "0.1"}`,
  "",
];
fs.writeFileSync("app/version.properties", lines.join("\n"));
console.log(`android.versionCode=${versionCode}`);
if (versionName) console.log(`android.versionName=${versionName}`);
