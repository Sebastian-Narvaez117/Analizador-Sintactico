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
    const dot = document.querySelector('.status-dot');
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

    if (!codigo.trim()) {
        mostrarError('Por favor, ingresa código para analizar');
        return;
    }

    mostrarCarga(true);
    ocultarError();

    try {
        const response = await fetch('/api/analizar', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ codigo })
        });

        const resultado = await response.json();

        if (!response.ok) {
            mostrarError(resultado.error || 'Error en el análisis');
            return;
        }

        mostrarResultados(resultado);

    } catch (error) {
        mostrarError('Error: ' + error.message);
    } finally {
        mostrarCarga(false);
    }
}

// ── Renderizado de resultados ──────────────────────────────────────── //

function mostrarResultados(r) {
    mostrarResumen(r);
    mostrarTokens(r);
    mostrarArbol(r);
    mostrarTablaThompson(r);
}

function mostrarResumen(r) {
    const resumenDiv = document.getElementById('resultadoResumen');
    const estado = r.balanceado ? '✓ Válido' : '✗ Inválido';

    resumenDiv.className = 'result-box ' + (r.balanceado ? 'success' : 'error');

    resumenDiv.innerHTML = `
        <h3>${estado}</h3>
        <p><strong>Balanceo:</strong> ${escapeHtml(r.mensajeBalanceo || 'N/A')}</p>
        <p><strong>Análisis:</strong> ${escapeHtml(r.mensaje || 'N/A')}</p>
    `;
}

function mostrarTokens(r) {
    const tokensDiv = document.getElementById('resultadoTokens');

    if (r.tokens && r.tokens.length > 0) {
        tokensDiv.innerHTML = r.tokens.map(token => {
            const tipo = clasificarToken(token);
            return `<span class="token-chip token-${tipo}" title="${escapeHtml(token)}">${chipLabel(token)}</span>`;
        }).join('');
    } else {
        tokensDiv.innerHTML = '<p class="placeholder">No hay tokens disponibles</p>';
    }
}

function mostrarArbol(r) {
    const arbolDiv = document.getElementById('resultadoArbol');

    if (r.arbolDerivacion) {
        let html = `<div class="arbol-visual">${construirArbolVisual(r.arbolDerivacion)}</div>`;

        if (r.arbolGraphviz) {
            html += `
                <div class="jflap-btns">
                    ${botonDescargaTexto(
                        'Descargar Árbol Graphviz (.dot)',
                        r.arbolGraphviz,
                        'arbol_derivacion.dot',
                        'text/vnd.graphviz'
                    )}
                </div>
            `;
        }

        arbolDiv.innerHTML = html;
    } else {
        arbolDiv.innerHTML = '<p class="placeholder">No hay árbol disponible (código inválido)</p>';
    }
}

function mostrarTablaThompson(r) {
    const tablaDiv = document.getElementById('resultadoTabla');

    let html = r.tablaThompson
        ? `<pre class="tabla-pre">${escapeHtml(r.tablaThompson)}</pre>`
        : '<p class="placeholder">No hay tabla disponible</p>';

    const hayJflap =
        r.jflapIdentificador ||
        r.jflapNumero ||
        r.jflapKeyword ||
        r.jflapBloqueControl;

    if (hayJflap) {
        html += '<div class="jflap-btns">';

        if (r.jflapIdentificador) {
            html += botonDescargaTexto(
                'Descargar AFN Identificador (.jff)',
                r.jflapIdentificador,
                'afn_identificador.jff',
                'application/xml'
            );
        }

        if (r.jflapNumero) {
            html += botonDescargaTexto(
                'Descargar AFN Número (.jff)',
                r.jflapNumero,
                'afn_numero.jff',
                'application/xml'
            );
        }

        if (r.jflapKeyword) {
            html += botonDescargaTexto(
                'Descargar AFN Keyword (.jff)',
                r.jflapKeyword,
                'afn_keyword.jff',
                'application/xml'
            );
        }

        if (r.jflapBloqueControl) {
            html += botonDescargaTexto(
                'Descargar AFN Bloque Control (.jff)',
                r.jflapBloqueControl,
                'afn_bloque_control.jff',
                'application/xml'
            );
        }

        html += '</div>';
    }

    tablaDiv.innerHTML = html;
}

function botonDescargaTexto(label, contenido, filename, mimeType) {
    const blob = new Blob([contenido], { type: mimeType });
    const url = URL.createObjectURL(blob);

    return `
        <a href="${url}" download="${filename}" class="btn btn-small btn-primary jflap-btn">
            ${label}
        </a>
    `;
}

// ── Clasificación de tokens ────────────────────────────────────────── //

