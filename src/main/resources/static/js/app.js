var MESES = ['Enero','Febrero','Marzo','Abril','Mayo','Junio','Julio','Agosto','Septiembre','Octubre','Noviembre','Diciembre'];
var productosData = [];

document.addEventListener('DOMContentLoaded', function() {
    console.log('=== INICIANDO APP ===');

    var mes = new Date().getMonth() + 1;
    var ahora = new Date();

    document.getElementById('statusDate').textContent = MESES[ahora.getMonth()] + ' ' + ahora.getFullYear();

    function inicializarMeses() {
        var mesDesdeElement = document.getElementById('mesDesde');
        var mesHastaElement = document.getElementById('mesHasta');
        var fecha = new Date();
        var mesActual = fecha.getMonth() + 1;
        var mesSiguiente = (mesActual === 12) ? 1 : mesActual + 1;

        if (mesDesdeElement) {
            mesDesdeElement.value = mesActual;
            mesDesdeElement.disabled = true;
        } else {
            console.error('Elemento mesDesde no encontrado');
        }

        if (mesHastaElement) {
            mesHastaElement.value = mesActual;
            var options = mesHastaElement.querySelectorAll('option');
            for (var j = 0; j < options.length; j++) {
                var opMes = parseInt(options[j].value, 10);
                options[j].disabled = opMes < mesActual;
            }
        } else {
            console.error('Elemento mesHasta no encontrado');
        }
    }

    inicializarMeses();
    
    var chips = document.querySelectorAll('.chip');
    for (var i = 0; i < chips.length; i++) {
        chips[i].addEventListener('click', function() {
            var input = this.querySelector('input');
            var chip = this;
            setTimeout(function() {
                chip.classList.toggle('active', input.checked);
            }, 10);
        });
    }

    var btnCalcular = document.getElementById('btnCalcular');
    if (btnCalcular) {
        btnCalcular.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();
            calcularDemanda();
        });
    }
    
    initStars();
    console.log('=== APP INICIALIZADA ===');
});

function getCats() {
    var checked = document.querySelectorAll('.chip input:checked');
    var cats = [];
    for (var i = 0; i < checked.length; i++) {
        cats.push(checked[i].parentElement.dataset.value);
    }
    return cats;
}

function showAlert(type, title, message) {
    var modals = document.querySelectorAll('.modal-overlay');
    for (var i = 0; i < modals.length; i++) { modals[i].remove(); }
    var icons = {
        warning: '<svg width="24" height="24" fill="none" stroke="currentColor" stroke-width="2.2" viewBox="0 0 24 24"><path d="M12 9v4m0 4h.01M10.29 3.86l-8.6 14.86A1 1 0 002.56 20h18.88a1 1 0 00.87-1.28l-8.6-14.86a1 1 0 00-1.72 0z"/></svg>',
        error: '<svg width="24" height="24" fill="none" stroke="currentColor" stroke-width="2.2" viewBox="0 0 24 24"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>',
        success: '<svg width="24" height="24" fill="none" stroke="currentColor" stroke-width="2.2" viewBox="0 0 24 24"><path d="M22 11.08V12a10 10 0 11-5.93-9.14"/><path d="M22 4L12 14.01l-3-3"/></svg>'
    };
    var subs = { warning: 'Revisar', error: 'Error', success: 'Éxito' };
    var overlay = document.createElement('div');
    overlay.className = 'modal-overlay';
    overlay.innerHTML = '<div class="modal-box"><div class="modal-header"><div class="modal-icon ' + type + '">' + icons[type] + '</div><div><div class="modal-title">' + title + '</div><div class="modal-subtitle">' + subs[type] + '</div></div></div><div class="modal-body"><div class="modal-message">' + message + '</div></div><div class="modal-footer"><button class="modal-btn primary" onclick="this.parentElement.parentElement.parentElement.remove()">Aceptar</button></div></div>';
    document.body.appendChild(overlay);
}

