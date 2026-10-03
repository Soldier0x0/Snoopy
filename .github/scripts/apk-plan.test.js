const assert = require("node:assert/strict");
const test = require("node:test");
const { artifactName, isDocsOnly, planApkRun } = require("./apk-plan");

test("docs, changelog, and README do not need an APK", () => {
  assert.equal(isDocsOnly(["README.md", "CHANGELOG.md", "docs/plan.md"]), true);
  assert.equal(isDocsOnly(["app/src/main/kotlin/com/snoopy/app/MainActivity.kt"]), false);
});

test("the artifact name is the git tree", () => {
  const tree = "0123456789abcdef0123456789abcdef01234567";
  assert.equal(artifactName(tree), `snoopy-apk-${tree}`);
});

test("a code change compiles", () => {
  const plan = planApkRun({ event: "pull_request", files: ["app/build.gradle.kts", "CHANGELOG.md"] });
  assert.equal(plan.build, true);
});
