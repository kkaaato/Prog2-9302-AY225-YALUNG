
/* === Weights & Targets === */
const WEIGHT_EXAM = 0.30;
const WEIGHT_CLASS_STANDING = 0.70;

const WEIGHT_ATTENDANCE = 0.40;  // within Class Standing
const WEIGHT_LAB_AVG   = 0.60;   // within Class Standing

const TARGET_PASS = 75;
const TARGET_EXCELLENT = 100;

/* === Helpers === */
const $ = (id) => document.getElementById(id);
const fmt = (n) => Number.isFinite(n) ? n.toFixed(2) : "—";

/* Validation */
function readDoubleRange(value, minInclusive, maxInclusive) {
  const v = Number(value);
  if (!Number.isFinite(v) || v < minInclusive || v > maxInclusive) {
    throw new Error(`Value must be a number between ${minInclusive} and ${maxInclusive}.`);
  }
  return v;
}

/* Strict input guards: only allow 0–100, digits and a single dot, with live range enforcement */
function attachNumberGuards(ids) {
  ids.forEach(id => {
    const el = $(id);

    // Block illegal keystrokes (letters, minus, spaces, multiple dots, etc.)
    el.addEventListener('beforeinput', (e) => {
      if (e.inputType === 'insertFromPaste') return; // handle in 'input'
      if (e.inputType.startsWith('delete')) return;  // allow deletions/backspace

      const data = e.data;
      if (data == null) return;

      // Allow digits and dot only
      if (!/[0-9.]/.test(data)) {
        e.preventDefault();
        return;
      }

      // Only one dot allowed
      if (data === '.' && el.value.includes('.')) {
        e.preventDefault();
        return;
      }

      // Predict the next value
      const selStart = el.selectionStart ?? el.value.length;
      const selEnd   = el.selectionEnd ?? el.value.length;
      const next = el.value.slice(0, selStart) + data + el.value.slice(selEnd);

      // Allow "0" or "0." or "100" partially while typing
      // Reject leading zeros like "00", "01" (except "0." case)
      if (/^0[0-9]/.test(next) && !/^0\./.test(next)) {
        e.preventDefault();
        return;
      }

      // If it's only a dot or starts with dot, normalize to "0."
      if (next === '.' || next === '.0') {
        // Let it pass; we'll normalize in 'input'
        return;
      }

      // Check numeric and within 0–100 when complete number
      if (/^\d*\.?\d*$/.test(next)) {
        const num = Number(next);
        // If next ends with a dot, user is still typing => allow
        const endsWithDot = next.endsWith('.');
        if (!Number.isNaN(num) && !endsWithDot) {
          if (num < 0 || num > 100) {
            e.preventDefault();
          }
        }
      } else {
        e.preventDefault();
      }
    });

    // Sanitize pasted content & enforce range on every change
    el.addEventListener('input', () => {
      let v = el.value;

      // Normalize leading dot to "0."
      if (v.startsWith('.')) v = '0' + v;

      // Remove invalid chars (keep digits and at most one dot)
      const parts = v.replace(/[^0-9.]/g, '').split('.');
      v = parts.shift() + (parts.length ? '.' + parts.join('') : '');

      // Prevent multi leading zeros like "00" (allow "0." case)
      if (/^0[0-9]/.test(v) && !/^0\./.test(v)) {
        // drop the extra leading zero(s)
        v = v.replace(/^0+/, '');
        if (v === '') v = '0';
      }

      // Clamp to [0, 100] if it parses and doesn't end with dot
      if (v !== '' && !v.endsWith('.')) {
        const num = Number(v);
        if (Number.isFinite(num)) {
          if (num < 0) v = '0';
          if (num > 100) v = '100';
        }
      }

      el.value = v;
      // Optional: live validity hint
      if (v === '' || v === '.' || v === '0.') {
        el.setCustomValidity('Please enter a number from 0 to 100.');
      } else {
        const num = Number(v);
        if (!Number.isFinite(num) || num < 0 || num > 100) {
          el.setCustomValidity('Please enter a number from 0 to 100.');
        } else {
          el.setCustomValidity('');
        }
      }
    });

    // Final clamp on blur (ensures "0.", "." become "0")
    el.addEventListener('blur', () => {
      let v = el.value;
      if (v === '' || v === '.' || v === '0.') {
        el.value = '';
        return;
      }
      let num = Number(v);
      if (!Number.isFinite(num)) {
        el.value = '';
        return;
      }
      if (num < 0) num = 0;
      if (num > 100) num = 100;
      el.value = String(num);
    });
  });
}

