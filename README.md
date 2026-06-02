# CalcHistory (Android Native)

Una calculadora Android moderna y minimalista con historial persistente, desarrollada completamente en **Kotlin** y **Jetpack Compose**. Inicialmente diseñada como una aplicación web progresiva (PWA), esta versión representa una reescritura nativa completa para ofrecer el máximo rendimiento, animaciones fluidas y una integración profunda con el ecosistema de Android.

## Características Principales

*   **Historial Interactivo:** Todo lo que calcules se guarda automáticamente. Puedes tocar operaciones anteriores para reutilizar la fórmula o el resultado exacto en un nuevo cálculo.
*   **Sumar Historial:** Un botón dedicado para sumar rápidamente todos los resultados acumulados en tu lista.
*   **Modo Oscuro / Claro:** Cambia instantáneamente entre un tema brillante (con un diseño estilo tarjetas y sombras sutiles) y un tema oscuro profundo. La preferencia se guarda de forma persistente.
*   **UI Edge-to-Edge:** Diseño moderno que se extiende por toda la pantalla de tu dispositivo, tiñendo la barra de estado y de navegación dinámicamente según el tema seleccionado.
*   **Teclado Optimizado:** Un teclado de 5 filas inspirado en los estándares de la industria, diseñado para maximizar el espacio libre en pantalla para que el historial sea siempre el protagonista.

## Tecnologías Utilizadas

*   **Kotlin:** Lenguaje principal del proyecto.
*   **Jetpack Compose:** Todo el diseño de la interfaz y las animaciones están hechos de forma declarativa sin usar XML.
*   **ViewModel & StateFlow:** Arquitectura robusta y reactiva para manejar los estados matemáticos de forma instantánea.
*   **SharedPreferences:** Almacenamiento local persistente para el historial y las preferencias de color.

## Instalación y Pruebas

Para compilar este proyecto, necesitas **Android Studio**.

1.  Clona el repositorio:
    ```bash
    git clone https://github.com/MavDevGit/calchistory.git
    ```
2.  Abre la carpeta del proyecto en Android Studio.
3.  Conecta tu dispositivo Android (con la depuración USB activada) o inicia el Emulador de Android.
4.  Presiona el botón de **Run** (`Shift + F10`) para instalar el APK en tu dispositivo.

## Estructura del Código

-   `CalculatorViewModel.kt`: Contiene el "cerebro" matemático, maneja la validación de operaciones y el formato visual de los números.
-   `CalculatorScreen.kt`: Aloja toda la interfaz visual construida con componentes de Compose, el modo oscuro reactivo y las interacciones táctiles de la cuadrícula.
-   `MathParser.kt`: Lógica personalizada y segura para evaluar ecuaciones matemáticas en tiempo real.
-   `HistoryManager.kt`: Interfaz limpia para el almacenamiento persistente en el dispositivo.

---
*Este proyecto es parte de la reescritura nativa oficial de CalcHistory.*
