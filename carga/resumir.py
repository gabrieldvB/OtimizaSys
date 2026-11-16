import csv
import json
import statistics
import sys
from collections import defaultdict
from pathlib import Path
from xml.sax.saxutils import escape


def ler_medidas(caminho):
    with caminho.open() as arquivo:
        metricas = json.load(arquivo)["metrics"]
    duracao = metricas["http_req_duration"]
    requisicoes = metricas["http_reqs"]
    erros = metricas["http_req_failed"]
    duracao = duracao.get("values", duracao)
    requisicoes = requisicoes.get("values", requisicoes)
    erros = erros.get("values", erros)
    return {
        "media_ms": duracao["avg"],
        "p95_ms": duracao["p(95)"],
        "p99_ms": duracao["p(99)"],
        "req_s": requisicoes["rate"],
        "erros_pct": 100 * erros.get("rate", erros.get("value", 0)),
    }


def resumir(diretorio):
    grupos = defaultdict(list)
    for caminho in sorted(diretorio.glob("*-r[123].json")):
        partes = caminho.stem.rsplit("-", 2)
        if len(partes) != 3 or not partes[1].isdigit():
            continue
        grupos[(partes[0], int(partes[1]))].append(ler_medidas(caminho))
    if not grupos:
        raise SystemExit("Nenhuma rodada k6 encontrada em resultados/")
    destino = diretorio / "resumo.csv"
    with destino.open("w", newline="") as arquivo:
        campos = ["experimento", "vus", "rodadas", "media_ms", "p95_ms", "p99_ms", "req_s", "erros_pct"]
        tabela = csv.DictWriter(arquivo, fieldnames=campos, lineterminator="\n")
        tabela.writeheader()
        for (nome, vus), rodadas in sorted(grupos.items()):
            if len(rodadas) != 3:
                raise SystemExit(f"{nome}/{vus}: esperado 3 rodadas, encontrado {len(rodadas)}")
            tabela.writerow({"experimento": nome, "vus": vus, "rodadas": 3, **{
                campo: round(statistics.median(rodada[campo] for rodada in rodadas), 3)
                for campo in campos[3:]
            }})
    faixas = diretorio / "faixas.csv"
    with faixas.open("w", newline="") as arquivo:
        tabela = csv.writer(arquivo, lineterminator="\n")
        tabela.writerow(("experimento", "vus", "metrica", "min", "max"))
        for (nome, vus), rodadas in sorted(grupos.items()):
            for campo in ("media_ms", "p95_ms", "p99_ms", "req_s", "erros_pct"):
                valores = [rodada[campo] for rodada in rodadas]
                tabela.writerow((nome, vus, campo, round(min(valores), 3), round(max(valores), 3)))
    print(destino)
    print(faixas)
    gerar_graficos(destino)


def gerar_graficos(resumo):
    with resumo.open(newline="") as arquivo:
        linhas = list(csv.DictReader(arquivo))
    for grupo in ("cache", "orm", "indice"):
        selecionadas = [linha for linha in linhas if linha["experimento"].startswith(grupo + "-")]
        if not selecionadas:
            continue
        for campo, titulo in (("p95_ms", "p95 (ms)"), ("req_s", "Requisições/s")):
            grafico = resumo.parent / f"{grupo}-{campo}.svg"
            grafico.write_text(desenhar_grafico(selecionadas, campo, titulo))
            print(grafico)


def desenhar_grafico(linhas, campo, titulo):
    cores = ("#2563eb", "#dc2626", "#16a34a", "#9333ea")
    nomes = sorted({linha["experimento"] for linha in linhas})
    limite = max(float(linha[campo]) for linha in linhas) * 1.15 or 1
    partes = ['<svg xmlns="http://www.w3.org/2000/svg" width="720" height="430" viewBox="0 0 720 430">',
              '<rect width="720" height="430" fill="white"/>',
              f'<text x="70" y="35" font-size="20">{escape(titulo)}</text>',
              '<path d="M70 60V350H650" fill="none" stroke="#333"/>']
    for fracao in (0, 0.25, 0.5, 0.75, 1):
        y = 350 - fracao * 270
        partes.append(f'<path d="M70 {y}H650" stroke="#e5e7eb"/>')
        partes.append(f'<text x="5" y="{y + 4}" font-size="11">{limite * fracao:.1f}</text>')
    for vus in (10, 50, 100):
        x = 90 + (vus - 10) * 6
        partes.append(f'<text x="{x - 8}" y="375" font-size="13">{vus}</text>')
    partes.append('<text x="325" y="405" font-size="14">Usuários virtuais</text>')
    for indice, nome in enumerate(nomes):
        valores = sorted((int(linha["vus"]), float(linha[campo])) for linha in linhas if linha["experimento"] == nome)
        pontos = [(90 + (vus - 10) * 6, 350 - valor * 270 / limite) for vus, valor in valores]
        cor = cores[indice % len(cores)]
        partes.append(f'<polyline points="{" ".join(f"{x},{y:.1f}" for x, y in pontos)}" fill="none" stroke="{cor}" stroke-width="3"/>')
        partes.extend(f'<circle cx="{x}" cy="{y:.1f}" r="4" fill="{cor}"/>' for x, y in pontos)
        partes.append(f'<text x="{380 + (indice % 2) * 160}" y="{48 + (indice // 2) * 17}" font-size="12" fill="{cor}">{escape(nome)}</text>')
    partes.append('</svg>')
    return "\n".join(partes)


if __name__ == "__main__":
    resumir(Path(sys.argv[1]) if len(sys.argv) > 1 else Path("resultados"))