/* Check required inputs and highlight any missing */
function ensureAllInputsFilled(ids) {
  ids.forEach(id => $(id).classList.remove('invalid'));

  const missing = ids.filter(id => {
    const el = $(id);
    return el.value === "" || el.value === null;
  });

  if (missing.length > 0) {
    missing.forEach(id => $(id).classList.add('invalid'));
    $(missing[0]).focus();
    alert("Please enter all required grades (Attendance, Lab Work 1, Lab Work 2, Lab Work 3).");
    return false;
  }
  return true;
}

/* Core math */
function requiredExamForTarget(classStanding, target) {
  // PrelimGrade = 0.30 * Exam + 0.70 * ClassStanding
  return (target - (WEIGHT_CLASS_STANDING * classStanding)) / WEIGHT_EXAM;
}

function buildRequirement(required, label) {
  const lines = [];
  lines.push(`  ${label}`);
  if (required <= 0) {
    lines.push(`    ✅ Already meets or exceeds this target.`);
  } else if (required > 100) {
    lines.push(`    ❌ Not Achievable`);
  } else {
    lines.push(`    📝 Need: ${fmt(required)}`);
  }
  return lines.join("\n");
}

/* UI handlers */
function calculate() {
  const out = $("result");
  out.textContent = "";

  const ids = ["attendance", "lw1", "lw2", "lw3"];
  if (!ensureAllInputsFilled(ids)) return;

  try {
    const attendanceScore = readDoubleRange($("attendance").value, 0, 100);
    const lw1 = readDoubleRange($("lw1").value, 0, 100);
    const lw2 = readDoubleRange($("lw2").value, 0, 100);
    const lw3 = readDoubleRange($("lw3").value, 0, 100);

    const labWorkAverage = (lw1 + lw2 + lw3) / 3.0;
    const classStanding = (attendanceScore * WEIGHT_ATTENDANCE) + (labWorkAverage * WEIGHT_LAB_AVG);

    const needPass = requiredExamForTarget(classStanding, TARGET_PASS);
    const needExcellent = requiredExamForTarget(classStanding, TARGET_EXCELLENT);

    const lines = [];
    lines.push("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    lines.push("  📊 YOUR GRADES");
    lines.push("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    lines.push(`  Attendance score:  ${fmt(attendanceScore)}`);
    lines.push(`  Lab Work 1:        ${fmt(lw1)}`);
    lines.push(`  Lab Work 2:        ${fmt(lw2)}`);
    lines.push(`  Lab Work 3:        ${fmt(lw3)}`);
    lines.push(`  Lab Work Avg:      ${fmt(labWorkAverage)}`);
    lines.push(`  Class Standing:    ${fmt(classStanding)}`);
    lines.push("");
    lines.push("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    lines.push("  🎯 REQUIRED PRELIM EXAM SCORES");
    lines.push("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    lines.push("");
    lines.push(buildRequirement(needPass, "To PASS (75):"));
    lines.push("");
    lines.push(buildRequirement(needExcellent, "For EXCELLENT (100):"));
    lines.push("");
    lines.push("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    lines.push("🍀Good lcuk on your prelim examls!🍀");
    lines.push("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

    out.textContent = lines.join("\n");
  } catch (err) {
    alert(`Input Error: ${err.message}`);
  }
}

function clearAll() {
  ["attendance", "lw1", "lw2", "lw3"].forEach(id => $(id).value = "");
  ["attendance", "lw1", "lw2", "lw3"].forEach(id => $(id).classList.remove('invalid'));
  $("result").textContent = "";
  $("attendance").focus();
}

/* Wire events */
window.addEventListener("DOMContentLoaded", () => {
  const ids = ["attendance", "lw1", "lw2", "lw3"];
  attachNumberGuards(ids);

  $("btnCalc").addEventListener("click", calculate);
  $("btnClear").addEventListener("click", clearAll);

  // Press Enter to calculate from any input
  ids.forEach(id => {
    $(id).addEventListener("keydown", (e) => {
      if (e.key === "Enter") calculate();
    });
  });
});
