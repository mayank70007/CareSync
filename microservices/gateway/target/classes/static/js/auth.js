(function(){
  function getToken(){ return localStorage.getItem('token'); }
  function getRole(){ return localStorage.getItem('role') || ''; }
  function isLoggedIn(){ return !!getToken(); }
  function authHeaders(){ const t = getToken(); return t ? { 'Authorization': 'Bearer ' + t } : {}; }
  function setAuthNote(msg){ try{ localStorage.setItem('auth.note', msg); }catch(_){} }
  function consumeAuthNote(){ try{ const m = localStorage.getItem('auth.note'); if(m) localStorage.removeItem('auth.note'); return m||''; }catch(_){ return ''; } }
  async function authFetch(input, init){
    const headers = { 'Accept':'application/json', ...(init&&init.headers)||{}, ...authHeaders() };
    const opts = { ...(init||{}), headers };
    const res = await fetch(input, opts);
    if(res.status === 401 || res.status === 403){
      setAuthNote('Your session has expired or you do not have access. Please sign in.');
      window.location.assign('/login.html');
      throw new Error('Unauthorized');
    }
    return res;
  }
  function ensureLogin(requiredRoles){
    if(!isLoggedIn()){ window.location.replace('/login.html'); return false; }
    if(Array.isArray(requiredRoles) && requiredRoles.length>0){
      const r = getRole();
      if(!requiredRoles.includes(r)){ window.location.replace('/'); return false; }
    }
    return true;
  }
  function renderAuthNav(loginId='loginLink', logoutId='logoutBtn'){
    const login = document.getElementById(loginId);
    const logout = document.getElementById(logoutId);
    if(login) login.style.display = isLoggedIn() ? 'none' : '';
    if(logout) logout.style.display = isLoggedIn() ? '' : 'none';
  }
  function logoutAndGoLogin(){ localStorage.clear(); window.location.assign('/login.html'); }
  function role(){ return getRole(); }

  window.getToken = getToken;
  window.getRole = getRole;
  window.role = role;
  window.isLoggedIn = isLoggedIn;
  window.authHeaders = authHeaders;
  window.authFetch = authFetch;
  window.ensureLogin = ensureLogin;
  window.renderAuthNav = renderAuthNav;
  window.logoutAndGoLogin = logoutAndGoLogin;
  window.consumeAuthNote = consumeAuthNote;
})();