function calcularDemanda() {
    var cats = getCats();
    if (cats.length === 0) { showAlert('warning', 'Sin categorías', 'Seleccione al menos una categoría.'); return; }

    var fecha = new Date();
    var desde = fecha.getMonth() + 1;
    var mesHastaElement = document.getElementById('mesHasta');
    var hasta = mesHastaElement ? parseInt(mesHastaElement.value, 10) : null;
    if (!hasta || isNaN(hasta)) { showAlert('warning', 'Mes hasta inválido', 'Seleccione un mes válido.'); return; }
    if (hasta < desde) { showAlert('warning', 'Mes inválido', 'No se puede seleccionar un mes anterior al actual.'); return; }

    var anioActual = fecha.getFullYear();
    var mesCoberturaInicio = (desde === 12) ? 1 : desde + 1;
    var anioCoberturaInicio = (desde === 12) ? anioActual + 1 : anioActual;
    var mesCoberturaFin = (hasta === 12) ? 1 : hasta + 1;
    var anioCoberturaFin = (hasta === 12) ? anioActual + 1 : anioActual;
    var coberturaTexto = MESES[mesCoberturaInicio - 1] + ' ' + anioCoberturaInicio;
    if (mesCoberturaInicio !== mesCoberturaFin || anioCoberturaInicio !== anioCoberturaFin) {
        coberturaTexto += ' a ' + MESES[mesCoberturaFin - 1] + ' ' + anioCoberturaFin;
    }

    var btn = document.getElementById('btnCalcular');
    btn.disabled = true;
    btn.textContent = 'Calculando...';
    toggleView('loading');

    var requestPromises = [];
    for (let mesPedido = desde; mesPedido <= hasta; mesPedido++) {
        for (let j = 0; j < cats.length; j++) {
            let categoria = cats[j];
            requestPromises.push(
                fetch('/api/prediccion/detalle', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ mes: mesPedido, anio: anioActual, categoria: categoria })
                })
                .then(function(r) { if (!r.ok) throw new Error('Error servidor: ' + r.status); return r.json(); })
                .then(function(data) { return { data: data, categoria: categoria, mesPedido: mesPedido }; })
            );
        }
    }

    Promise.all(requestPromises)
    .then(function(results) {
        var totalDemanda = 0, totalStock = 0, totalRecomendado = 0;
        productosData = [];
        results.forEach(function(result) {
            if (!result || !result.data) return;
            var data = result.data;
            var demandaCategoria = data.demandaTotalCategoria || 0;
            var stockCategoria = data.stockSeguridadTotal || 0;
            var recomendacionCategoria = (typeof data.recomendacion === 'number') ? data.recomendacion : demandaCategoria + stockCategoria;
            totalDemanda += demandaCategoria;
            totalStock += stockCategoria;
            totalRecomendado += recomendacionCategoria;
            if (data.detalleProductos && Array.isArray(data.detalleProductos)) {
                data.detalleProductos.forEach(function(item) {
                    productosData.push({ cat: result.categoria, nom: item.producto || 'Producto', cant: item.cantidadSugerida || 0, rango: MESES[result.mesPedido - 1] + ' ' + anioActual });
                });
            }
        });
        document.getElementById('kpiDemanda').textContent = totalDemanda;
        document.getElementById('kpiStock').textContent = totalStock;
        document.getElementById('kpiRecomendacion').textContent = totalRecomendado;
        document.getElementById('kpiProductos').textContent = productosData.length;
        var nombreMesDesde = MESES[desde - 1], nombreMesHasta = MESES[hasta - 1];
        document.getElementById('kpiSubPedido').textContent = (desde === hasta) ? 'Pedido: ' + nombreMesDesde + ' ' + anioActual : 'Pedidos: ' + nombreMesDesde + ' ' + anioActual + ' a ' + nombreMesHasta + ' ' + anioActual;
        document.getElementById('kpiSubObjetivo').textContent = 'Cobertura: ' + coberturaTexto;
        var inicioCobertura = MESES[mesCoberturaInicio - 1] + ' ' + anioCoberturaInicio;
        var textoFlujo = (desde === hasta) ? 'Pedido generado en ' + nombreMesDesde + ' ' + anioActual + ' para cubrir ' + inicioCobertura + '.' : 'Pedidos generados desde ' + nombreMesDesde + ' ' + anioActual + ' hasta ' + nombreMesHasta + ' ' + anioActual + ' para cubrir desde ' + inicioCobertura + ' hasta ' + MESES[mesCoberturaFin - 1] + ' ' + anioCoberturaFin + '.';
        document.getElementById('flujoBannerTexto').textContent = textoFlujo;
        document.getElementById('tablaSubtitulo').innerHTML = textoFlujo;
        renderTabla(productosData);
        toggleView('results');
    })
    .catch(function(error) { showAlert('error', 'Error', 'No se pudo conectar: ' + error.message); toggleView('empty'); })
    .finally(function() { btn.disabled = false; btn.textContent = 'Calcular Demanda'; });
}

