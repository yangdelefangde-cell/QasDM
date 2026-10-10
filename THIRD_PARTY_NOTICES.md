# Third-party software

The Android APK uses JLaTeXMath Android 0.2.0 by Dimitry Ivanov, based on JLaTeXMath by Calixte Denizet and the Scilab contributors. It is distributed without modification under GPL version 2 or later with the upstream exception for linking independent modules. Original LICENSE and COPYING files are bundled under assets/licenses. Font licenses distributed inside the dependency remain included by upstream.

Source: https://github.com/noties/jlatexmath-android/tree/android

The standalone HTML continues to load KaTeX for web formula rendering. Android and web formula engines may differ in command support.

The UI localization tables were prepared with opencc-js 1.0.5 (The nk2028 Project, MIT) using OpenCC conversion dictionaries (Apache-2.0). The converter is a build-only helper; no converter script or remote translation service is required at runtime. The upstream MIT notice is retained in the standalone HTML and Android license assets. Source: https://github.com/nk2028/opencc-js and https://github.com/BYVoid/OpenCC.

KaTeX and Mermaid, loaded on demand by the web page, use the MIT license. QasDM's custom license applies only to its own code and assets and does not restrict independently licensed third-party components.
