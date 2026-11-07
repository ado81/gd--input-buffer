# GD Input Buffer

Proyecto de investigación para implementar un buffer de entrada nativo en Geometry Dash Android mediante Geode.

## Estado real

- [x] Scaffold nativo Geode creado.
- [x] Compilación Android32 correcta.
- [x] Compilación Android64 correcta.
- [x] Paquetes `.geode` generados por GitHub Actions.
- [ ] Confirmar carga del mod dentro de Geometry Dash 2.2.144 / bindings 2.2081.
- [ ] Verificar símbolos y firmas reales del binario Android de Geometry Dash para cada ABI.
- [ ] Implementar hooks de entrada y ciclo de simulación.
- [ ] Implementar detección fiable de frame seguro.
- [ ] Reproducir el toque pendiente en el hilo del juego.
- [ ] Probar niveles y escenarios de colisión en dispositivo real.

**Importante:** los paquetes actuales son scaffolds de diagnóstico. Solo registran que el mod se cargó; no interceptan ni retrasan toques y no implementan el buffer jugable.

## Compatibilidad objetivo

- Geometry Dash Android mostrado como 2.2.144.
- Versión unificada de bindings Geode: 2.2081 (confirmar version code 41 en la instalación).
- Geode SDK 5.11.2.
- ABI Android32 y Android64.

## Arquitectura

El buffer debe ejecutarse dentro del proceso del juego. Un overlay Android externo no puede consultar de forma fiable el frame ni el estado interno de colisión. La lógica debe interceptar la entrada y consultar el estado de simulación desde hooks nativos verificados.

## Construcción

GitHub Actions: [Build Geode Android mod](https://github.com/ado81/gd--input-buffer/actions/workflows/geode-mod.yml)

Los artefactos de CI son paquetes Geode separados por ABI. No son APKs del juego ni incluyen Geometry Dash. Para usarlos se necesita un entorno con Geode compatible.

## Seguridad y alcance

No se modifica ni redistribuye el APK original de Geometry Dash. No se usan offsets de Windows, macOS o iOS como si fueran offsets Android. Si no se puede confirmar que el estado es seguro, la futura integración debe conservar/cancelar la entrada en lugar de asumir seguridad.

Consulta [docs/GEODE_ANDROID_INTEGRATION.md](docs/GEODE_ANDROID_INTEGRATION.md) para los requisitos técnicos pendientes.
