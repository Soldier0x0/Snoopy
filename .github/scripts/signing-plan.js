/**
 * Release signing choice for CI, matching Nucleus.
 * With no ANDROID_KEYSTORE_BASE64, use the bundled debug keystore.
 * When BASE64 is set, the other three secrets must all be present.
 */

function planSigning(env) {
  const base64 = String(env.ANDROID_KEYSTORE_BASE64 || "").trim();
  if (!base64) {
    return { mode: "debug", fail: false, reason: "debug" };
  }
  const missing = ["ANDROID_KEYSTORE_PASSWORD", "ANDROID_KEY_ALIAS", "ANDROID_KEY_PASSWORD"].filter(
    (name) => !String(env[name] || "").trim(),
  );
  if (missing.length > 0) {
    return {
      mode: "release",
      fail: true,
      reason: `ANDROID_KEYSTORE_BASE64 is set, but ${missing.join(", ")} is missing.`,
    };
  }
  return { mode: "release", fail: false, reason: "release" };
}

if (require.main === module) {
  const plan = planSigning(process.env);
  if (plan.fail) {
    console.error(plan.reason);
    process.exit(1);
  }
  process.stdout.write(`${plan.mode}\n`);
}

module.exports = { planSigning };
