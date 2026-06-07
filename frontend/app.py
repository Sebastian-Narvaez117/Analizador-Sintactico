from flask import Flask, render_template, request, jsonify
import requests
import json

app = Flask(__name__)

# Configuración del backend Java
BACKEND_URL = "http://localhost:8080/api/analizar"

@app.route('/')
def index():
    return render_template('index.html')

@app.route('/api/analizar', methods=['POST'])
def analizar():
    """Proxy que envía el código al backend Java y retorna el resultado"""
    try:
        codigo = request.get_json().get('codigo', '')
        
        if not codigo.strip():
            return jsonify({'error': 'El código no puede estar vacío'}), 400
        
        # Llamar al backend Java
        response = requests.post(
            BACKEND_URL,
            data=codigo,
            headers={'Content-Type': 'text/plain'},
            timeout=10
        )
        
        if response.status_code == 200:
            resultado = response.json()
            return jsonify(resultado)
        else:
            return jsonify({'error': 'Error en el backend'}), response.status_code
            
    except requests.exceptions.ConnectionError:
        return jsonify({'error': 'No se pudo conectar al backend. ¿Está ejecutándose en puerto 8080?'}), 503
    except Exception as e:
        return jsonify({'error': str(e)}), 500

if __name__ == '__main__':
    print("🚀 Frontend Flask ejecutándose en http://localhost:5000")
    print("📡 Backend esperado en http://localhost:8080")
    app.run(debug=True, port=5000)
