// ── Estado del backend ─────────────────────────────────────────────── //

function verificarBackend() {
    fetch('http://localhost:8080/api/analizar', {
        method: 'POST',
        headers: { 'Content-Type': 'text/plain' },
        body: 'test'
    })
    .then(() => actualizarEstado(true))
    .catch(() => actualizarEstado(false));
}

function actualizarEstado(online) {
    const dot  = document.querySelector('.status-dot');
    const text = document.getElementById('statusText');
    if (online) {
        dot.classList.add('online');
        text.textContent = 'Backend conectado';
    } else {
        dot.classList.remove('online');
        text.textContent = 'Backend desconectado';
    }
}

// ── Análisis ───────────────────────────────────────────────────────── //

async function analizar() {
    const codigo = document.getElementById('codigo').value;
    if (!codigo.trim()) { mostrarError('Por favor, ingresa código para analizar'); return; }

    mostrarCarga(true);
    ocultarError();

    try {
        const response = await fetch('/api/analizar', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ codigo })
        });

        const resultado = await response.json();
        if (!response.ok) { mostrarError(resultado.error || 'Error en el análisis'); return; }
        mostrarResultados(resultado);

    } catch (error) {
        mostrarError('Error: ' + error.message);
    } finally {
        mostrarCarga(false);
    }
}

// ── Renderizado de resultados ──────────────────────────────────────── //

function mostrarResultados(r) {
    // Resumen
    const resumenDiv  = document.getElementById('resultadoResumen');
    const estado      = r.balanceado ? '✓ Válido' : '✗ Inválido';
    resumenDiv.className = 'result-box ' + (r.balanceado ? 'success' : 'error');
    resumenDiv.innerHTML = `
        <h3>${estado}</h3>
        <p><strong>Balanceo:</strong> ${r.mensajeBalanceo || 'N/A'}</p>
        <p><strong>Análisis:</strong> ${r.mensaje || 'N/A'}</p>`;

    // Tokens
    const tokensDiv = document.getElementById('resultadoTokens');
    if (r.tokens && r.tokens.length > 0) {
        tokensDiv.innerHTML = r.tokens.map(token => {
            const tipo = clasificarToken(token);
            return `<span class="token-chip token-${tipo}" title="${escapeHtml(token)}">${chipLabel(token)}</span>`;
        }).join('');
    } else {
        tokensDiv.innerHTML = '<p class="placeholder">No hay tokens disponibles</p>';
    }

    // Árbol
    const arbolDiv = document.getElementById('resultadoArbol');
    if (r.arbolDerivacion) {
        arbolDiv.innerHTML = `<div class="arbol-visual">${construirArbolVisual(r.arbolDerivacion)}</div>`;
    } else {
        arbolDiv.innerHTML = '<p class="placeholder">No hay árbol disponible (código inválido)</p>';
    }

    // Tabla Thompson + botones JFLAP
    const tablaDiv = document.getElementById('resultadoTabla');
    let html = r.tablaThompson
        ? `<pre class="tabla-pre">${escapeHtml(r.tablaThompson)}</pre>`
        : '<p class="placeholder">No hay tabla disponible</p>';

    // Botones de descarga JFLAP
    if (r.jflapIdentificador || r.jflapNumero || r.jflapKeyword) {
        html += '<div class="jflap-btns">';
        if (r.jflapIdentificador)
            html += botonDescarga('AFN Identificador', r.jflapIdentificador, 'afn_identificador.jff');
        if (r.jflapNumero)
            html += botonDescarga('AFN Número', r.jflapNumero, 'afn_numero.jff');
        if (r.jflapKeyword)
            html += botonDescarga('AFN Keyword', r.jflapKeyword, 'afn_keyword.jff');
        html += '</div>';
    }

    tablaDiv.innerHTML = html;
}

function botonDescarga(label, contenido, filename) {
    const blob = new Blob([contenido], { type: 'application/xml' });
    const url  = URL.createObjectURL(blob);
    return `<a href="${url}" download="${filename}" class="btn btn-small btn-primary jflap-btn">
                Descargar ${label} (.jff)
            </a>`;
}

// ── Clasificación de tokens ────────────────────────────────────────── //

