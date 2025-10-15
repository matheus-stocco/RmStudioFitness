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
$(".nav-toggle")?.addEventListener("click", ()=>{
  $(".nav-links")?.classList.toggle("open");
});

// ---------- Estados ----------
const tbody = $("#tbody");
let cache = [];           // cache da listagem
let sortBy = "id";        // coluna atual
let sortDir = "asc";      // asc/desc
let currentPage = 1;      // página atual
const itemsPerPage = 10;  // itens por página

// Atualizar estatísticas
function updateStats() {
  if(!$("#total-estados")) return; // Só se a UI existir
  const total = cache.length;
  const ativos = cache.length; // Todos os estados são considerados ativos
  const ultima = new Date().toLocaleString('pt-BR');
  
  $("#total-estados").textContent = total;
  $("#estados-ativos").textContent = ativos;
  $("#ultima-atualizacao").textContent = ultima;
}

// Paginação
function updatePagination() {
  const pagination = $("#pagination");
  if(!pagination) return;
  const totalPages = Math.ceil(cache.length / itemsPerPage);
  
  if (totalPages <= 1) {
    pagination.style.display = "none";
    return;
  }
  
  pagination.style.display = "flex";
  $("#current-page").textContent = currentPage;
  $("#total-pages").textContent = totalPages;
  
  $("#btn-prev").disabled = currentPage === 1;
  $("#btn-next").disabled = currentPage === totalPages;
}

// Navegação da paginação
$("#btn-prev")?.addEventListener("click", () => {
  if (currentPage > 1) {
    currentPage--;
    render();
  }
});

$("#btn-next")?.addEventListener("click", () => {
  const totalPages = Math.ceil(cache.length / itemsPerPage);
  if (currentPage < totalPages) {
    currentPage++;
    render();
  }
});

async function listar(){
  // Só executa em telas que usam esta listagem
  if(!tbody) return;
  try {
    showLoading();
    const r = await fetch("/api/estados?page=0&size=1000&sort=nome,asc");
    if(!r.ok){ 
      toast("Falha ao carregar estados", false); 
      return; 
    }
    const pageData = await r.json();
    cache = Array.isArray(pageData) ? pageData : (pageData.content || []);
    currentPage = 1; // Reset para primeira página
    updateStats();
    render();
  } catch (error) {
    toast("Erro de conexão: " + error.message, false);
  } finally {
    hideLoading();
  }
}

function render(){
  if(!tbody) return;
  let rows = [...cache];

  // filtro
  const q = ($("#buscar")?.value || "").trim().toLowerCase();
  if(q){
    rows = rows.filter(e => (e.nome||"").toLowerCase().includes(q) || (e.uf||"").toLowerCase().includes(q));
  }

  // ordenação
  rows.sort((a,b)=>{
    let x=a[sortBy] ?? "", y=b[sortBy] ?? "";
    if(typeof x === "string") x = x.toLowerCase();
    if(typeof y === "string") y = y.toLowerCase();
    return (x>y ? 1 : x<y ? -1 : 0) * (sortDir==="asc" ? 1 : -1);
  });

  // paginação
  const startIndex = (currentPage - 1) * itemsPerPage;
  const endIndex = startIndex + itemsPerPage;
  const paginatedRows = rows.slice(startIndex, endIndex);

  // desenha
  tbody.innerHTML = "";
  if(rows.length===0){
    const tr = document.createElement("tr");
    tr.innerHTML = `
      <td colspan="4" style="text-align: center; padding: 40px; color: var(--muted);">
        <i class="fas fa-search" style="font-size: 2rem; margin-bottom: 16px; display: block; opacity: 0.5;"></i>
        Nenhum estado encontrado.
        ${q ? `<br><small>Não há resultados para "${q}"</small>` : ''}
      </td>`;
    tbody.appendChild(tr);
    return;
  }
  
  for(const e of paginatedRows){
    const tr = document.createElement("tr");
    tr.innerHTML = `
      <td><strong>${e.id}</strong></td>
      <td>${e.nome}</td>
      <td>
        <span class="badge" title="Unidade Federativa">
          <span class="dot"></span> 
          ${e.uf}
        </span>
      </td>
      <td class="cell-actions">
        <button class="btn btn-ghost" data-view="${e.id}" title="Ver detalhes">
          <i class="fas fa-eye"></i>
        </button>
        <button class="btn btn-danger" data-del="${e.id}" title="Excluir">
          <i class="fas fa-trash"></i>
        </button>
      </td>`;
    tbody.appendChild(tr);
  }
  
  updatePagination();
}

