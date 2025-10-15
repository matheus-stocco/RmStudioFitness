// ---------- Helpers UI ----------
const $ = s => document.querySelector(s);
const $$ = s => document.querySelectorAll(s);

const toasts = $("#toasts");
function toast(msg, ok=true){
  const el = document.createElement("div");
  el.className = "toast " + (ok ? "ok" : "err");
  el.textContent = msg;
  toasts?.appendChild(el);
  setTimeout(()=>{ el.style.opacity=0; setTimeout(()=>el.remove(),250) }, 2500);
}

function confirmDialog(text){
  return new Promise(resolve=>{
    const dlg = $("#modal-confirm");
    if(!dlg){ resolve(true); return; }
    $("#modal-text").textContent = text;
    dlg.showModal();
    const ok = $("#confirmar"), no = $("#cancelar");
    const off = () => { ok.onclick = no.onclick = null; dlg.close(); };
    ok.onclick = ()=>{ off(); resolve(true); };
    no.onclick = ()=>{ off(); resolve(false); };
    dlg.addEventListener("cancel", ()=>{ off(); resolve(false); }, {once:true});
  });
}

function debounce(fn, t=250){ let id; return (...a)=>{ clearTimeout(id); id=setTimeout(()=>fn(...a), t); }; }

// ---------- Loading States ----------
function showLoading() {
  const overlay = $("#loading-overlay");
  if (overlay) overlay.style.display = "flex";
}

function hideLoading() {
  const overlay = $("#loading-overlay");
  if (overlay) overlay.style.display = "none";
}

// ---------- Navbar mobile ----------
// Apenas executa se o elemento existir
document.addEventListener("DOMContentLoaded", () => {
  const navToggle = $(".nav-toggle");
  const navLinks = $(".nav-links");
  if (navToggle && navLinks) {
    navToggle.addEventListener("click", ()=>{
      navLinks.classList.toggle("open");
    });
  }
});

// ---------- Atalhos de teclado ----------
document.addEventListener("keydown", (e) => {
  // Ctrl/Cmd + K para focar na busca
  if ((e.ctrlKey || e.metaKey) && e.key === "k") {
    e.preventDefault();
    const buscar = $("#buscar");
    if (buscar) buscar.focus();
  }
  
  // Ctrl/Cmd + N para novo item
  if ((e.ctrlKey || e.metaKey) && e.key === "n") {
    e.preventDefault();
    const nome = $("#nome");
    if (nome) nome.focus();
  }
  
  // Escape para fechar modais
  if (e.key === "Escape") {
    const openModal = document.querySelector("dialog[open]");
    if (openModal) openModal.close();
  }
});

// ---------- Tooltips para botões ----------
function addTooltips() {
  const buttons = document.querySelectorAll('[title]');
  buttons.forEach(btn => {
    btn.addEventListener('mouseenter', (e) => {
      const tooltip = document.createElement('div');
      tooltip.className = 'tooltip';
      tooltip.textContent = e.target.title;
      document.body.appendChild(tooltip);
      
      const rect = e.target.getBoundingClientRect();
      tooltip.style.left = rect.left + (rect.width / 2) - (tooltip.offsetWidth / 2) + 'px';
      tooltip.style.top = rect.top - tooltip.offsetHeight - 8 + 'px';
    });
    
    btn.addEventListener('mouseleave', () => {
      const tooltip = document.querySelector('.tooltip');
      if (tooltip) tooltip.remove();
    });
  });
}

// Inicialização de tooltips
document.addEventListener("DOMContentLoaded", () => {
  addTooltips();
});
