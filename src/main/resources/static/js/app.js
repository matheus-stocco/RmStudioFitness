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


// ---------- Gerenciamento de Papéis ----------
document.addEventListener('DOMContentLoaded', () => {
    const tabelaUsuariosBody = document.getElementById('tabelaUsuarios');
    if (!tabelaUsuariosBody) {
        return; // Só executa na página de gerenciamento
    }

    const buscaUsuarioInput = document.getElementById('buscaUsuario');
    const filtroRoleSelect = document.getElementById('filtroRole');

    const buscarUsuarios = async () => {
        const nome = buscaUsuarioInput.value;
        const role = filtroRoleSelect.value;
        
        try {
            const response = await fetch(`/api/gerenciamento/usuarios?nome=${encodeURIComponent(nome)}&role=${encodeURIComponent(role)}`);
            if (!response.ok) {
                throw new Error('Falha ao buscar usuários');
            }
            const usuarios = await response.json();
            
            tabelaUsuariosBody.innerHTML = ''; // Limpa a tabela
            if (usuarios.length === 0) {
                tabelaUsuariosBody.innerHTML = '<tr><td colspan="4" class="text-center">Nenhum usuário encontrado.</td></tr>';
                return;
            }

            usuarios.forEach(usuario => {
                const roles = usuario.perfis.map(p => p.replace('ROLE_', '')).join(', ');
                const isPersonal = usuario.perfis.includes('ROLE_PERSONAL');
                const isAdmin = usuario.perfis.includes('ROLE_ADMIN');

                let botoesAcao = '';
                if (!isAdmin) {
                    if (isPersonal) {
                        botoesAcao = `<button class="btn btn-danger btn-sm btn-remover-personal" data-id="${usuario.id}">Remover PERSONAL</button>`;
                    } else {
                        botoesAcao = `<button class="btn btn-success btn-sm btn-adicionar-personal" data-id="${usuario.id}">Tornar PERSONAL</button>`;
                    }
                } else {
                    botoesAcao = '<span class="text-muted">Admin</span>';
                }

                const linha = `
                    <tr>
                        <td>${usuario.nome}</td>
                        <td>${usuario.email}</td>
                        <td><span class="role-badge">${roles}</span></td>
                        <td class="text-center">${botoesAcao}</td>
                    </tr>`;
                tabelaUsuariosBody.insertAdjacentHTML('beforeend', linha);
            });
        } catch (error) {
            console.error('Erro:', error);
            tabelaUsuariosBody.innerHTML = '<tr><td colspan="4" class="text-center text-danger">Erro ao buscar usuários.</td></tr>';
        }
    };

    const manipularRole = async (id, acao) => {
        const url = `/api/gerenciamento/usuarios/${id}/${acao}-role-personal`;
        try {
            const response = await fetch(url, { method: 'POST' });
            if (!response.ok) {
                throw new Error(`Erro na API ao ${acao === 'adicionar' ? 'adicionar' : 'remover'} papel`);
            }
            buscarUsuarios(); // Atualiza a lista
        } catch (error) {
            console.error('Erro:', error);
            alert(`Erro ao ${acao === 'adicionar' ? 'adicionar' : 'remover'} o papel PERSONAL.`);
        }
    };

    buscaUsuarioInput.addEventListener('keyup', debounce(buscarUsuarios, 300));
    filtroRoleSelect.addEventListener('change', buscarUsuarios);

    tabelaUsuariosBody.addEventListener('click', (event) => {
        const target = event.target;
        const id = target.dataset.id;

        if (target.classList.contains('btn-adicionar-personal')) {
            manipularRole(id, 'adicionar');
        } else if (target.classList.contains('btn-remover-personal')) {
            manipularRole(id, 'remover');
        }
    });

    // Carga inicial
    buscarUsuarios();
});
