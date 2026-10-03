const assert = require("node:assert/strict");
const test = require("node:test");
const { planSigning } = require("./signing-plan");

test("no secrets uses the bundled debug keystore", () => {
  assert.deepEqual(planSigning({}), { mode: "debug", fail: false, reason: "debug" });
});

test("base64 alone fails when the other secrets are missing", () => {
  const plan = planSigning({ ANDROID_KEYSTORE_BASE64: "abc" });
  assert.equal(plan.fail, true);
  assert.match(plan.reason, /ANDROID_KEYSTORE_PASSWORD/);
});

test("all four secrets select release signing", () => {
  assert.deepEqual(
    planSigning({
      ANDROID_KEYSTORE_BASE64: "abc",
      ANDROID_KEYSTORE_PASSWORD: "store",
      ANDROID_KEY_ALIAS: "alias",
      ANDROID_KEY_PASSWORD: "key",
    }),
    { mode: "release", fail: false, reason: "release" },
  );
});