// ordenar ao clicar no cabeçalho
$$("th[data-col]").forEach(th=>{
  th.addEventListener("click", ()=>{
    const col = th.getAttribute("data-col");
    if(sortBy===col) sortDir = (sortDir==="asc" ? "desc" : "asc");
    else { sortBy = col; sortDir = "asc"; }
    currentPage = 1; // Reset para primeira página
    render();
  });
});

// busca
$("#buscar")?.addEventListener("input", debounce(() => {
  currentPage = 1; // Reset para primeira página
  render();
}, 300));

// Botão de atualizar
$("#btn-refresh")?.addEventListener("click", listar);

// UF sempre em maiúsculas
$("#uf")?.addEventListener("input", e=>{
  e.target.value = e.target.value.replace(/[^a-zA-Z]/g,'').toUpperCase().slice(0,2);
});

// submit do formulário -> JSON (mantém seu controller atual que espera @RequestBody)
$("#form-estado")?.addEventListener("submit", async ev=>{
  ev.preventDefault();
  
  const nome = $("#nome").value.trim();
  const uf   = $("#uf").value.trim().toUpperCase();
  
  if(!nome || uf.length!==2){ 
    toast("Preencha nome e UF válidos", false); 
    return; 
  }

  try {
    showLoading();
    const r = await fetch("/api/estados", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ nome, uf })
    });

    if(!r.ok){ 
      const errorText = await r.text();
      toast("Erro ao salvar: " + errorText, false); 
      return; 
    }
    
    $("#form-estado").reset();
    toast("Estado salvo com sucesso! 🎉");
    await listar();
  } catch (error) {
    toast("Erro de conexão: " + error.message, false);
  } finally {
    hideLoading();
  }
});

// Visualizar detalhes
function showDetails(estado) {
  const detailId = $("#detail-id");
  const detailNome = $("#detail-nome");
  const detailUf = $("#detail-uf");
  const detailData = $("#detail-data");
  const modalDetails = $("#modal-details");
  
  if (detailId) detailId.textContent = estado.id;
  if (detailNome) detailNome.textContent = estado.nome;
  if (detailUf) detailUf.textContent = estado.uf;
  if (detailData) detailData.textContent = new Date().toLocaleDateString('pt-BR');
  if (modalDetails) modalDetails.showModal();
}

// Fechar modal de detalhes
const closeDetails = $("#close-details");
if (closeDetails) {
  closeDetails.addEventListener("click", () => {
    const modalDetails = $("#modal-details");
    if (modalDetails) modalDetails.close();
  });
}

// excluir (delegação)
if (tbody) {
  tbody.addEventListener("click", async ev=>{
    const btn = ev.target.closest("[data-del]");
    const viewBtn = ev.target.closest("[data-view]");
    
    if(viewBtn) {
      const id = viewBtn.getAttribute("data-view");
      const estado = cache.find(e => e.id == id);
      if(estado) showDetails(estado);
      return;
    }
    
    if(!btn) return;
    
    const id = btn.getAttribute("data-del");
    const estado = cache.find(e => e.id == id);
    
    if(!estado) return;
    
    if(!await confirmDialog(`Excluir o estado "${estado.nome}" (${estado.uf})?`)) return;
    
    try {
      showLoading();
      const r = await fetch(`/api/estados/${id}`, { method: "DELETE" });
      if(!r.ok){ 
        const errorText = await r.text();
        toast("Erro ao excluir: " + errorText, false); 
        return; 
      }
      toast("Estado excluído com sucesso! 🗑️");
      await listar();
    } catch (error) {
      toast("Erro de conexão: " + error.message, false);
    } finally {
      hideLoading();
    }
  });
}

// Atalhos de teclado
document.addEventListener("keydown", (e) => {
  // Ctrl/Cmd + K para focar na busca
  if ((e.ctrlKey || e.metaKey) && e.key === "k") {
    e.preventDefault();
    $("#buscar")?.focus();
  }
  
  // Ctrl/Cmd + N para novo estado
  if ((e.ctrlKey || e.metaKey) && e.key === "n") {
    e.preventDefault();
    $("#nome")?.focus();
  }
  
  // Escape para fechar modais
  if (e.key === "Escape") {
    const openModal = document.querySelector("dialog[open]");
    if (openModal) openModal.close();
  }
});

// Tooltips para botões
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

// boot
document.addEventListener("DOMContentLoaded", () => {
  addTooltips();
  // Não chamar listar() automaticamente pois pode conflitar com scripts específicos das páginas
  // Cada página deve chamar sua própria função de inicialização
});