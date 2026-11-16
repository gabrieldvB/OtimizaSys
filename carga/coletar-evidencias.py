import csv
import json
import time
from pathlib import Path
from urllib.request import Request, urlopen


BASE = "http://localhost:8080"
LOG = Path("/tmp/otimiza-tools/api-sql.log")
RESULTADOS = Path("resultados")


def requisitar(caminho, metodo="GET", corpo=None):
    inicio_log = LOG.stat().st_size
    requisicao = Request(BASE + caminho, data=json.dumps(corpo).encode() if corpo else None,
                         headers={"Content-Type": "application/json"} if corpo else {}, method=metodo)
    inicio = time.perf_counter()
    with urlopen(requisicao, timeout=30) as resposta:
        conteudo = resposta.read()
        status = resposta.status
    tempo_ms = (time.perf_counter() - inicio) * 1000
    time.sleep(0.05)
    with LOG.open("rb") as arquivo:
        arquivo.seek(inicio_log)
        log = arquivo.read().decode(errors="replace")
    consultas = [linha.split(" : ", 1)[-1] for linha in log.splitlines() if "org.hibernate.SQL" in linha]
    return {"status": status, "tempo_ms": round(tempo_ms, 3), "bytes": len(conteudo),
            "json": json.loads(conteudo), "consultas": consultas}


def coletar_cache():
    etapas = [
        ("miss", "/api/clientes/42/com-cache", "GET", None),
        ("hit", "/api/clientes/42/com-cache", "GET", None),
        ("sem-cache", "/api/clientes/42/sem-cache", "GET", None),
        ("put", "/api/clientes/42", "PUT", {"nome": "Cliente atualizado"}),
        ("apos-put", "/api/clientes/42/com-cache", "GET", None),
        ("ttl-miss", "/api/clientes/44/com-cache", "GET", None),
        ("ttl-hit", "/api/clientes/44/com-cache", "GET", None),
    ]
    medidas = []
    for nome, caminho, metodo, corpo in etapas:
        medidas.append({"etapa": nome, **requisitar(caminho, metodo, corpo)})
    time.sleep(61)
    medidas.append({"etapa": "ttl-expirado", **requisitar("/api/clientes/44/com-cache")})
    (RESULTADOS / "cache-demonstracao.json").write_text(json.dumps(medidas, ensure_ascii=False, indent=2))
    return medidas


def coletar_orm():
    linhas = []
    sql = []
    for quantidade in (10, 50, 100):
        respostas = {}
        for variante in ("lazy", "eager", "otimizado"):
            medida = requisitar(f"/api/pedidos/{variante}?inicio=1&quantidade={quantidade}")
            respostas[variante] = medida["json"]
            linhas.append({"variante": variante, "quantidade": quantidade, "selects": len(medida["consultas"]),
                           "tempo_ms": medida["tempo_ms"], "bytes": medida["bytes"],
                           "itens": sum(pedido["quantidadeDeItens"] for pedido in medida["json"])})
            if quantidade == 10:
                sql.extend([f"{variante} ({quantidade} pedidos):", *medida["consultas"][:3], ""])
        if not (respostas["lazy"] == respostas["eager"] == respostas["otimizado"]):
            raise AssertionError(f"JSONs diferentes para {quantidade} pedidos")
    with (RESULTADOS / "orm-consultas.csv").open("w", newline="") as arquivo:
        tabela = csv.DictWriter(arquivo, fieldnames=list(linhas[0]), lineterminator="\n")
        tabela.writeheader()
        tabela.writerows(linhas)
    (RESULTADOS / "sql-representativo.txt").write_text("\n".join(sql))
    return linhas


if __name__ == "__main__":
    RESULTADOS.mkdir(exist_ok=True)
    print("Cache:", [(m["etapa"], len(m["consultas"]), m["tempo_ms"]) for m in coletar_cache()])
    print("ORM:", coletar_orm())
