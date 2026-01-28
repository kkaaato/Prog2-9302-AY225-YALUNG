
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

/* Custom confirmation dialog with Yes/No buttons */
function showConfirmDialog(message) {
  return new Promise((resolve) => {
    const overlay = document.createElement('div');
    overlay.style.cssText = `
      position: fixed; top: 0; left: 0; width: 100%; height: 100%;
      background: rgba(0, 0, 0, 0.5); display: flex; align-items: center;
      justify-content: center; z-index: 10000;
    `;

    const dialog = document.createElement('div');
    dialog.style.cssText = `
      background: white; padding: 20px; border-radius: 8px;
      box-shadow: 0 4px 6px rgba(0, 0, 0, 0.2); max-width: 400px;
      text-align: center; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Arial, sans-serif;
    `;

    const text = document.createElement('p');
    text.textContent = message;
    text.style.cssText = `margin: 0 0 20px 0; font-size: 16px; color: #333;`;

    const buttonContainer = document.createElement('div');
    buttonContainer.style.cssText = `display: flex; gap: 10px; justify-content: center;`;

    const yesBtn = document.createElement('button');
    yesBtn.textContent = 'Yes';
    yesBtn.style.cssText = `
      padding: 8px 20px; background: #39FF14; color: white; border: none;
      border-radius: 4px; cursor: pointer; font-weight: bold;
    `;
    yesBtn.onclick = () => {
      overlay.remove();
      resolve(true);
    };

    const noBtn = document.createElement('button');
    noBtn.textContent = 'No';
    noBtn.style.cssText = `
      padding: 8px 20px; background: #FF1493; color: white; border: none;
      border-radius: 4px; cursor: pointer; font-weight: bold;
    `;
    noBtn.onclick = () => {
      overlay.remove();
      resolve(false);
    };

    buttonContainer.appendChild(yesBtn);
    buttonContainer.appendChild(noBtn);
    dialog.appendChild(text);
    dialog.appendChild(buttonContainer);
    overlay.appendChild(dialog);
    document.body.appendChild(overlay);
  });
}

/* Custom prompt dialog for excused absences */
function showExcusedAbsencesDialog(maxValue) {
  return new Promise((resolve) => {
    const overlay = document.createElement('div');
    overlay.style.cssText = `
      position: fixed; top: 0; left: 0; width: 100%; height: 100%;
      background: rgba(0, 0, 0, 0.5); display: flex; align-items: center;
      justify-content: center; z-index: 10000;
    `;

    const dialog = document.createElement('div');
    dialog.style.cssText = `
      background: white; padding: 20px; border-radius: 8px;
      box-shadow: 0 4px 6px rgba(0, 0, 0, 0.2); max-width: 400px;
      text-align: center; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Arial, sans-serif;
    `;

    const label = document.createElement('p');
    label.textContent = 'Select number of excused absences:';
    label.style.cssText = `margin: 0 0 15px 0; font-size: 16px; color: #333;`;

    const inputContainer = document.createElement('div');
    inputContainer.style.cssText = `display: flex; align-items: center; gap: 10px; margin-bottom: 15px;`;

    const input = document.createElement('input');
    input.type = 'text';
    input.value = '0';
    input.readOnly = true;
    input.style.cssText = `
      flex: 1; padding: 8px; border: 2px solid #800000;
      border-radius: 4px; font-size: 14px; box-sizing: border-box;
      text-align: center; font-weight: bold;
    `;

    // Prevent any text input
    input.addEventListener('beforeinput', (e) => e.preventDefault());
    input.addEventListener('keydown', (e) => {
      if (e.key === 'ArrowUp') {
        e.preventDefault();
        const current = Number(input.value);
        if (current < maxValue) {
          input.value = String(current + 1);
        }
      } else if (e.key === 'ArrowDown') {
        e.preventDefault();
        const current = Number(input.value);
        if (current > 0) {
          input.value = String(current - 1);
        }
      } else if (!/[ArrowUp|ArrowDown]/.test(e.key)) {
        e.preventDefault();
      }
    });

    const upBtn = document.createElement('button');
    upBtn.textContent = '▲';
    upBtn.style.cssText = `
      padding: 8px 12px; background: #39FF14; color: white; border: none;
      border-radius: 4px; cursor: pointer; font-weight: bold; font-size: 14px;
    `;
    upBtn.onclick = () => {
      const current = Number(input.value);
      if (current < maxValue) {
        input.value = String(current + 1);
      }
    };

    const downBtn = document.createElement('button');
    downBtn.textContent = '▼';
    downBtn.style.cssText = `
      padding: 8px 12px; background: #39FF14; color: white; border: none;
      border-radius: 4px; cursor: pointer; font-weight: bold; font-size: 14px;
    `;
    downBtn.onclick = () => {
      const current = Number(input.value);
      if (current > 0) {
        input.value = String(current - 1);
      }
    };

    inputContainer.appendChild(input);
    inputContainer.appendChild(upBtn);
    inputContainer.appendChild(downBtn);

    const buttonContainer = document.createElement('div');
    buttonContainer.style.cssText = `display: flex; gap: 10px; justify-content: center;`;

    const okBtn = document.createElement('button');
    okBtn.textContent = 'OK';
    okBtn.style.cssText = `
      padding: 8px 20px; background: #39FF14; color: white; border: none;
      border-radius: 4px; cursor: pointer; font-weight: bold;
    `;
    okBtn.onclick = () => {
      overlay.remove();
      resolve(Number(input.value));
    };

    const cancelBtn = document.createElement('button');
    cancelBtn.textContent = 'Cancel';
    cancelBtn.style.cssText = `
      padding: 8px 20px; background: #FF1493; color: white; border: none;
      border-radius: 4px; cursor: pointer; font-weight: bold;
    `;
    cancelBtn.onclick = () => {
      overlay.remove();
      resolve(null);
    };

    buttonContainer.appendChild(okBtn);
    buttonContainer.appendChild(cancelBtn);
    dialog.appendChild(label);
    dialog.appendChild(inputContainer);
    dialog.appendChild(buttonContainer);
    overlay.appendChild(dialog);
    document.body.appendChild(overlay);

    input.focus();
  });
}

