(() => {
  const $ = s => document.querySelector(s);
  const $$ = s => [...document.querySelectorAll(s)];
  const io = new IntersectionObserver(entries => entries.forEach(e => { if (e.isIntersecting) e.target.classList.add('visible'); }), { threshold: .12 });
  $$('.reveal').forEach(el => io.observe(el));
  const modal = $('#buyModal');
  const openBuy = (plan) => { $('#modalPlan').textContent = plan; modal.classList.add('open'); modal.setAttribute('aria-hidden','false'); };
  $$('[data-buy]').forEach(b => b.addEventListener('click', () => openBuy(b.dataset.buy)));
  $('#closeModal')?.addEventListener('click', () => { modal.classList.remove('open'); modal.setAttribute('aria-hidden','true'); });
  modal?.addEventListener('click', e => { if (e.target === modal) $('#closeModal').click(); });
  document.addEventListener('keydown', e => { if (e.key === 'Escape') $('#closeModal')?.click(); });
  $('#year').textContent = new Date().getFullYear();
  const shell = $('#visualShell');
  if (shell && matchMedia('(pointer:fine)').matches) {
    shell.addEventListener('mousemove', e => { const r=shell.getBoundingClientRect(), x=(e.clientX-r.left)/r.width-.5, y=(e.clientY-r.top)/r.height-.5; shell.style.transform=`perspective(1000px) rotateY(${x*5}deg) rotateX(${-y*4}deg) translateY(-3px)`; });
    shell.addEventListener('mouseleave', () => shell.style.transform='');
  }
})();
