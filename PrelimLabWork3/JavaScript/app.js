
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

/* Strict input guards: allow 1–4 for attendance, 0–100 for lab work */
function attachNumberGuards(ids) {
  ids.forEach(id => {
    const el = $(id);
    const isAttendance = id === 'attendance';
    const maxValue = isAttendance ? 4 : 100;
    const minValue = isAttendance ? 1 : 0;
    const allowDecimal = !isAttendance;

    // Block illegal keystrokes (letters, minus, spaces, etc.)
    el.addEventListener('beforeinput', (e) => {
      if (e.inputType === 'insertFromPaste') return; // handle in 'input'
      if (e.inputType.startsWith('delete')) return;  // allow deletions/backspace

      const data = e.data;
      if (data == null) return;

      // Allow digits only for attendance, digits and dot for lab work
      const allowedPattern = allowDecimal ? /[0-9.]/ : /[0-9]/;
      if (!allowedPattern.test(data)) {
        e.preventDefault();
        return;
      }

      // Only one dot allowed (for lab work only)
      if (allowDecimal && data === '.' && el.value.includes('.')) {
        e.preventDefault();
        return;
      }

      // Predict the next value
      const selStart = el.selectionStart ?? el.value.length;
      const selEnd   = el.selectionEnd ?? el.value.length;
      const next = el.value.slice(0, selStart) + data + el.value.slice(selEnd);

      // For lab work: reject leading zeros like "00", "01" (except "0." case)
      if (allowDecimal && /^0[0-9]/.test(next) && !/^0\./.test(next)) {
        e.preventDefault();
        return;
      }

      // For attendance: reject leading zeros
      if (!allowDecimal && /^0[0-9]/.test(next)) {
        e.preventDefault();
        return;
      }

      // If it's only a dot or starts with dot (lab work only), normalize to "0."
      if (allowDecimal && (next === '.' || next === '.0')) {
        return;
      }

      // Check numeric and within range
      if (allowDecimal) {
        if (/^\d*\.?\d*$/.test(next)) {
          const num = Number(next);
          const endsWithDot = next.endsWith('.');
          if (!Number.isNaN(num) && !endsWithDot) {
            if (num < 0 || num > maxValue) {
              e.preventDefault();
            }
          }
        } else {
          e.preventDefault();
        }
      } else {
        // Attendance: integers only
        if (/^\d+$/.test(next)) {
          const num = Number(next);
          if (num < 1 || num > maxValue) {
            e.preventDefault();
          }
        }
      }
    });

    // Sanitize pasted content & enforce range on every change
    el.addEventListener('input', () => {
      let v = el.value;

      if (allowDecimal) {
        // For lab work
        if (v.startsWith('.')) v = '0' + v;
        const parts = v.replace(/[^0-9.]/g, '').split('.');
        v = parts.shift() + (parts.length ? '.' + parts.join('') : '');

        if (/^0[0-9]/.test(v) && !/^0\./.test(v)) {
          v = v.replace(/^0+/, '');
          if (v === '') v = '0';
        }

        if (v !== '' && !v.endsWith('.')) {
          const num = Number(v);
          if (Number.isFinite(num)) {
            if (num < 0) v = '0';
            if (num > 100) v = '100';
          }
        }

        el.value = v;
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
      } else {
        // Attendance: integers only 1-4
        v = v.replace(/[^0-9]/g, '');
        if (v === '') {
          // Allow empty for now
        } else {
          const num = Number(v);
          if (num < 1) v = '1';
          if (num > 4) v = '4';
        }
        el.value = v;
        if (v === '') {
          el.setCustomValidity('Please enter total attendance (1-4).');
        } else {
          const num = Number(v);
          if (!Number.isFinite(num) || num < 1 || num > 4) {
            el.setCustomValidity('Please enter a number from 1 to 4.');
          } else {
            el.setCustomValidity('');
          }
        }
      }
    });

    // Final clamp on blur
    el.addEventListener('blur', () => {
      let v = el.value;
      if (v === '' || (allowDecimal && (v === '.' || v === '0.'))) {
        el.value = '';
        return;
      }
      let num = Number(v);
      if (!Number.isFinite(num)) {
        el.value = '';
        return;
      }
      if (num < 0) num = 0;
      if (num > maxValue) num = maxValue;
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
    const attendanceCount = Number($("attendance").value);
    
    // Check if attendance is less than 2 (meaning 3+ absences out of 4)
    if (attendanceCount < 2) {
      const lines = [];
      lines.push("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
      lines.push("  ❌ AUTOMATIC FAILURE");
      lines.push("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
      lines.push("");
      lines.push(`  Total Attendance: ${attendanceCount}/4`);
      lines.push("");
      lines.push("  You have exceeded 3 absences.");
      lines.push("  You are automatically FAILED.");
      lines.push("");
      lines.push("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
      
      out.textContent = lines.join("\n");
      return;
    }
    
    // Convert attendance count to percentage
    const attendancePercentage = (attendanceCount / 4) * 100;
    
    const lw1 = readDoubleRange($("lw1").value, 0, 100);
    const lw2 = readDoubleRange($("lw2").value, 0, 100);
    const lw3 = readDoubleRange($("lw3").value, 0, 100);

    const labWorkAverage = (lw1 + lw2 + lw3) / 3.0;
    const classStanding = (attendancePercentage * WEIGHT_ATTENDANCE) + (labWorkAverage * WEIGHT_LAB_AVG);

    const needPass = requiredExamForTarget(classStanding, TARGET_PASS);
    const needExcellent = requiredExamForTarget(classStanding, TARGET_EXCELLENT);

    const lines = [];
    lines.push("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    lines.push("  📊 YOUR GRADES");
    lines.push("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    lines.push(`  Total Attendance:  ${attendanceCount}/4`);
    lines.push(`  Attendance %:      ${fmt(attendancePercentage)}`);
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
