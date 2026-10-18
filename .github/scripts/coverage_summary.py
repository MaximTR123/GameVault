import csv
import os

path = "target/site/jacoco/jacoco.csv"

if not os.path.exists(path):
    print("JaCoCo report not found")
    raise SystemExit

packages = {}
with open(path, encoding="utf-8") as f:
    for row in csv.DictReader(f):
        d = packages.setdefault(row["PACKAGE"], [0, 0, 0, 0])
        d[0] += int(row["LINE_MISSED"])
        d[1] += int(row["LINE_COVERED"])
        d[2] += int(row["BRANCH_MISSED"])
        d[3] += int(row["BRANCH_COVERED"])


def pct(missed, covered):
    total = missed + covered
    return f"{100 * covered / total:.0f}%" if total else "n/a"


print("## Coverage (JaCoCo)")
print()
print("| Package | Lines | Branches |")
print("|---|---|---|")

total = [0, 0, 0, 0]
for name, d in sorted(packages.items()):
    print(f"| {name} | {pct(d[0], d[1])} | {pct(d[2], d[3])} |")
    for i in range(4):
        total[i] += d[i]

print(f"| **Total** | **{pct(total[0], total[1])}** | **{pct(total[2], total[3])}** |")