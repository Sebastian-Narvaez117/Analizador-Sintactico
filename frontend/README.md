# MiniCompilador Frontend - Flask

Frontend visual para el analizador sintáctico de MiniCompilador.

## 🚀 Instalación

### 1. Instalar dependencias
```bash
pip install -r requirements.txt
```

### 2. Asegúrate de que el backend Java está ejecutándose
```bash
# En otra terminal, desde la raíz del proyecto
mvn spring-boot:run
```

El backend debe estar en: **http://localhost:8080**

### 3. Ejecutar el frontend
```bash
python app.py
```

El frontend estará disponible en: **http://localhost:5000**

## 📋 Estructura

```
frontend/
├── app.py                 # Servidor Flask principal
├── requirements.txt       # Dependencias Python
├── templates/
│   └── index.html        # Interfaz HTML
└── static/
    ├── style.css         # Estilos CSS (tema oscuro moderno)
    └── script.js         # Interactividad y conexión a API
```

## 🎨 Características

- ✨ Interfaz moderna con tema oscuro
- 🔌 Conexión automática al backend Java
- 📝 Editor de código con syntax highlighting natural
- 🏷️ Visualización de tokens como chips
- 🌳 Árbol de derivación formateado
- 📊 Tabla de transiciones Thompson
- ⌨️ Atajo Ctrl+Enter para analizar
- 🔄 Verificación automática del estado del backend

## 🎯 Cómo usar

1. **Escribe código** en el editor izquierdo
2. **Presiona "Analizar"** o usa **Ctrl+Enter**
3. **Observa los resultados** en los tabs:
   - **Resumen**: Estado del análisis
   - **Tokens**: Elementos léxicos detectados
   - **Árbol**: Árbol de derivación sintáctico
   - **Tabla Thompson**: Tabla de transiciones del autómata

## 📝 Ejemplos de código

```java
int x = 5;
```

```java
if (x > 0) {
    y = 10;
} else {
    y = 20;
}
```

## 🔧 Troubleshooting

### Backend desconectado
Si ves "🔴 Backend desconectado":
- Asegúrate de ejecutar `mvn spring-boot:run` en el proyecto raíz
- Verifica que está en puerto 8080
- Espera 5 segundos para la reconexión automática

### Error: "Address already in use"
Si el puerto 5000 está en uso:
```bash
# Cambiar puerto en app.py
app.run(debug=True, port=5001)
```

## 🛠️ Desarrollo

Para cambios en CSS/JS, recarga el navegador. Para cambios en Python, reinicia `app.py`.

Debug mode está activo: `app.run(debug=True, ...)`
