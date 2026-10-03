const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");
const test = require("node:test");
const { assertThisReleaseOnly, changelogSection, prepareRelease, readReleaseNotes, releaseNotes } = require("./release-notes");

const sample = `# Changelog

Intro.

## v0.2

Same app as v0.1. Notes are required.

## v0.1

- First weigh-in screen
- Share one session file

Not in this build: body-fat parsing.
`;

test("a section stops at the next release heading", () => {
  assert.equal(changelogSection(sample, "v0.2"), "Same app as v0.1. Notes are required.");
  const older = changelogSection(sample, "v0.1");
  assert.match(older, /weigh-in screen/);
  assert.doesNotMatch(older, /Notes are required/);
});

test("notes keep the install lines around the changelog", () => {
  const notes = releaseNotes({ changelog: sample, tag: "v0.1" });
  assert.match(notes, /^Snoopy v0\.1 for Android\.\n/);
  assert.match(notes, /Share one session file/);
  assert.match(notes, /Download snoopy-v0\.1\.apk/);
  assert.match(notes, /Do not uninstall/);
});

test("a pull request cannot pre-seed the following version", () => {
  const changelog = ["## v0.2", "", "- Later.", "", "## v0.1", "", "- First weigh-in screen.", ""].join("\n");
  assert.throws(() => assertThisReleaseOnly({ changelog, tag: "v0.1" }), /v0\.2/);
});

test("a note that only says the app is the same does not publish", () => {
  const prose = "## v0.1\n\nThe app on the phone is the same as v0.0. Nothing in the app changed.\n";
  assert.throws(() => prepareRelease({ changelog: prose, tag: "v0.1" }), /same as the previous one/);
});

test("the repository changelog documents v0.1", () => {
  const changelogPath = path.join(__dirname, "..", "..", "CHANGELOG.md");
  const notes = readReleaseNotes("v0.1", changelogPath);
  assert.match(notes, /Bluetooth Low Energy/);
  assert.match(notes, /snoopy-v0\.1\.apk/);
  const changelog = fs.readFileSync(changelogPath, "utf8");
  assert.equal(prepareRelease({ changelog, tag: "v0.1" }).publish, true);
  assert.deepEqual(prepareRelease({ changelog, tag: "v0.2" }), { publish: false, reason: "missing" });
});