function renderTabla(productos) {
    var tbody = document.getElementById('tbodyProductos');
    tbody.innerHTML = '';
    var catActual = '', numCat = 0;
    for (var i = 0; i < productos.length; i++) {
        var p = productos[i];
        if (p.cat !== catActual) {
            catActual = p.cat; numCat = 0;
            var rangoTexto = p.rango ? ' · Pedido: ' + p.rango : '';
            var trSep = document.createElement('tr');
            trSep.className = 'cat-sep';
            trSep.innerHTML = '<td></td><td colspan="3" class="cat-sep-cell">Categoría: ' + catActual + rangoTexto + '</td>';
            tbody.appendChild(trSep);
        }
        numCat++;
        var tr = document.createElement('tr');
        tr.innerHTML = '<td class="col-num">' + numCat + '</td><td class="col-producto">' + p.nom + '</td><td style="width:120px;font-size:12px;">Sug: <strong>' + p.cant + '</strong></td><td style="width:100px;"><input type="number" class="cell-input" value="' + p.cant + '" min="0" data-original="' + p.cant + '" onchange="ajustarTotal()"></td>';
        tbody.appendChild(tr);
    }
    actualizarTotalesFooter();
    document.getElementById('btnOrdenar').disabled = false;
    document.getElementById('ordenExito').classList.add('hidden');
}

function ajustarTotal() {
    var inputs = document.querySelectorAll('.cell-input');
    for (var i = 0; i < inputs.length; i++) {
        var orig = parseInt(inputs[i].dataset.original), act = parseInt(inputs[i].value) || 0;
        inputs[i].classList.toggle('edited', act !== orig);
    }
    actualizarTotalesFooter();
}

function actualizarTotalesFooter() {
    var inputs = document.querySelectorAll('.cell-input'), sug = 0, adj = 0;
    for (var i = 0; i < inputs.length; i++) { sug += parseInt(inputs[i].dataset.original); adj += parseInt(inputs[i].value) || 0; }
    document.getElementById('valSugerido').textContent = sug;
    document.getElementById('valAjustado').textContent = adj;
    document.getElementById('difTexto').innerHTML = adj !== sug ? '(Diferencia: <strong>' + (adj > sug ? '+' : '') + (adj - sug) + '</strong>)' : '(Sin modificaciones)';
}

function generarOrden() {
    var total = document.getElementById('valAjustado').textContent;
    document.getElementById('ordenExito').innerHTML = '<strong>Orden Generada.</strong> Se registraron <strong>' + total + ' unidades</strong>.';
    document.getElementById('ordenExito').classList.remove('hidden');
    document.getElementById('btnOrdenar').disabled = true;
    showAlert('success', 'Orden generada', 'Se registraron ' + total + ' unidades.');
}

function toggleView(v) {
    document.getElementById('emptyState').classList.add('hidden');
    document.getElementById('loadingState').classList.add('hidden');
    document.getElementById('resultsWrapper').classList.add('hidden');
    if (v === 'empty') document.getElementById('emptyState').classList.remove('hidden');
    if (v === 'loading') document.getElementById('loadingState').classList.remove('hidden');
    if (v === 'results') document.getElementById('resultsWrapper').classList.remove('hidden');
}