/* Validation */
function readDoubleRange(value, minInclusive, maxInclusive) {
  const v = Number(value);
  if (!Number.isFinite(v) || v < minInclusive || v > maxInclusive) {
    throw new Error(`Value must be a number between ${minInclusive} and ${maxInclusive}.`);
  }
  return v;
}

/* Strict input guards: allow 0–4 for attendance/excused, 0–100 for lab work */
function attachNumberGuards(ids) {
  ids.forEach(id => {
    const el = $(id);
    const isAttendance = id === 'attendance';
    const isExcused = id === 'excused';
    const maxValue = (isAttendance || isExcused) ? 5 : 100;
    const minValue = 0;
    const allowDecimal = !isAttendance && !isExcused;

    // Block illegal keystrokes (letters, minus, spaces, etc.)
    el.addEventListener('beforeinput', (e) => {
      if (e.inputType === 'insertFromPaste') return; // handle in 'input'
      if (e.inputType.startsWith('delete')) return;  // allow deletions/backspace

      const data = e.data;
      if (data == null) return;

      // Allow digits only for attendance, digits only for lab work (no decimals)
      const allowedPattern = /[0-9]/;
      if (!allowedPattern.test(data)) {
        e.preventDefault();
        return;
      }

      // Predict the next value
      const selStart = el.selectionStart ?? el.value.length;
      const selEnd   = el.selectionEnd ?? el.value.length;
      const next = el.value.slice(0, selStart) + data + el.value.slice(selEnd);

      // Reject leading zeros like "00", "01"
      if (/^0[0-9]/.test(next)) {
        e.preventDefault();
        return;
      }

      // Check numeric and within range (all fields are integers now)
      if (/^\d+$/.test(next)) {
        const num = Number(next);
        if (num < 0 || num > maxValue) {
          e.preventDefault();
        }
      } else {
        e.preventDefault();
      }
    });

    // Sanitize pasted content & enforce range on every change
    el.addEventListener('input', () => {
      let v = el.value;

      // All fields are now integers only (no decimals)
      v = v.replace(/[^0-9]/g, '');
      if (v === '') {
        // Allow empty for now
      } else {
        // Reject leading zeros like "00", "01"
        if (/^0[0-9]/.test(v)) {
          v = v.replace(/^0+/, '');
          if (v === '') v = '0';
        }

        const num = Number(v);
        if (num < 0) v = '0';
        if (num > maxValue) v = String(maxValue);
      }
      el.value = v;

      if (v === '') {
        if (isAttendance || isExcused) {
          el.setCustomValidity(`Please enter ${isExcused ? 'excused absences' : 'total attendance'} (0-5).`);
        } else {
          el.setCustomValidity('Please enter a number from 0 to 100.');
        }
      } else {
        const num = Number(v);
        if (!Number.isFinite(num) || num < 0 || num > maxValue) {
          el.setCustomValidity(`Please enter a number from 0 to ${maxValue}.`);
        } else {
          el.setCustomValidity('');
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
    // excused field defaults to 0, so allow it to be empty
    if (id === 'excused') return false;
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
    const difficulty = getDifficultyAssessment(required);
    lines.push(`    📝 Need: ${fmt(required)} ${difficulty}`);
  }
  return lines.join("\n");
}

function getDifficultyAssessment(requiredScore) {
  if (requiredScore <= 30) {
    return "✅ (Very Achievable!)";
  } else if (requiredScore <= 50) {
    return "✅ (Achievable)";
  } else if (requiredScore <= 75) {
    return "⚠️ (Moderate Difficulty)";
  } else {
    return "❌ (Very Challenging)";
  }
}

/* UI handlers */
function calculate() {
  const out = $("result");
  out.textContent = "";

  const ids = ["attendance", "lw1", "lw2", "lw3"];
  if (!ensureAllInputsFilled(ids)) return;

  try {
    const totalSessions = 5;
    const attendanceCount = Number($("attendance").value);
    const missingSessions = totalSessions - attendanceCount;
    
    let excusedAbsences = 0;

    // Ask for excused absences only if attendance is incomplete
    if (attendanceCount < totalSessions) {
      showConfirmDialog(`You have ${missingSessions} absence(s).\nAny excused absences?`)
        .then(async (hasExcused) => {
          if (hasExcused) {
            const input = await showExcusedAbsencesDialog(missingSessions);
            
            if (input !== null) {
              excusedAbsences = input;
            }
          }
          
          // Continue with calculation
          performCalculation(attendanceCount, excusedAbsences);
        });
    } else {
      // No absences, proceed directly
      performCalculation(attendanceCount, 0);
    }
  } catch (err) {
    alert(`Input Error: ${err.message}`);
  }
}

function performCalculation(attendanceCount, excusedAbsences) {
  const out = $("result");
  const totalSessions = 5;

  try {
    // Calculate unexcused absences
    const unexcusedAbsences = Math.max(0, totalSessions - attendanceCount - excusedAbsences);

    // Auto-fail rule: 4 or more unexcused absences
    if (unexcusedAbsences >= 4) {
      const lines = [];
      lines.push("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
      lines.push("  ❌ AUTOMATIC FAILURE");
      lines.push("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
      lines.push("");
      lines.push(`  Present:            ${attendanceCount}/${totalSessions}`);
      lines.push(`  Excused Absences:   ${excusedAbsences}`);
      lines.push(`  Unexcused Absences: ${unexcusedAbsences}`);
      lines.push("");
      lines.push("  You have 4 or more UNEXCUSED absences.");
      lines.push("  You are automatically FAILED.");
      lines.push("");
      lines.push("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
      
      out.textContent = lines.join("\n");
      return;
    }
    
    // Calculate effective attendance (present + excused, capped at total sessions)
    const effectiveAttendance = Math.min(totalSessions, attendanceCount + excusedAbsences);
    const attendancePercentage = (effectiveAttendance / totalSessions) * 100;
    
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
    lines.push(`  Present:            ${attendanceCount}/${totalSessions}`);
    lines.push(`  Excused Absences:   ${excusedAbsences}`);
    lines.push(`  Unexcused Absences: ${unexcusedAbsences}`);
    lines.push(`  Effective Attend.:  ${effectiveAttendance}/${totalSessions}`);
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
    
    // Check if both grades are not achievable
    if (needPass > 100 && needExcellent > 100) {
      lines.push("  ❌ GOAL NOT ACHIEVABLE");
      lines.push("");
      lines.push("  Your current grades are too low to achieve");
      lines.push("  a passing or excellent score, even with a");
      lines.push("  perfect exam.");
      lines.push("");
      lines.push("  💪 Don't give up! Try again next year");
      lines.push("  with better preparation.");
    } else {
      lines.push(buildRequirement(needPass, "To PASS (75):"));
      lines.push("");
      lines.push(buildRequirement(needExcellent, "For EXCELLENT (100):"));
    }
    
    lines.push("");
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
