# Focused product comparison for v56

This research informs interface choices; no upstream implementation or design assets are copied.

| Project | Primary source | Relevant behavior | QasDM choice |
|---|---|---|---|
| RikkaHub | https://github.com/rikkahub/rikkahub ; https://github.com/rikkahub/docs/blob/main/quickstart.mdx | Provider configuration and feature model selection are distinct; supports custom URLs/models. | Provider preset + editable endpoint + fetched/manual model, reusable connections. Only OpenAI-compatible chat protocol is claimed here. |
| Kelivo | https://github.com/Chevey339/kelivo | Multi-provider client with model selection, tools and local data workflows. | Keep role conversations familiar while exposing actual execution and backup contents rather than hiding task failures. |
| SillyTavern | https://docs.sillytavern.app/usage/core-concepts/worldinfo/ ; https://docs.sillytavern.app/extensions/chat-vectorization/ | Keyword activation and optional vector-based retrieval help limit inserted memory/context. | Keyword retrieval with source labels and existing access permissions; no automatic insertion of every diary and no extra embedding API cost. |

v56 also follows the compatible speech request schema documented at https://developers.openai.com/api/reference/resources/audio/subresources/speech/methods/create . Online services remain separately configured by the user; actual model list and voice support depend on their account/service.

Future comparisons may examine broader task planning and embedding retrieval, based on user feedback rather than declaring full feature parity.