function nuevoPedido() {
    var mesActual = new Date().getMonth() + 1;
    var mesDesdeElement = document.getElementById('mesDesde');
    var mesHastaElement = document.getElementById('mesHasta');
    if (mesDesdeElement) { mesDesdeElement.value = mesActual; mesDesdeElement.disabled = true; }
    if (mesHastaElement) { mesHastaElement.value = mesActual; var opts = mesHastaElement.querySelectorAll('option'); for (var j = 0; j < opts.length; j++) { opts[j].disabled = parseInt(opts[j].value, 10) < mesActual; } }
    var inputs = document.querySelectorAll('.chip input');
    for (var i = 0; i < inputs.length; i++) inputs[i].checked = false;
    var chips = document.querySelectorAll('.chip');
    for (var i = 0; i < chips.length; i++) chips[i].classList.remove('active');
    productosData = [];
    document.getElementById('kpiDemanda').textContent = '0';
    document.getElementById('kpiStock').textContent = '0';
    document.getElementById('kpiRecomendacion').textContent = '0';
    document.getElementById('kpiProductos').textContent = '0';
    document.getElementById('kpiSubObjetivo').textContent = 'Para el mes a cubrir';
    document.getElementById('kpiSubPedido').textContent = 'Pedido: —';
    document.getElementById('tbodyProductos').innerHTML = '';
    document.getElementById('valSugerido').textContent = '0';
    document.getElementById('valAjustado').textContent = '0';
    document.getElementById('difTexto').textContent = '(Sin modificaciones)';
    document.getElementById('ordenExito').classList.add('hidden');
    document.getElementById('btnOrdenar').disabled = true;
    document.getElementById('flujoBannerTexto').textContent = '';
    document.getElementById('tablaSubtitulo').textContent = 'Revise y ajuste las cantidades antes de confirmar.';
    toggleView('empty');
}

