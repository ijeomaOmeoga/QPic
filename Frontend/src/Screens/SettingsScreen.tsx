import React, { useEffect, useRef, useState } from 'react';

type Appearance = 'system' | 'light' | 'dark' | 'blue' | 'red';

const appearances: { id: Appearance; label: string; detail: string }[] = [
	{ id: 'system', label: 'System', detail: 'Follow your device settings' },
	{ id: 'light', label: 'Light mode', detail: 'A bright, clean look' },
	{ id: 'dark', label: 'Dark mode', detail: 'Easy on the eyes' },
	{ id: 'blue', label: 'Blue gradient', detail: 'Blue fading into black' },
	{ id: 'red', label: 'Red gradient', detail: 'Red fading into black' },
];

export default function SettingsScreen() {
	const [username, setUsername] = useState('Your username');
	const [draftUsername, setDraftUsername] = useState(username);
	const [editing, setEditing] = useState(false);
	const [picture, setPicture] = useState('');
	const [appearance, setAppearance] = useState<Appearance>('system');
	const [systemDark, setSystemDark] = useState(false);
	const fileInput = useRef<HTMLInputElement>(null);

	useEffect(() => {
		const query = window.matchMedia('(prefers-color-scheme: dark)');
		const update = () => setSystemDark(query.matches);
		update();
		query.addEventListener?.('change', update);
		return () => query.removeEventListener?.('change', update);
	}, []);

	const theme = appearance === 'system' ? (systemDark ? 'dark' : 'light') : appearance;
	const saveUsername = () => {
		const trimmed = draftUsername.trim();
		if (trimmed) setUsername(trimmed);
		else setDraftUsername(username);
		setEditing(false);
	};

	const onPictureSelected = (event: React.ChangeEvent<HTMLInputElement>) => {
		const file = event.target.files?.[0];
		if (file) setPicture(URL.createObjectURL(file));
	};

	return (
		<main className={`settings-screen theme-${theme}`}>
			<style>{`
				* { box-sizing: border-box; }
				.settings-screen { --bg:#f4f6fb; --surface:#fff; --text:#192033; --muted:#758096; --line:#e7eaf1; --accent:#5267e8; min-height:100vh; padding:48px 20px; color:var(--text); background:var(--bg); font-family:Inter,ui-sans-serif,system-ui,-apple-system,BlinkMacSystemFont,"Segoe UI",sans-serif; transition:background .2s,color .2s; }
				.settings-screen.theme-dark { --bg:#11141c; --surface:#1b202b; --text:#f2f4fa; --muted:#a0a9ba; --line:#303746; --accent:#8493ff; }
				.settings-screen.theme-blue { --bg:linear-gradient(135deg,#1267d8,#10131d 68%); --surface:rgba(22,27,39,.94); --text:#f4f6ff; --muted:#b0bdd2; --line:rgba(255,255,255,.12); --accent:#79aaff; }
				.settings-screen.theme-red { --bg:linear-gradient(135deg,#c83242,#10131d 68%); --surface:rgba(22,27,39,.94); --text:#fff5f5; --muted:#d0b6ba; --line:rgba(255,255,255,.12); --accent:#ff929d; }
				.settings-screen.theme-blue,.settings-screen.theme-red { background:var(--bg); }
				.settings-container { width:min(760px,100%); margin:0 auto; }
				.settings-heading { margin:0 0 8px; font-size:32px; letter-spacing:-.7px; }
				.settings-intro { margin:0 0 30px; color:var(--muted); font-size:15px; }
				.settings-card { background:var(--surface); border:1px solid var(--line); border-radius:16px; padding:24px; margin-bottom:18px; box-shadow:0 8px 26px rgba(20,30,60,.045); }
				.settings-card h2 { margin:0 0 18px; font-size:17px; }
				.profile-row { display:flex; align-items:center; gap:18px; }
				.avatar { width:68px; height:68px; flex:none; border-radius:50%; display:grid; place-items:center; overflow:hidden; background:linear-gradient(135deg,#8296ff,#5267e8); color:#fff; font-size:24px; font-weight:700; }
				.avatar img { width:100%; height:100%; object-fit:cover; }
				.profile-info { flex:1; min-width:0; }
				.profile-info label { display:block; margin-bottom:6px; color:var(--muted); font-size:12px; font-weight:600; }
				.username-input { width:100%; max-width:360px; padding:10px 12px; border:1px solid var(--line); border-radius:8px; background:var(--bg); color:var(--text); font:inherit; }
				.profile-actions { display:flex; flex-wrap:wrap; gap:8px; margin-top:10px; }
				.settings-button { border:1px solid var(--line); border-radius:8px; padding:8px 12px; color:var(--text); background:transparent; font:inherit; font-size:13px; font-weight:600; cursor:pointer; }
				.settings-button:hover,.appearance-option:hover,.link-row:hover { border-color:var(--accent); }
				.settings-button.primary { color:#fff; background:var(--accent); border-color:var(--accent); }
				.appearance-grid { display:grid; grid-template-columns:repeat(auto-fit,minmax(190px,1fr)); gap:10px; }
				.appearance-option { display:flex; align-items:center; gap:11px; min-height:66px; padding:12px; text-align:left; color:var(--text); background:var(--surface); border:1px solid var(--line); border-radius:10px; cursor:pointer; }
				.appearance-option[aria-pressed="true"] { border-color:var(--accent); box-shadow:0 0 0 1px var(--accent); }
				.theme-swatch { width:25px; height:25px; flex:none; border-radius:50%; border:1px solid var(--line); }
				.swatch-system { background:linear-gradient(90deg,#fff 50%,#202532 50%); }
				.swatch-light { background:#f4f6fb; }
				.swatch-dark { background:#202532; }
				.swatch-blue { background:linear-gradient(135deg,#1267d8,#10131d); }
				.swatch-red { background:linear-gradient(135deg,#c83242,#10131d); }
				.appearance-copy { display:flex; flex-direction:column; gap:3px; }
				.appearance-copy strong { font-size:13px; }
				.appearance-copy small { color:var(--muted); font-size:11px; }
				.link-row { display:flex; justify-content:space-between; align-items:center; padding:15px 0; color:var(--text); text-decoration:none; border-bottom:1px solid var(--line); }
				.link-row:last-child { padding-bottom:0; border-bottom:0; }
				.link-arrow { color:var(--muted); font-size:20px; }
				.logout-card { padding:0; overflow:hidden; }
				.logout-button { width:100%; padding:18px 24px; text-align:left; border:0; color:#d94855; background:transparent; font:inherit; font-weight:650; cursor:pointer; }
				.logout-button:hover { background:rgba(217,72,85,.07); }
				@media(max-width:520px) { .settings-screen { padding:30px 15px; } .settings-heading { font-size:28px; } .settings-card { padding:19px; } .profile-row { align-items:flex-start; } }
			`}</style>

			<div className="settings-container">
				<h1 className="settings-heading">Settings</h1>
				<p className="settings-intro">Manage your profile and personalize your experience.</p>

				<section className="settings-card" aria-labelledby="profile-heading">
					<h2 id="profile-heading">Profile</h2>
					<div className="profile-row">
						<div className="avatar" aria-label="Profile picture">
							{picture ? <img src={picture} alt="Profile" /> : username.trim().charAt(0).toUpperCase() || 'U'}
						</div>
						<div className="profile-info">
							<label htmlFor="username">Username</label>
							{editing ? (
								<input id="username" className="username-input" value={draftUsername} maxLength={40} autoFocus onChange={event => setDraftUsername(event.target.value)} onKeyDown={event => { if (event.key === 'Enter') saveUsername(); if (event.key === 'Escape') { setDraftUsername(username); setEditing(false); } }} />
							) : <div>{username}</div>}
							<div className="profile-actions">
								{editing ? <>
									<button className="settings-button primary" onClick={saveUsername}>Save</button>
									<button className="settings-button" onClick={() => { setDraftUsername(username); setEditing(false); }}>Cancel</button>
								</> : <button className="settings-button" onClick={() => { setDraftUsername(username); setEditing(true); }}>Edit username</button>}
								<button className="settings-button" onClick={() => fileInput.current?.click()}>Change picture</button>
								<input ref={fileInput} type="file" accept="image/*" hidden onChange={onPictureSelected} aria-label="Choose profile picture" />
							</div>
						</div>
					</div>
				</section>

				<section className="settings-card" aria-labelledby="appearance-heading">
					<h2 id="appearance-heading">Appearance</h2>
					<div className="appearance-grid">
						{appearances.map(option => (
							<button key={option.id} className="appearance-option" aria-pressed={appearance === option.id} onClick={() => setAppearance(option.id)}>
								<span className={`theme-swatch swatch-${option.id}`} />
								<span className="appearance-copy"><strong>{option.label}</strong><small>{option.detail}</small></span>
							</button>
						))}
					</div>
				</section>

				<section className="settings-card" aria-labelledby="legal-heading">
					<h2 id="legal-heading">About</h2>
					<a className="link-row" href="/terms-of-use"><span>Terms of use</span><span className="link-arrow" aria-hidden="true">›</span></a>
					<a className="link-row" href="/privacy-policy"><span>Privacy policy</span><span className="link-arrow" aria-hidden="true">›</span></a>
				</section>

				<section className="settings-card logout-card" aria-label="Account actions">
					<button className="logout-button" onClick={() => { window.location.href = '/login'; }}>Log out</button>
				</section>
			</div>
		</main>
	);
}