function clasificarToken(token) {
    const match = token.match(/Token\((\w+)\s*\|/);
    const tipo  = match ? match[1].toUpperCase() : '';

    const keywords    = ['IF','ELSE','WHILE','FOR','RETURN','PRINT'];
    const tipos_dato  = ['INT','DOUBLE','FLOAT','LONG','BOOLEAN','CHAR','STRING',
                         'T_INTEGER','T_DOUBLE','T_FLOAT','T_LONG','T_BOOLEAN','T_CHAR'];
    const operadores  = ['ASIGNACION','MAS','MENOS','MULTIPLICACION','DIVISION','MODULO',
                         'IGUAL','IGUALIGUAL','NOIGUAL','DISTINTO','MENORQUE','MAYORQUE',
                         'MENOREQUAL','MAYOREQUAL','MENOR_IGUAL','MAYOR_IGUAL',
                         'AND','OR','NOT','INCREMENTO','DECREMENTO',
                         'MAS_IGUAL','MENOS_IGUAL','MULT_IGUAL','DIV_IGUAL','MULT','DIV'];
    const delimitadores = ['PARENABRE','PARENCIERRE','LLAVEABRE','LLAVECIERRE',
                           'CORCHETEABRE','CORCHETECIERRE','PUNTOCOMA','COMA','PUNTO'];
    const numeros     = ['NUMERO'];
    const cadenas     = ['LITERAL_CADENA','LITERAL_CHAR'];
    const booleanos   = ['LITERAL_BOOL','LITERAL_NULO'];
    const comentarios = ['COMENTARIO_LINEA','COMENTARIO_BLOQUE'];
    const directivas  = ['DIRECTIVA'];

    if (keywords.includes(tipo))     return 'keyword';
    if (tipos_dato.includes(tipo))   return 'keyword';
    if (operadores.includes(tipo))   return 'operator';
    if (delimitadores.includes(tipo))return 'delimiter';
    if (numeros.includes(tipo))      return 'number';
    if (cadenas.includes(tipo))      return 'string';
    if (booleanos.includes(tipo))    return 'number';
    if (comentarios.includes(tipo))  return 'comment';
    if (directivas.includes(tipo))   return 'directive';
    return 'identifier';
}

/** Muestra solo el valor del token en el chip (el tipo va en el tooltip). */
function chipLabel(token) {
    const match = token.match(/\|\s*([^|]+?)\s*\|/);
    return escapeHtml(match ? match[1] : token);
}

// ── Árbol visual ───────────────────────────────────────────────────── //

function construirArbolVisual(texto) {
    return texto.split('\n').filter(l => l.trim()).map(linea => {
        const spaces  = linea.search(/\S/);
        const nivel   = Math.floor(spaces / 2);
        const contenido = linea.trim();
        const prefijo   = nivel > 0 ? '├─ ' : '';
        return `<div class="arbol-nivel nivel-${nivel}">${prefijo}<span class="arbol-nodo">${escapeHtml(contenido)}</span></div>`;
    }).join('');
}

// ── Tabs ───────────────────────────────────────────────────────────── //

function switchTab(tabName) {
    document.querySelectorAll('.tab-content').forEach(t => t.classList.remove('active'));
    document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
    document.getElementById(tabName).classList.add('active');
    event.target.classList.add('active');
}

// ── UI helpers ─────────────────────────────────────────────────────── //

function limpiar() {
    document.getElementById('codigo').value = '';
    ['resultadoResumen','resultadoTokens','resultadoArbol','resultadoTabla'].forEach(id => {
        document.getElementById(id).innerHTML = '<p class="placeholder">—</p>';
    });
    ocultarError();
}

function mostrarCarga(mostrar) {
    document.getElementById('loading').classList.toggle('hidden', !mostrar);
}

function mostrarError(msg) {
    const div = document.getElementById('errorMsg');
    div.textContent = msg;
    div.classList.remove('hidden');
    setTimeout(() => div.classList.add('hidden'), 6000);
}

function ocultarError() {
    document.getElementById('errorMsg').classList.add('hidden');
}

function escapeHtml(texto) {
    if (typeof texto !== 'string') return '';
    const d = document.createElement('div');
    d.textContent = texto;
    return d.innerHTML;
}

// ── Init ───────────────────────────────────────────────────────────── //

document.addEventListener('DOMContentLoaded', () => {
    verificarBackend();
    setInterval(verificarBackend, 5000);

    document.getElementById('codigo').addEventListener('keydown', e => {
        if (e.ctrlKey && e.key === 'Enter') analizar();
    });
});