// ============================================
// ✨ ESTRELLAS — NUEVA CAPA VISUAL Y ANIMACIÓN ✨
// ============================================
function initStars() {
    var canvas = document.getElementById('particleCanvas');
    if (!canvas) return;

    var ctx = canvas.getContext('2d');
    var stars = [];
    var particles = [];
    var rings = [];
    var flashes = [];

    var W = 0, H = 0;
    function resize() {
        canvas.width = window.innerWidth;
        canvas.height = window.innerHeight;
        W = canvas.width;
        H = canvas.height;
    }
    resize();
    window.addEventListener('resize', resize);

    var palette = [
        { r: 255, g: 90,  b: 160 },
        { r: 255, g: 140, b: 80  },
        { r: 255, g: 215, b: 80  },
        { r: 90,  g: 200, b: 255 },
        { r: 130, g: 240, b: 220 },
        { r: 190, g: 120, b: 255 },
        { r: 255, g: 110, b: 200 },
        { r: 120, g: 255, b: 170 }
    ];

    var GRID_COLS = 6, GRID_ROWS = 4;
    function densityMap() {
        var map = [];
        for (var r = 0; r < GRID_ROWS; r++) {
            map.push([]);
            for (var c = 0; c < GRID_COLS; c++) map[r].push(0);
        }
        for (var i = 0; i < stars.length; i++) {
            var s = stars[i];
            var cx = Math.min(GRID_COLS - 1, Math.max(0, Math.floor(s.x / W * GRID_COLS)));
            var cy = Math.min(GRID_ROWS - 1, Math.max(0, Math.floor(s.y / H * GRID_ROWS)));
            map[cy][cx]++;
        }
        return map;
    }

    function emptiestCell() {
        var map = densityMap();
        var bestR = 0, bestC = 0, bestV = Infinity;
        for (var r = 0; r < GRID_ROWS; r++) {
            for (var c = 0; c < GRID_COLS; c++) {
                var v = map[r][c] + Math.random() * 0.5;
                if (v < bestV) { bestV = v; bestR = r; bestC = c; }
            }
        }
        var cw = W / GRID_COLS, ch = H / GRID_ROWS;
        return {
            x: cw * bestC + cw * (0.2 + Math.random() * 0.6),
            y: ch * bestR + ch * (0.2 + Math.random() * 0.6)
        };
    }

    function makeStar(x, y) {
        var col = palette[Math.floor(Math.random() * palette.length)];
        var ang = Math.random() * Math.PI * 2;
        var spd = 0.6 + Math.random() * 1.2;
        var maxSz = 6 + Math.random() * 10;
        var growDur  = 55 + Math.floor(Math.random() * 35);
        var liveDur  = 90 + Math.floor(Math.random() * 90);
        var totalLife = growDur + liveDur;

        return {
            x: (x != null) ? x : Math.random() * W,
            y: (y != null) ? y : Math.random() * H,
            vx: Math.cos(ang) * spd,
            vy: Math.sin(ang) * spd,
            col: col,
            sz: 0,
            maxSz: maxSz,
            rot: Math.random() * Math.PI * 2,
            rotSpd: (Math.random() - 0.5) * 0.04,
            age: 0,
            growDur: growDur,
            liveDur: liveDur,
            totalLife: totalLife,
            pulsePhase: Math.random() * Math.PI * 2,
            pulseSpd: 0.08 + Math.random() * 0.08,
            wobble: Math.random() * Math.PI * 2,
            wobbleSpd: 0.015 + Math.random() * 0.02,
            exploded: false
        };
    }

    function makeParticle(x, y, col) {
        var ang = Math.random() * Math.PI * 2;
        var sp = 2.5 + Math.random() * 5.5;
        return {
            x: x, y: y,
            vx: Math.cos(ang) * sp,
            vy: Math.sin(ang) * sp,
            sz: 1.5 + Math.random() * 3.5,
            col: col,
            life: 0,
            maxLife: 30 + Math.floor(Math.random() * 30),
            drag: 0.95 + Math.random() * 0.03
        };
    }

    function makeRing(x, y, col) {
        return {
            x: x, y: y, r: 2,
            // Ondas mucho más grandes y notorias
            maxR: 150 + Math.random() * 150, 
            col: col, life: 0,
            maxLife: 30 + Math.floor(Math.random() * 20) // Un poco más de tiempo para apreciarlas
        };
    }

    function makeFlash(x, y, col) {
        return {
            x: x, y: y,
            sz: 10, 
            // Flash central inmenso
            maxSz: 150 + Math.random() * 100, 
            col: col, life: 0,
            maxLife: 15 + Math.floor(Math.random() * 10)
        };
    }

    function explode(s) {
        if (s.exploded) return;
        s.exploded = true;
        
        flashes.push(makeFlash(s.x, s.y, s.col));
        
        // Doble anillo shockwave (se ve brutal y no pesa porque no usa shadowBlur)
        rings.push(makeRing(s.x, s.y, s.col));
        rings.push(makeRing(s.x, s.y, s.col));
        
        // Partículas ajustadas
        var n = 12 + Math.floor(Math.random() * 10);
        for (var i = 0; i < n; i++) {
            particles.push(makeParticle(s.x, s.y, s.col));
        }
    }

    function starPath(cx, cy, oR, iR, rot) {
        var step = Math.PI / 5;
        var a = rot - Math.PI / 2;
        ctx.beginPath();
        for (var i = 0; i < 10; i++) {
            var rr = (i % 2 === 0) ? oR : iR;
            var px = cx + Math.cos(a) * rr;
            var py = cy + Math.sin(a) * rr;
            if (i === 0) ctx.moveTo(px, py); else ctx.lineTo(px, py);
            a += step;
        }
        ctx.closePath();
    }

    function drawStar(s, alpha, glowMul) {
        var oR = s.sz;
        if (oR < 0.3) return;
        var iR = oR * 0.45;
        var c = s.col;

        var glowR = oR * 2.5 * glowMul;
        var grad = ctx.createRadialGradient(s.x, s.y, oR * 0.3, s.x, s.y, glowR);
        grad.addColorStop(0, 'rgba(' + c.r + ',' + c.g + ',' + c.b + ',' + (alpha * 0.35) + ')');
        grad.addColorStop(0.4, 'rgba(' + c.r + ',' + c.g + ',' + c.b + ',' + (alpha * 0.12) + ')');
        grad.addColorStop(1, 'rgba(' + c.r + ',' + c.g + ',' + c.b + ',0)');
        ctx.fillStyle = grad;
        ctx.beginPath();
        ctx.arc(s.x, s.y, glowR, 0, Math.PI * 2);
        ctx.fill();

        ctx.fillStyle = 'rgba(' + c.r + ',' + c.g + ',' + c.b + ',' + alpha + ')';
        starPath(s.x, s.y, oR, iR, s.rot);
        ctx.fill();

        var cGrad = ctx.createRadialGradient(s.x, s.y, 0, s.x, s.y, oR * 0.45);
        cGrad.addColorStop(0, 'rgba(255,255,255,' + (alpha * 0.9) + ')');
        cGrad.addColorStop(1, 'rgba(255,255,255,0)');
        ctx.fillStyle = cGrad;
        ctx.beginPath();
        ctx.arc(s.x, s.y, oR * 0.45, 0, Math.PI * 2);
        ctx.fill();

        ctx.fillStyle = 'rgba(255,255,255,' + (alpha * 0.7) + ')';
        starPath(s.x, s.y, oR * 0.3, iR * 0.3, s.rot);
        ctx.fill();
    }

    // Aumentado a 80 para que se sienta más lleno en normal
    var MAX_STARS = 80; 
    
    // Spawn normal más rápido (100ms) para que se llene rápido después de la explosión
    var spawnInterval = setInterval(function() {
        if (stars.length < MAX_STARS) {
            var pos = (Math.random() < 0.7) ? emptiestCell() : { x: Math.random() * W, y: Math.random() * H };
            stars.push(makeStar(pos.x, pos.y));
        }
    }, 100);

    // ==========================================
    // 🎆 GRAN EXPLOSIÓN INICIAL EN TODA LA PANTALLA
    // ==========================================
    var initialBurst = 100;
    for (var b = 0; b < initialBurst; b++) {
        var ix = Math.random() * W;
        var iy = Math.random() * H;
        var star = makeStar(ix, iy);
        // Forzamos a que la estrella ya esté completamente formada
        star.age = star.growDur + 1; 
        star.sz = star.maxSz; 
        stars.push(star);
    }
    // Explotamos todas inmediatamente
    var initSnapshot = stars.slice();
    for (var b = 0; b < initSnapshot.length; b++) {
        explode(initSnapshot[b]);
    }
    // Limpiamos las estrellas explotadas (los efectos de partículas/rings siguen vivos)
    stars = stars.filter(function(s) { return !s.exploded; });

    var redistribTimer = setInterval(function() {
        if (stars.length < 6) return;
        var target = emptiestCell();
        var alive = stars.filter(function(s){ return !s.exploded && s.age > s.growDur; });
        if (alive.length === 0) return;
        var pick = alive[Math.floor(Math.random() * alive.length)];
        var dx = target.x - pick.x, dy = target.y - pick.y;
        var d = Math.sqrt(dx*dx + dy*dy) || 1;
        var sp = 0.4 + Math.random() * 0.5;
        pick.vx = (dx / d) * sp;
        pick.vy = (dy / d) * sp;
    }, 700);

    function animate() {
        ctx.clearRect(0, 0, W, H);

        var i, s;

        for (i = stars.length - 1; i >= 0; i--) {
            s = stars[i];
            s.age++;
            s.rot += s.rotSpd;
            s.pulsePhase += s.pulseSpd;
            s.wobble += s.wobbleSpd;

            s.x += s.vx + Math.sin(s.wobble) * 0.9;
            s.y += s.vy + Math.cos(s.wobble * 0.85) * 0.9;

            if (s.x < -30) s.x = W + 30;
            else if (s.x > W + 30) s.x = -30;
            if (s.y < -30) s.y = H + 30;
            else if (s.y > H + 30) s.y = -30;

            if (s.age <= s.growDur) {
                var t = s.age / s.growDur;
                t = t * t * (3 - 2 * t);
                s.sz = s.maxSz * t;
                var aGrow = t * 0.95;
                var pulseG = 0.75 + Math.sin(s.pulsePhase) * 0.25;
                drawStar(s, aGrow * pulseG, 1 + Math.sin(s.pulsePhase) * 0.3);
            }
            else if (s.age <= s.totalLife) {
                var pulse = 0.7 + Math.sin(s.pulsePhase) * 0.3;
                s.sz = s.maxSz * (0.92 + Math.sin(s.pulsePhase * 0.6) * 0.08);
                var aLive = 0.85 * pulse + 0.15;
                drawStar(s, aLive, 1.2 + Math.sin(s.pulsePhase) * 0.5);
            }
            else {
                explode(s);
                stars.splice(i, 1);
                continue;
            }
        }

        for (i = rings.length - 1; i >= 0; i--) {
            var rg = rings[i];
            rg.life++;
            if (rg.life >= rg.maxLife) { rings.splice(i, 1); continue; }
            var rt = rg.life / rg.maxLife;
            rg.r = rg.maxR * rt;
            var rAlpha = (1 - rt) * 0.7;
            ctx.strokeStyle = 'rgba(' + rg.col.r + ',' + rg.col.g + ',' + rg.col.b + ',' + rAlpha + ')';
            ctx.lineWidth = Math.max(1, 4 * (1 - rt)); // Línea un poco más gruesa
            ctx.beginPath();
            ctx.arc(rg.x, rg.y, rg.r, 0, Math.PI * 2);
            ctx.stroke();
        }

        for (i = flashes.length - 1; i >= 0; i--) {
            var fl = flashes[i];
            fl.life++;
            if (fl.life >= fl.maxLife) { flashes.splice(i, 1); continue; }
            var ft = fl.life / fl.maxLife;
            var fSize = fl.maxSz * (1 - (1 - ft) * (1 - ft));
            var fAlpha = (1 - ft) * 0.5;
            ctx.fillStyle = 'rgba(255,255,245,' + fAlpha + ')';
            ctx.beginPath();
            ctx.arc(fl.x, fl.y, fSize, 0, Math.PI * 2);
            ctx.fill();
        }

        for (i = particles.length - 1; i >= 0; i--) {
            var p = particles[i];
            p.life++;
            if (p.life >= p.maxLife) { particles.splice(i, 1); continue; }
            p.x += p.vx; p.y += p.vy;
            p.vx *= p.drag; p.vy *= p.drag;
            p.vy += 0.03;
            var pt = 1 - p.life / p.maxLife;
            var pAlpha = pt * 0.9;
            var pSize = p.sz * (0.3 + pt * 0.7);

            var pGrad = ctx.createRadialGradient(p.x, p.y, 0, p.x, p.y, pSize * 3);
            pGrad.addColorStop(0, 'rgba(' + p.col.r + ',' + p.col.g + ',' + p.col.b + ',' + (pAlpha * 0.5) + ')');
            pGrad.addColorStop(1, 'rgba(' + p.col.r + ',' + p.col.g + ',' + p.col.b + ',0)');
            ctx.fillStyle = pGrad;
            ctx.beginPath();
            ctx.arc(p.x, p.y, pSize * 3, 0, Math.PI * 2);
            ctx.fill();

            ctx.fillStyle = 'rgba(' + p.col.r + ',' + p.col.g + ',' + p.col.b + ',' + pAlpha + ')';
            ctx.beginPath();
            ctx.arc(p.x, p.y, pSize, 0, Math.PI * 2);
            ctx.fill();

            ctx.fillStyle = 'rgba(255,255,255,' + (pAlpha * 0.7) + ')';
            ctx.beginPath();
            ctx.arc(p.x, p.y, pSize * 0.35, 0, Math.PI * 2);
            ctx.fill();
        }

        requestAnimationFrame(animate);
    }
    animate();
}