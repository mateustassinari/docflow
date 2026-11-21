#!/usr/bin/env bash
# Gera os diagramas do site (docs/diagramas/*.svg) a partir do modelo C4 em docs/workspace.dsl.
# Só precisa do Docker. Uso: ./docs/gerar-diagramas.sh
set -euo pipefail
cd "$(dirname "$0")"

tmp="$(mktemp -d)"
trap 'rm -rf "$tmp"' EXIT
usuario="$(id -u):$(id -g)"

# 1. Structurizr DSL -> C4-PlantUML (um arquivo por visão)
docker run --rm --user "$usuario" -v "$PWD:/in:ro" -v "$tmp:/out" \
  structurizr/structurizr:2026.10.26 export -w /in/workspace.dsl -f plantuml/c4plantuml -o /out

# O exportador não tem opção para desligar a legenda; as formas e cores já falam por si
sed -i '/^SHOW_LEGEND/d' "$tmp"/*.puml

# 2. C4-PlantUML -> SVG
docker run --rm --user "$usuario" -v "$tmp:/data" -w /data plantuml/plantuml:1.2026.10 -tsvg '*.puml'

mkdir -p diagramas
for svg in "$tmp"/*.svg; do
  nome="$(basename "$svg")"
  cp "$svg" "diagramas/${nome#structurizr-}"
done
echo "Diagramas gerados em docs/diagramas/"