function clasificarToken(token) {
    const match = token.match(/Token\((\w+)\s*\|/);
    const tipo = match ? match[1].toUpperCase() : '';

    const keywords = ['IF', 'ELSE', 'WHILE', 'FOR', 'RETURN', 'PRINT'];

    const tiposDato = [
        'INT', 'DOUBLE', 'FLOAT', 'LONG', 'BOOLEAN', 'CHAR', 'STRING',
        'T_INTEGER', 'T_DOUBLE', 'T_FLOAT', 'T_LONG', 'T_BOOLEAN', 'T_CHAR'
    ];

    const operadores = [
        'ASIGNACION', 'MAS', 'MENOS', 'MULTIPLICACION', 'DIVISION', 'MODULO',
        'IGUAL', 'IGUALIGUAL', 'NOIGUAL', 'DISTINTO', 'MENORQUE', 'MAYORQUE',
        'MENOREQUAL', 'MAYOREQUAL', 'MENOR_IGUAL', 'MAYOR_IGUAL',
        'AND', 'OR', 'NOT', 'INCREMENTO', 'DECREMENTO',
        'MAS_IGUAL', 'MENOS_IGUAL', 'MULT_IGUAL', 'DIV_IGUAL', 'MULT', 'DIV'
    ];

    const delimitadores = [
        'PARENABRE', 'PARENCIERRE', 'LLAVEABRE', 'LLAVECIERRE',
        'CORCHETEABRE', 'CORCHETECIERRE', 'PUNTOCOMA', 'COMA', 'PUNTO'
    ];

    const numeros = ['NUMERO'];
    const cadenas = ['LITERAL_CADENA', 'LITERAL_CHAR'];
    const booleanos = ['LITERAL_BOOL', 'LITERAL_NULO'];
    const comentarios = ['COMENTARIO_LINEA', 'COMENTARIO_BLOQUE'];
    const directivas = ['DIRECTIVA'];

    if (keywords.includes(tipo)) return 'keyword';
    if (tiposDato.includes(tipo)) return 'keyword';
    if (operadores.includes(tipo)) return 'operator';
    if (delimitadores.includes(tipo)) return 'delimiter';
    if (numeros.includes(tipo)) return 'number';
    if (cadenas.includes(tipo)) return 'string';
    if (booleanos.includes(tipo)) return 'number';
    if (comentarios.includes(tipo)) return 'comment';
    if (directivas.includes(tipo)) return 'directive';

    return 'identifier';
}

function chipLabel(token) {
    const match = token.match(/\|\s*([^|]+?)\s*\|/);
    return escapeHtml(match ? match[1] : token);
}

// ── Árbol visual ───────────────────────────────────────────────────── //

function construirArbolVisual(texto) {
    return texto
        .split('\n')
        .filter(linea => linea.trim())
        .map(linea => {
            const espacios = linea.search(/\S/);
            const nivel = Math.floor(espacios / 2);
            const contenido = linea.trim();
            const prefijo = nivel > 0 ? '├─ ' : '';

            return `
                <div class="arbol-nivel nivel-${nivel}">
                    ${prefijo}<span class="arbol-nodo">${escapeHtml(contenido)}</span>
                </div>
            `;
        })
        .join('');
}

// ── Tabs ───────────────────────────────────────────────────────────── //

function switchTab(tabName) {
    document.querySelectorAll('.tab-content').forEach(tab => {
        tab.classList.remove('active');
    });

    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.classList.remove('active');
    });

    document.getElementById(tabName).classList.add('active');

    if (event && event.target) {
        event.target.classList.add('active');
    }
}

// ── UI helpers ─────────────────────────────────────────────────────── //

function limpiar() {
    document.getElementById('codigo').value = '';

    document.getElementById('resultadoResumen').innerHTML =
        '<p class="placeholder">Ingresa código y presiona "Analizar"</p>';

    document.getElementById('resultadoTokens').innerHTML =
        '<p class="placeholder">Los tokens aparecerán aquí</p>';

    document.getElementById('resultadoArbol').innerHTML =
        '<p class="placeholder">El árbol de derivación aparecerá aquí</p>';

    document.getElementById('resultadoTabla').innerHTML =
        '<p class="placeholder">La tabla de transiciones aparecerá aquí</p>';

    ocultarError();
}

function mostrarCarga(mostrar) {
    document.getElementById('loading').classList.toggle('hidden', !mostrar);
}

function mostrarError(msg) {
    const div = document.getElementById('errorMsg');
    div.textContent = msg;
    div.classList.remove('hidden');

    setTimeout(() => {
        div.classList.add('hidden');
    }, 6000);
}

function ocultarError() {
    document.getElementById('errorMsg').classList.add('hidden');
}

function escapeHtml(texto) {
    if (typeof texto !== 'string') {
        return '';
    }

    const div = document.createElement('div');
    div.textContent = texto;
    return div.innerHTML;
}

// ── Init ───────────────────────────────────────────────────────────── //

document.addEventListener('DOMContentLoaded', () => {
    verificarBackend();
    setInterval(verificarBackend, 5000);

    const editor = document.getElementById('codigo');

    editor.addEventListener('keydown', e => {
        if (e.ctrlKey && e.key === 'Enter') {
            analizar();
        }
    });
});