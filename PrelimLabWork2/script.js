(function () {
    const GMAIL_REGEX = /^[a-zA-Z0-9._%+-]+@gmail\.com$/;
    const emailInput = document.getElementById('signupEmail');

    emailInput.addEventListener('input', () => {
      const val = emailInput.value.trim();
      emailInput.setCustomValidity(
        GMAIL_REGEX.test(val)
          ? ''
          : 'Please use a valid Gmail address (name@gmail.com).'
        );});})();

        // quick default creds — temporary
        let validCredentials = [
            { username: 'admin', password: 'password123', email: 'admin@gmail.com' },
            { username: 'student', password: 'password123', email: 'student@gmail.com' }
        ];

        // attendanceRecords array
        let attendanceRecords = [];

        // beep: local 'beep.mp3' — just play it
        const beepAudio = new Audio('beep.mp3');
        beepAudio.preload = 'auto';
        function playBeepSound() {
            try {
                beepAudio.currentTime = 0;
                // play() returns a promise; ignore failures (autoplay blockers)
                beepAudio.play().catch(() => {});
            } catch (e) {
                console.warn('Beep playback failed', e);
            }
        }

        // format timestamp (MM/DD/YYYY HH:MM:SS)
        function formatTimestamp(date) {
            const month = String(date.getMonth() + 1).padStart(2, '0');
            const day = String(date.getDate()).padStart(2, '0');
            const year = date.getFullYear();
            const hours = String(date.getHours()).padStart(2, '0');
            const minutes = String(date.getMinutes()).padStart(2, '0');
            const seconds = String(date.getSeconds()).padStart(2, '0');
            
            return `${month}/${day}/${year} ${hours}:${minutes}:${seconds}`;
        }

        // show section — hide the rest quickly
        function showSection(sectionId) {
            document.querySelectorAll('.section').forEach(section => {
                section.classList.remove('active');
            });
            document.getElementById(sectionId).classList.add('active');
        }

        // quick password checks
        function validatePassword(password) {
            const minLength = 8;
            const hasNumber = /\d/.test(password);
            const hasLetter = /[a-zA-Z]/.test(password);
            
            if (password.length < minLength) {
                return 'Your oath must contain at least 8 runes';
            }
            if (!hasNumber) {
                return 'Your oath must bear the mark of numbers';
            }
            if (!hasLetter) {
                return 'Your oath must bear the mark of letters';
            }
            return null;
        }

        // eye buttons toggle password visibility
        document.querySelectorAll('.eye-button').forEach(button => {
            button.addEventListener('click', function() {
                const targetId = this.getAttribute('data-target');
                const input = document.getElementById(targetId);
                
                if (input.type === 'password') {
                    input.type = 'text';
                    this.textContent = '👁️‍🗨️';
                } else {
                    input.type = 'password';
                    this.textContent = '👁️';
                }
            });
        });

        // render attendance list into modal (no ceremony)
        function displayAttendanceList() {
            const container = document.getElementById('attendanceListContainer');
            
            if (attendanceRecords.length === 0) {
                container.innerHTML = '<div class="no-records">No warriors have entered the saga yet...</div>';
            } else {
                let html = '<div class="attendance-list">';
                html += `<h3>⚔️ Total Warriors: ${attendanceRecords.length}</h3>`;
                
                attendanceRecords.slice().reverse().forEach((record, index) => {
                    html += `
                        <div class="attendance-item">
                            <div class="username">⚔️ ${record.username}</div>
                            <div class="timestamp">🕰️ ${record.timestamp}</div>
                        </div>
                    `;
                });
                
                html += '</div>';
                container.innerHTML = html;
            }
        }

        // open attendance modal
        document.getElementById('viewAttendanceBtn').addEventListener('click', function() {
            displayAttendanceList();
            document.getElementById('attendanceModal').classList.add('show');
        });

        // close modal button
        document.getElementById('closeModalBtn').addEventListener('click', function() {
            document.getElementById('attendanceModal').classList.remove('show');
        });

        // click outside -> close modal
        document.getElementById('attendanceModal').addEventListener('click', function(e) {
            if (e.target === this) {
                this.classList.remove('show');
            }
        });

        // switch views quickly
        document.getElementById('showSignup').addEventListener('click', function() {
            showSection('signupSection');
            document.getElementById('signupForm').reset();
            document.getElementById('signupError').classList.remove('show');
            document.getElementById('signupSuccess').classList.remove('show');
        });

        document.getElementById('showLogin').addEventListener('click', function() {
            showSection('loginSection');
            document.getElementById('loginForm').reset();
            document.getElementById('loginError').classList.remove('show');
        });

        // signup submit — quick validation, beep on error
        document.getElementById('signupForm').addEventListener('submit', function(e) {
            e.preventDefault();
            
            const username = document.getElementById('signupUsername').value.trim();
            const email = document.getElementById('signupEmail').value.trim();
            const password = document.getElementById('signupPassword').value;
            const confirmPassword = document.getElementById('signupConfirmPassword').value;
            const errorMessage = document.getElementById('signupError');
            const successMessage = document.getElementById('signupSuccess');
            
            // Hide previous messages
            errorMessage.classList.remove('show');
            successMessage.classList.remove('show');
            
            // Validate username length
            if (username.length < 3) {
                errorMessage.textContent = '⚔️ A warrior\'s name must be at least 3 characters';
                errorMessage.classList.add('show');
                return;
            }
            
            // Check if username already exists
            if (validCredentials.some(cred => cred.username === username)) {
                errorMessage.textContent = '⚔️ This name is already taken by another warrior!';
                errorMessage.classList.add('show');
                playBeepSound();
                return;
            }
            
            // Check if email already exists
            if (validCredentials.some(cred => cred.email === email)) {
                errorMessage.textContent = '⚔️ This scroll is already claimed!';
                errorMessage.classList.add('show');
                playBeepSound();
                return;
            }
            // Validate password strength
            const passwordError = validatePassword(password);
            if (passwordError) {
                errorMessage.textContent = `⚔️ ${passwordError}`;
                errorMessage.classList.add('show');
                playBeepSound();
                return;
            }
            // Check if passwords match
            if (password !== confirmPassword) {
                errorMessage.textContent = '⚔️ Your oaths do not match!';
                errorMessage.classList.add('show');
                playBeepSound();
                return;
            }
            // All validations passed, register the user
            validCredentials.push({ username, password, email });
            successMessage.classList.add('show');
            this.reset();
            setTimeout(() => {
                showSection('loginSection');
            }, 2000);
        });
        // login submit — check creds, beep on fail
        document.getElementById('loginForm').addEventListener('submit', function(e)
        {
            e.preventDefault();
            
            const username = document.getElementById('loginUsername').value.trim();
            const password = document.getElementById('loginPassword').value;
            const errorMessage = document.getElementById('loginError');
            
            // Hide previous error
            errorMessage.classList.remove('show');
            
            // Check credentials
            const user = validCredentials.find(cred => cred.username === username && cred.password === password);
            if (!user) {
                errorMessage.classList.add('show');
                playBeepSound();
                return;
            }
            // Successful login
            const timestamp = formatTimestamp(new Date());
            attendanceRecords.push({ username, timestamp });
            
            document.getElementById('welcomeUsername').textContent = username;
            document.getElementById('timestampDisplay').textContent = timestamp;
            
            this.reset();
            showSection('successSection');
        });
        // logout — back to login
        document.getElementById('logoutBtn').addEventListener('click', function() {
            showSection('loginSection');
        });
        // download attendance as .txt — quick and dirty
        document.getElementById('downloadBtn').addEventListener('click', function() {
            if (attendanceRecords.length === 0) {
                alert('No attendance records to download.');
                return;
            }

            // Build a readable text representation
            let txtContent = '';
            attendanceRecords.forEach(record => {
                txtContent += `Username: ${record.username}\tTimestamp: ${record.timestamp}\r\n`;
            });

            // Create a Blob with text and trigger download as .txt
            const blob = new Blob([txtContent], { type: 'text/plain;charset=utf-8' });
            const url = URL.createObjectURL(blob);
            const link = document.createElement('a');
            link.href = url;
            link.download = 'odyssey_attendance.txt';
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            URL.revokeObjectURL(url);
        });

        // modal download button — download the current attendance list from modal
        document.getElementById('downloadModalBtn').addEventListener('click', function() {
            if (attendanceRecords.length === 0) {
                alert('No attendance records to download.');
                return;
            }

            let txtContent = '';
            attendanceRecords.forEach(record => {
                txtContent += `Username: ${record.username}\tTimestamp: ${record.timestamp}\r\n`;
            });

            const blob2 = new Blob([txtContent], { type: 'text/plain;charset=utf-8' });
            const url2 = URL.createObjectURL(blob2);
            const link2 = document.createElement('a');
            link2.href = url2;
            link2.download = 'odyssey_attendance.txt';
            document.body.appendChild(link2);
            link2.click();
            document.body.removeChild(link2);
            URL.revokeObjectURL(url2);
        });